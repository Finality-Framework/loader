package team.rainfall.luminosity.utils;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import team.rainfall.finality.FinalityLogger;

public class AsmUtil {
    public static String getFullInvokeName(MethodInsnNode methodInsnNode){
        return methodInsnNode.owner.replaceAll("/",".")+"."+methodInsnNode.name+methodInsnNode.desc;
    }
    public static boolean annotationExists (String name, MethodNode node) {
        if(node.visibleAnnotations == null) return false;
        for (AnnotationNode annotation : node.visibleAnnotations) {
            if (annotation.desc.equals(name)) {
                return true;
            }
        }
        return false;
    }
    public static boolean annotationExists (String name, ClassNode node) {
        FinalityLogger.info("START");
        if(node.visibleAnnotations == null) return false;
        FinalityLogger.info("START2");
        for (AnnotationNode annotation : node.visibleAnnotations) {
            FinalityLogger.info("START3 "+annotation.desc);
            if (annotation.desc.equals(name)) {
                return true;
            }
        }
        return false;
    }
    public static AnnotationNode getAnnotation(String desc,MethodNode node){
        if(node.visibleAnnotations == null) return null;
        for (AnnotationNode annotation : node.visibleAnnotations) {
            if (annotation.desc.equals(desc)) {
                return annotation;
            }
        }
        return null;
    }
    public static AnnotationNode getAnnotation(String desc,ClassNode node){
        if(node.visibleAnnotations == null) return null;
        for (AnnotationNode annotation : node.visibleAnnotations) {
            if (annotation.desc.equals(desc)) {
                return annotation;
            }
        }
        return null;
    }
    public static Object getAnnotationValue(String key, AnnotationNode node){
        boolean foundTarget = false;
        boolean isKey = true;
        for (Object obj : node.values){
            if(obj.toString().equals(key) && isKey){
                foundTarget = true;
                continue;
            }
            if(foundTarget){
                return obj;
            }
            isKey = !isKey;
        }
        return null;
    }
    public static MethodNode getMethodFromClass(String name,ClassNode node){
        if(name.contains("(") && name.contains(")")){
            return getMethodFromClass(name,node,true);
        }else {
            return getMethodFromClass(name,node,false);
        }
    }
    public static MethodNode getMethodFromClass(String name,ClassNode node,boolean isFullName){
        for (MethodNode method : node.methods) {
            if (method.name.equals(name) && !isFullName) {
                return method;
            }
            if((method.name+method.desc).equals(name) && isFullName) {
                return method;
            }
        }
        return null;
    }
}
