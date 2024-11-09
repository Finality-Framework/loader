package team.rainfall.luminosity;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.luminosity.transformers.AccessTransformer;
import team.rainfall.luminosity.transformers.InjectTransformer;
import team.rainfall.luminosity.utils.AsmUtil;
import team.rainfall.luminosity.tweakMethods.InjectMethod;

import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static team.rainfall.luminosity.utils.JarUtil.getClassFromJar;

public class TweakProcess {
    public final static int writeMode = ClassWriter.COMPUTE_FRAMES;
    //ASM needs dependencies to compute frames
    public static ClassLoader classLoader = new URLClassLoader(new URL[]{});
    public ArrayList<JarFile> pluginJars = new ArrayList<>();
    public JarFile targetJar = null;
    public File targetFile = null;
    public ArrayList<Plugin> plugins = new ArrayList<>();
    public ArrayList<TweakedClass> tweakedClasses = new ArrayList<>();
    InjectTransformer injectTransformer = new InjectTransformer();
    public TweakProcess(ArrayList<JarFile> pluginJars, JarFile targetJar) {
        this.pluginJars = pluginJars;
        this.targetJar = targetJar;
    }

    public void tweak() {
        FinalityLogger.info("Start injecting code");
        DependenciesClassLoader dependenciesClassLoader = new DependenciesClassLoader(new URL[]{},Thread.currentThread().getContextClassLoader());
        try {
            dependenciesClassLoader.addUrl2(targetFile.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        classLoader = dependenciesClassLoader;
        readManifests();
        inject();
        try {
            tweakAcc();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        tweakedClasses.forEach(tweakedClass -> System.out.println(tweakedClass.className));
    }
    public void tweakAcc() throws FileNotFoundException {
        for(Plugin plugin : plugins){
            for(String clzPath : plugin.manifest.publicizedClasses){
                ClassNode classNode = getClassFromJar(targetJar,clzPath);
                AccessTransformer.transform(classNode);
                TweakedClass tweakedClass = new TweakedClass();
                ClassWriter writer = new ClassWriter(writeMode);
                classNode.accept(writer);
                tweakedClass.className = clzPath;
                tweakedClass.classBytes = writer.toByteArray();
                boolean dontAdd = false;
                for (TweakedClass tweakedClass1 : tweakedClasses){
                    if (tweakedClass1.className.equals(tweakedClass.className)) {
                        dontAdd = true;
                        break;
                    }
                }
                if(!dontAdd) {
                    tweakedClasses.add(tweakedClass);
                }
            }
        }
    }
    public void inject() {
        ArrayList<InjectMethod> injectMethods = new ArrayList<>();
        for (Plugin plugin : plugins) {
            plugin.manifest.tweakClasses.forEach((tweakClass) -> {
                try {
                    ClassNode node = getClassFromJar(plugin.file, tweakClass);
                    if (AsmUtil.annotationExists("Lteam/rainfall/luminosity/annotations/Tweak;", node)) {
                        FinalityLogger.debug("Found tweak annotation in class " + tweakClass);
                        ClassNode targetClassNode = getClassFromJar(targetJar,AsmUtil.getAnnotationValue("targetClass", AsmUtil.getAnnotation("Lteam/rainfall/luminosity/annotations/Tweak;", node)).toString());
                        String targetClassName = AsmUtil.getAnnotationValue("targetClass", AsmUtil.getAnnotation("Lteam/rainfall/luminosity/annotations/Tweak;", node)).toString();
                        if(plugin.manifest.publicizedClasses.contains(targetClassName)) {
                            AccessTransformer.transform(targetClassNode);
                            plugin.manifest.publicizedClasses.remove(targetClassName);
                        }
                        for (MethodNode methodNode : node.methods) {
                            if (AsmUtil.annotationExists("Lteam/rainfall/luminosity/annotations/Inject;", methodNode)) {
                                FinalityLogger.debug("Found targetMethod " + methodNode.name);
                                String targetMethodName = AsmUtil.getAnnotationValue("targetMethod", AsmUtil.getAnnotation("Lteam/rainfall/luminosity/annotations/Inject;", methodNode)).toString();
                                MethodNode targetMethodNode = AsmUtil.getMethodFromClass(targetMethodName, targetClassNode);
                                InjectMethod injectMethod = new InjectMethod();
                                injectMethod.position = AsmUtil.getAnnotationValue("position", AsmUtil.getAnnotation("Lteam/rainfall/luminosity/annotations/Inject;", methodNode)).toString();
                                injectMethod.methodNode = methodNode;
                                injectMethod.targetClass = targetClassName;
                                injectMethod.targetMethod = targetMethodName;
                                injectMethod.targetClassNode = targetClassNode;
                                injectMethod.targetNode = targetMethodNode;
                                injectMethod.classNode = node;
                                injectMethod.sourceClass = tweakClass;
                                injectMethods.add(injectMethod);
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("Error while getting class: " + e.getMessage());
                }
            });
        }
        System.out.println(injectMethods.stream().collect(Collectors.groupingBy(InjectMethod::getFullMethodName)));
        injectMethods.stream().collect(Collectors.groupingBy(InjectMethod::getFullMethodName)).forEach((k,v)->{
            ClassNode targetClassNode = v.get(0).targetClassNode;
            MethodNode targetMethodNode = v.get(0).targetNode;
            injectTransformer.transform(v.get(0).sourceClass, v.toArray(new InjectMethod[0]), targetMethodNode);
        });
        injectMethods.stream().collect(Collectors.groupingBy(InjectMethod::getFullClassName)).forEach((k,v)->{
            ClassWriter writer = new ClassWriter(writeMode);
            ClassNode targetClassNode = v.get(0).targetClassNode;
            targetClassNode.accept(writer);
            byte[] bytes = writer.toByteArray();
            TweakedClass tweakedClass = new TweakedClass();
            tweakedClass.classBytes = bytes;
            tweakedClass.className = AsmUtil.getAnnotationValue("targetClass", AsmUtil.getAnnotation("Lteam/rainfall/luminosity/annotations/Tweak;", v.get(0).classNode)).toString();
            tweakedClasses.add(tweakedClass);
        });

    }

    public void readManifests() {
        for (JarFile jarFile : pluginJars) {
            Plugin plugin = new Plugin();
            plugin.file = jarFile;
            //Search for manifest
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().equalsIgnoreCase("modify.json")) {
                    try {
                        plugin.manifest = new TweakManifest(jarFile.getInputStream(entry));
                    } catch (IOException e) {
                        System.out.println("Error while reading manifest: " + e.getMessage());
                        break;
                    }
                    break;
                }
            }
            if (plugin.manifest == null) {
                continue;
            }
            plugins.add(plugin);
        }
    }
}
