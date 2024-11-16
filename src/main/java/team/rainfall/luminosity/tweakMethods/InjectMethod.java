package team.rainfall.luminosity.tweakMethods;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class InjectMethod {
    public String sourceClassName;
    public ClassNode sourceClassNode;
    public MethodNode sourceMethodNode;
    public String position;
    public String targetMethodName;
    public String targetClassName;
    public MethodNode targetMethodNode;
    public ClassNode targetClassNode;
    public String getFullMethodName(){
        return targetClassName + "." + targetMethodName;
    }
    public String getFullClassName(){
        return targetClassName;
    }
}
