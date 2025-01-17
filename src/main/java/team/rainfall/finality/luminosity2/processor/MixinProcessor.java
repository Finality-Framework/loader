package team.rainfall.finality.luminosity2.processor;

import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.luminosity2.Processor;
import team.rainfall.finality.luminosity2.utils.AnnotationUtil;
import team.rainfall.finality.luminosity2.utils.MethodUtil;

import java.util.ArrayList;

public class MixinProcessor implements Processor {
    ClassNode sourceNode;
    ClassNode targetNode;
    ArrayList<String> methodNameList = new ArrayList<>();
    public void process(){
        FinalityLogger.debug("Mixin Process S "+sourceNode.name+" T "+targetNode.name);
        Remapper remapper = new SimpleRemapper(sourceNode.name,targetNode.name);
        sourceNode.accept(new ClassRemapper(sourceNode,remapper));
        findMethods();
        mixinMethods();
    }
    public void findMethods(){
        for (MethodNode methodNode:targetNode.methods){
            if (AnnotationUtil.annotationExists("Lteam/rainfall/finality/luminosity2/annotations/Shadow;",methodNode)){
                continue;
            }
            methodNameList.add(methodNode.name+methodNode.desc);
        }
    }
    public void mixinMethods(){
        for(MethodNode methodNode:sourceNode.methods){
            if(methodNameList.contains(methodNode.name+methodNode.desc)){
                targetNode.methods.remove(MethodUtil.findMethodByName(targetNode,methodNode.name+methodNode.desc));
                targetNode.methods.add(methodNode);
            }
        }
    }


    public MixinProcessor(ClassNode sourceNode , ClassNode targetNode){
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
    }
}
