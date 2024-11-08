package team.rainfall.luminosity.tweakMethods;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class InjectMethod {
    public String sourceClass;
    public ClassNode classNode;
    public MethodNode methodNode;
    public String position;
    public String targetMethod;
    public String targetClass;
    public MethodNode targetNode;
    public ClassNode targetClassNode;
    public String getFullMethodName(){
        return targetClass + "." + targetMethod;
    }
    public String getFullClassName(){
        return targetClass;
    }
}
