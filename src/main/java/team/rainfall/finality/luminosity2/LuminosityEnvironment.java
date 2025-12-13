package team.rainfall.finality.luminosity2;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.FileManager;
import team.rainfall.finality.loader.plugin.PluginData;
import team.rainfall.finality.loader.gui.ErrorCode;
import team.rainfall.finality.loader.util.FinalityClassLoader;
import team.rainfall.finality.luminosity2.processor.*;
import team.rainfall.finality.luminosity2.utils.AnnotationUtil;
import team.rainfall.finality.luminosity2.utils.ClassInfo;
import team.rainfall.finality.luminosity2.utils.JarUtil;
import team.rainfall.finality.luminosity2.utils.Luminosity_ClassWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

public class LuminosityEnvironment {
    public ArrayList<ClassInfo> classInfos = new ArrayList<>();
    ArrayList<PluginData> pluginData = null;
    JarFile coreJar = null;
    FinalityClassLoader classLoader = new FinalityClassLoader(new URL[]{}, Thread.currentThread().getContextClassLoader());
    HashMap<String, List<ClassInfo>> classNodeMap = new HashMap<>();

    public LuminosityEnvironment(ArrayList<PluginData> pluginData, File coreJar) {
        Luminosity_ClassWriter.classLoader = classLoader;
        try {
            this.coreJar = new JarFile(coreJar);
            classLoader.addUrl2(coreJar.toURI().toURL());
            this.pluginData = pluginData;
            for (PluginData data : pluginData) {
                classLoader.addUrl2(data.file.toURI().toURL());
            }
        } catch (IOException ignored) {
        }
    }

    public void dispose() {
        pluginData = null;
        classInfos.clear();
        classInfos = null;
        classNodeMap.clear();
        classNodeMap = null;
    }

    public void run() {
        bind();
        runMixin();
        runFieldOp();
        writeBytes();
    }

    public void runMixin() {
        for (Map.Entry<String, List<ClassInfo>> entry : classNodeMap.entrySet()) {
            try {
                ClassNode targetNode = JarUtil.getClassFromJar(coreJar, entry.getKey());
                for (ClassInfo sourceInfo : entry.getValue()) {
                    MixinProcessor mixinProcessor = new MixinProcessor(sourceInfo.node, targetNode);
                    mixinProcessor.process();
                    NewFieldProcessor newFieldProcessor = new NewFieldProcessor(sourceInfo.node, targetNode);
                    newFieldProcessor.process();
                    InjectProcessor injectProcessor = new InjectProcessor(targetNode);
                    injectProcessor.process();
                }
                ClassInfo classInfo = new ClassInfo();
                classInfo.name = entry.getKey();
                classInfo.node = targetNode;
                classInfos.add(classInfo);
                FinalityLogger.info("Added into classInfos "+classInfo.name);
            } catch (FileNotFoundException e) {
                FinalityLogger.error("Failed to find target class " + entry.getKey(), e);
            }
        }
    }

    private void runFieldOp() {
        for (Map.Entry<String, List<ClassInfo>> entry : classNodeMap.entrySet()) {
            try {
                ClassNode targetNode = getTargetClass2(entry.getKey());
                if (targetNode == null) {
                    targetNode = JarUtil.getClassFromJar(coreJar, entry.getKey());
                }
                for (ClassInfo sourceInfo : entry.getValue()) {
                    MapBackProcessor mapBackProcessor = new MapBackProcessor(sourceInfo.node,sourceInfo.name.replaceAll("\\.","/"));
                    mapBackProcessor.process();
                    sourceInfo.node = mapBackProcessor.destNode;
                    SetterProcessor setterProcessor = new SetterProcessor(sourceInfo.node, targetNode);
                    setterProcessor.process();
                    GetterProcessor getterProcessor = new GetterProcessor(sourceInfo.node, targetNode);
                    getterProcessor.process();
                    classInfos.add(sourceInfo);
                }

            } catch (FileNotFoundException e) {
                FinalityLogger.error("Failed to find target class " + entry.getKey(), e);
            }
        }
    }

    public void bind() {
        for (PluginData pluginData : pluginData) {
            if (pluginData.manifest.useLuminosity) {
                for (String tweakClassName : pluginData.manifest.tweakClasses) {
                    try {
                        ClassNode node = JarUtil.getClassFromJar(pluginData.jarFile, tweakClassName);
                        AnnotationNode annotationNode = AnnotationUtil.getAnnotation("Lteam/rainfall/finality/luminosity2/annotations/Mixin;", node);
                        if (annotationNode != null) {
                            String targetClassName = (String) AnnotationUtil.getAnnotationValue("mixinClass", annotationNode);
                            List<ClassInfo> boundClasses = classNodeMap.get(targetClassName);
                            if (boundClasses != null) {
                                boundClasses.add(new ClassInfo(node,Type.getObjectType(node.name).getClassName()));
                            } else {
                                ArrayList<ClassInfo> tempNodes = new ArrayList<>();
                                tempNodes.add(new ClassInfo(node,Type.getObjectType(node.name).getClassName()));
                                classNodeMap.put(targetClassName, tempNodes);
                            }
                        }
                    } catch (Exception e) {
                        FinalityLogger.error("Exception while binding class " + tweakClassName, e);
                    }
                }
            }
        }
    }

    private ClassNode getTargetClass2(String key) {
        for (ClassInfo classInfo : classInfos) {
            if (classInfo.name.equals(key)) return classInfo.node;
        }
        return null;
    }

    /**
     * Write modified classes to a jar file.<br/>
     * This can be used to avoid class initialization problems.
     */
    public void writeBytes() {
        try {
            for (ClassInfo classInfo : classInfos) {
                Luminosity_ClassWriter classWriter = new Luminosity_ClassWriter(ClassWriter.COMPUTE_FRAMES);
                classInfo.node.accept(classWriter);
                classInfo.bytes = classWriter.toByteArray();
            }
            //Write and load classes from jar to avoid init problem
            CachePacker.packClassesIntoJar(classInfos, "./.finality/luminosity2.jar");
        } catch (Exception e) {
            ErrorCode.showInternalError("Sonata - 02");
            FinalityLogger.error("Exception while writing bytes", e);
            System.exit(1);
        }
    }


    public void load(FinalityClassLoader classLoader) throws MalformedURLException {
        classLoader.addUrl2(new File("./.finality/luminosity2.jar").toURI().toURL());
    }

}
