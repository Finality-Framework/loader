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
import java.util.Iterator;

public class MixinProcessor implements Processor {
    ClassNode sourceNode;
    ClassNode targetNode;
    ArrayList<String> methodNameList = new ArrayList<>();
    public void process(){
        FinalityLogger.debug("Mixin Process S "+sourceNode.name+" T "+targetNode.name);
        ClassNode destNode = new ClassNode();
        removeShadows();
        Remapper remapper = new SimpleRemapper(sourceNode.name,targetNode.name);
        sourceNode.accept(new ClassRemapper(destNode,remapper));
        sourceNode = destNode;
        findMethods();
        mixinMethods();
    }
    public void removeShadows(){
        Iterator<MethodNode> nodeIterator = sourceNode.methods.iterator();
        while (nodeIterator.hasNext()){
            MethodNode methodNode = nodeIterator.next();
            FinalityLogger.debug("DBG1 " + methodNode.name);
            if (AnnotationUtil.annotationExists("Lteam/rainfall/finality/luminosity2/annotations/Shadow;", methodNode)) {
                FinalityLogger.debug("ShadowMethod " + methodNode.name);
                nodeIterator.remove();
            }
        }
    }
    public void findMethods(){

        for (MethodNode methodNode:targetNode.methods){
            methodNameList.add(methodNode.name+methodNode.desc);
        }
    }
    public void mixinMethods(){
        for(MethodNode methodNode:sourceNode.methods){
            //Skip init method
            if(methodNode.name.equals("<init>") && !AnnotationUtil.annotationExists("Lteam/rainfall/finality/luminosity2/annotations/Overwrite;", methodNode)) continue;
            if(methodNode.name.equals("<clinit>") && !AnnotationUtil.annotationExists("Lteam/rainfall/finality/luminosity2/annotations/Overwrite;", methodNode)) continue;
            if(methodNameList.contains(methodNode.name+methodNode.desc)){
                targetNode.methods.remove(MethodUtil.findMethodByName(targetNode,methodNode.name+methodNode.desc));
                methodNameList.remove(methodNode.name+methodNode.desc);
            }
            FinalityLogger.debug("L2 Mixin "+methodNode.name+" to "+targetNode.name);
            targetNode.methods.add(methodNode);
            methodNameList.add(methodNode.name+methodNode.desc);
        }
    }

    public MixinProcessor(ClassNode sourceNode , ClassNode targetNode){
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
    }
}
