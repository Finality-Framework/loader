package team.rainfall.finality.luminosity2.utils;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class MethodUtil {
    public static MethodNode findMethodByName(ClassNode classNode, String name){
        for(MethodNode methodNode:classNode.methods) {
            String fullName = methodNode.name+methodNode.desc;
            if (name.equals(fullName)) {
                return methodNode;
            }
        }
        return null;
    }
    public static String getFullName(MethodNode methodNode){
        return methodNode.name+methodNode.desc;
    }
}
