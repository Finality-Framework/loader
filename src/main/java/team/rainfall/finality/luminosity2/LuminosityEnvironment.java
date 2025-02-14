package team.rainfall.finality.luminosity2;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.plugin.PluginData;
import team.rainfall.finality.loader.util.ErrorCode;
import team.rainfall.finality.loader.util.FinalityClassLoader;
import team.rainfall.finality.loader.util.FinalityException;
import team.rainfall.finality.luminosity2.processor.MixinProcessor;
import team.rainfall.finality.luminosity2.utils.AnnotationUtil;
import team.rainfall.finality.luminosity2.utils.ClassInfo;
import team.rainfall.finality.luminosity2.utils.JarUtil;
import team.rainfall.finality.luminosity2.utils.Luminosity_ClassWriter;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    FinalityClassLoader classLoader = new FinalityClassLoader(new URL[]{},Thread.currentThread().getContextClassLoader());
    HashMap<String, List<ClassNode>> classNodeMap = new HashMap<>();
    public LuminosityEnvironment(ArrayList<PluginData> pluginData, File coreJar){
        Luminosity_ClassWriter.classLoader = classLoader;
        try {
            this.coreJar = new JarFile(coreJar);
            classLoader.addUrl2(coreJar.toURI().toURL());
            this.pluginData = pluginData;
            for(PluginData data : pluginData){
                FinalityLogger.debug("L2 "+data.manifest.id);
                classLoader.addUrl2(data.file.toURI().toURL());
            }
        } catch (IOException ignored) {
        }
    }
    public void run(){
        bind();
        runMixin();
        writeBytes();
    }
    public void runMixin(){
        for(Map.Entry<String,List<ClassNode>> entry : classNodeMap.entrySet()){
            try {
                ClassNode targetNode = JarUtil.getClassFromJar(coreJar,entry.getKey());
                for(ClassNode classNode  : entry.getValue()){
                    MixinProcessor mixinProcessor = new MixinProcessor(classNode,targetNode);
                    mixinProcessor.process();
                }
                ClassInfo classInfo = new ClassInfo();
                classInfo.name = entry.getKey();
                classInfo.node = targetNode;
                classInfos.add(classInfo);
            } catch (FileNotFoundException e) {
                FinalityLogger.error("Failed to find target class "+entry.getKey(),e);
            }
        }
    }
    public void bind(){
        for(PluginData pluginData : pluginData){
            if(pluginData.manifest.useLuminosity){
                for(String tweakClassName : pluginData.manifest.tweakClasses){
                    try {
                        ClassNode node = JarUtil.getClassFromJar(pluginData.jarFile,tweakClassName);
                        AnnotationNode annotationNode = AnnotationUtil.getAnnotation("Lteam/rainfall/finality/luminosity2/annotations/Mixin;",node);
                        if (annotationNode != null) {
                            String targetClassName = (String) AnnotationUtil.getAnnotationValue("mixinClass",annotationNode);
                            List<ClassNode> boundClasses = classNodeMap.get(targetClassName);
                            if(boundClasses != null){
                                boundClasses.add(node);
                            }else {
                                ArrayList<ClassNode> tempNodes = new ArrayList<>();
                                tempNodes.add(node);
                                classNodeMap.put(targetClassName,tempNodes);
                            }
                        }
                    } catch (Exception e) {
                        FinalityLogger.error("Exception while binding class "+tweakClassName,e);
                    }
                }
            }
        }
    }
    // 写出字节
    public void writeBytes(){
        try {
            for (ClassInfo classInfo : classInfos) {
                Luminosity_ClassWriter classWriter = new Luminosity_ClassWriter(ClassWriter.COMPUTE_FRAMES);
                classInfo.node.accept(classWriter);
                classInfo.bytes = classWriter.toByteArray();
            }
        }catch (Exception e){
            ErrorCode.showInternalError("Sonata - 02");
            FinalityLogger.error("Exception while writing bytes",e);
            System.exit(1);
        }
    }
    public void load(FinalityClassLoader classLoader){
        for(ClassInfo classInfo:classInfos){
            classLoader.defineClass2(classInfo.name,classInfo.bytes,0,classInfo.bytes.length);
        }
    }

}
