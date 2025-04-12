package team.rainfall.luminosity;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.MethodNode;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.plugin.PluginData;
import team.rainfall.finality.loader.plugin.TweakManifest;
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
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static team.rainfall.luminosity.utils.JarUtil.getClassFromJar;
public class TweakProcess {
    public final static int writeMode = ClassWriter.COMPUTE_FRAMES;
    //ASM needs dependencies to compute frames
    public static ClassLoader classLoader = new URLClassLoader(new URL[]{});
    public ArrayList<File> pluginFiles = new ArrayList<>();
    public JarFile targetJar;
    public File targetFile = null;
    public ArrayList<Plugin> plugins = new ArrayList<>();
    public ArrayList<TweakedClass> tweakedClasses = new ArrayList<>();
    InjectTransformer injectTransformer = new InjectTransformer();
    public TweakProcess(ArrayList<PluginData> pluginDatas, JarFile targetJar) {
        pluginDatas.forEach((v) -> {
            if(v.manifest.useLuminosity) {
                this.pluginFiles.add(v.file);
            }
        });
        this.targetJar = targetJar;
    }
    public void tweak() {
        FinalityLogger.info("Code injection started");
        DependenciesClassLoader dependenciesClassLoader = new DependenciesClassLoader(new URL[]{},Thread.currentThread().getContextClassLoader());
        try {
            dependenciesClassLoader.addUrl2(targetFile.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        classLoader = dependenciesClassLoader;
        readManifests();
        tweakOverlay();
        inject();
        try {
            tweakAcc();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        computeBytes();
        FinalityLogger.info("Code injection finished");
        //tweakedClasses.forEach(tweakedClass -> System.out.println(tweakedClass.className));
    }

    public void tweakOverlay(){
        for (Plugin plugin : plugins) {

            Iterator<String> iterator = plugin.manifest.tweakClasses.iterator();
            while (iterator.hasNext()){
                String tweakClass = iterator.next();
                try {
                    ClassNode node = getClassFromJar(plugin.jarFile, tweakClass);
                    if (AsmUtil.annotationExists("Lteam/rainfall/luminosity/annotations/Overlay;", node)) {
                        if(plugin.manifest.sdkVersion <= 1){
                            FinalityLogger.warn("Plugin "+plugin.manifest.packageName+" tried to use overlay feature,but SDK version is too low("+plugin.manifest.sdkVersion+")");
                            FinalityLogger.warn("Overlay tweak won't be applied!");
                            break;
                        }
                        TweakedClass tweakedClass = new TweakedClass();
                        tweakedClass.classNode = node;
                        tweakedClass.className = tweakClass;
                        tweakedClasses.add(tweakedClass);
                        iterator.remove();
                    }
                } catch (Exception e) {
                    FinalityLogger.error("Error while tweaking overlay class " + tweakClass,e);
                }
            }
        }
    }

    public void tweakAcc() throws FileNotFoundException {
        for(Plugin plugin : plugins){
            for(String clzPath : plugin.manifest.publicizedClasses){
                ClassNode classNode = null;
                for(TweakedClass tweakedClass : tweakedClasses){
                    if(tweakedClass.className.equals(clzPath)){
                        classNode = tweakedClass.classNode;
                    }
                }
                if(classNode == null) classNode = getClassFromJar(targetJar,clzPath);
                AccessTransformer.transform(classNode);
                TweakedClass tweakedClass = new TweakedClass();
                tweakedClass.className = clzPath;
                tweakedClass.classNode = classNode;
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
            for(String tweakClass:plugin.manifest.tweakClasses){
                try {
                    ClassNode node = getClassFromJar(plugin.jarFile, tweakClass);
                    if (AsmUtil.annotationExists("Lteam/rainfall/luminosity/annotations/Tweak;", node)) {
                        FinalityLogger.debug("Found tweak annotation in class " + tweakClass);
                        String targetClassName = AsmUtil.getAnnotationValue("targetClass", AsmUtil.getAnnotation("Lteam/rainfall/luminosity/annotations/Tweak;", node)).toString();
                        ClassNode targetClassNode = null;
                        for(TweakedClass tweakedClass : tweakedClasses){
                            if(tweakedClass.className.equals(targetClassName)){
                                targetClassNode = tweakedClass.classNode;
                            }
                        }
                        if(targetClassNode == null) targetClassNode = getClassFromJar(targetJar,targetClassName);
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
                                injectMethod.sourceMethodNode = methodNode;
                                injectMethod.targetClassName = targetClassName;
                                injectMethod.targetMethodName = targetMethodName;
                                injectMethod.targetClassNode = targetClassNode;
                                injectMethod.targetMethodNode = targetMethodNode;
                                injectMethod.sourceClassNode = node;
                                injectMethod.sourceClassName = tweakClass;
                                injectMethods.add(injectMethod);
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    FinalityLogger.error("Error while getting class: " + e.getMessage());
                }
            }
        }
        injectMethods.stream().collect(Collectors.groupingBy(InjectMethod::getFullMethodName)).forEach((k,v)->{
            MethodNode targetMethodNode = v.get(0).targetMethodNode;
            injectTransformer.transform(v.get(0).sourceClassName, v.toArray(new InjectMethod[0]), targetMethodNode);
        });
        injectMethods.stream().collect(Collectors.groupingBy(InjectMethod::getFullClassName)).forEach((k,v)->{
            ClassNode targetClassNode = v.get(0).targetClassNode;
            TweakedClass tweakedClass = new TweakedClass();
            tweakedClass.classNode = targetClassNode;
            tweakedClass.className = AsmUtil.getAnnotationValue("targetClass", AsmUtil.getAnnotation("Lteam/rainfall/luminosity/annotations/Tweak;", v.get(0).sourceClassNode)).toString();
            tweakedClasses.add(tweakedClass);
        });

    }

    public void readManifests() {
        for (File file : pluginFiles) {
            String absolutePath = file.getAbsoluteFile().getAbsolutePath();
            try {
                Plugin plugin = new Plugin();
                JarFile jarFile = new JarFile(file);
                plugin.file = file;
                plugin.jarFile = jarFile;
                //Search for manifest
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().equalsIgnoreCase("modify.json")) {
                        try {
                            plugin.manifest = new TweakManifest(jarFile.getInputStream(entry));
                        } catch (IOException e) {
                            FinalityLogger.error("Error while reading manifest: " + e.getMessage());
                            break;
                        }
                        break;
                    }
                }

                if (plugin.manifest == null) {
                    continue;
                }
                if (plugin.manifest.sdkVersion <= 0) {
                    FinalityLogger.warn("Found incompatible plugin " + absolutePath + " using SDK version " + plugin.manifest.sdkVersion);
                    FinalityLogger.warn("This plugin won't be loaded!");
                    continue;
                }
                AtomicBoolean shouldContinue = new AtomicBoolean(false);
                plugins.forEach((v)->{
                    if(plugin.manifest.packageName.equals(v.manifest.packageName)){
                        FinalityLogger.warn("Found duplicate plugin " + absolutePath + " , package name is " + plugin.manifest.packageName);
                        FinalityLogger.warn("This plugin won't be loaded!");
                        shouldContinue.set(true);
                    }
                });
                if(shouldContinue.get()){
                    continue;
                }
                plugins.add(plugin);
            } catch (Exception e) {
                FinalityLogger.error("Error while reading the manifest of plugin "+absolutePath,e);
            }
        }
    }
    public void computeBytes(){
        for(TweakedClass tweakedClass : tweakedClasses){
            if(tweakedClass.classBytes == null){
                ClassWriter writer = new ClassWriter(writeMode);
                tweakedClass.classNode.accept(writer);
                tweakedClass.classBytes = writer.toByteArray();
            }
        }
    }
}
