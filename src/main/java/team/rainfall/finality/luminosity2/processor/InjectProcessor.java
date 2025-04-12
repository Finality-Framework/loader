package team.rainfall.finality.luminosity2.processor;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import team.rainfall.finality.luminosity2.Processor;
import team.rainfall.finality.luminosity2.utils.AnnotationUtil;
import team.rainfall.finality.luminosity2.utils.NumberUtil;

import java.util.ArrayList;

import static org.objectweb.asm.Opcodes.*;

/**
 * Inject an injection callback to target method.<br/>
 * Should be called after {@link MixinProcessor} executed.
 * @since 1.3.2
 * @author RedreamR
 */
public class InjectProcessor implements Processor {
    ClassNode classNode;

    @Override
    public void process() {
        for (MethodNode method : classNode.methods) {
            if (AnnotationUtil.annotationExists("Lteam/rainfall/finality/luminosity2/annotation/Inject;", method)) {
                AnnotationNode injectAnnotation = AnnotationUtil.getAnnotation("Lteam/rainfall/finality/luminosity2/annotation/Inject;", method);
                if (injectAnnotation != null) {
                    String targetMethodName = (String) AnnotationUtil.getAnnotationValue("methodName", injectAnnotation);
                    for (MethodNode targetMethod : classNode.methods) {
                        if (targetMethod.name.equals(targetMethodName)) {
                            injectHead(method, targetMethod);
                        }
                    }
                }

            }
        }
    }

    void injectHead(MethodNode sourceNode, MethodNode targetNode) {
        InsnList insnList = new InsnList();
        insnList.add(new TypeInsnNode(NEW, "team/rainfall/finality/luminosity2/CallbackInfo"));
        insnList.add(new InsnNode(DUP));
        insnList.add(new MethodInsnNode(INVOKESPECIAL, "team/rainfall/finality/luminosity2/CallbackInfo", "<init>", "()V", false));
        insnList.add(new VarInsnNode(ASTORE, targetNode.maxLocals + 1));
        if (!NumberUtil.isBitSet(sourceNode.access, 3)) {
            insnList.add(new VarInsnNode(ALOAD, 0));
        }
        insnList.add(getLocalVarIndexOfParams(targetNode));
        insnList.add(new VarInsnNode(ALOAD, targetNode.maxLocals + 1));
        if (NumberUtil.isBitSet(sourceNode.access, 3)) {
            insnList.add(new MethodInsnNode(INVOKESTATIC, classNode.name, sourceNode.name, sourceNode.desc, false));
        }else {
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, classNode.name, sourceNode.name, sourceNode.desc, false));
        }

        targetNode.instructions.insert(insnList);
    }
    InsnList getLocalVarIndexOfParams(MethodNode node) {
        int flag = 1;
        if (NumberUtil.isBitSet(node.access, 3)) {
            flag = 0;
        }
        ArrayList<Integer> indexes = new ArrayList<>();
        InsnList insnList = new InsnList();
        Type[] types = Type.getMethodType(node.desc).getArgumentTypes();
        //System.out.println(types.length);
        for (int i = 0; i < types.length; i++) {
            //System.out.println(types[i].getDescriptor());
            indexes.add(i);
        }
        for (Integer integer : indexes) {
            switch (types[integer].getDescriptor()) {
                case "I":
                case "Z":
                    insnList.add(new VarInsnNode(ILOAD, integer+flag));
                    break;
                case "J":
                    insnList.add(new VarInsnNode(LLOAD, integer+flag));
                    break;
                case "F":
                    insnList.add(new VarInsnNode(FLOAD, integer+flag));
                    break;
                case "D":
                    insnList.add(new VarInsnNode(DLOAD, integer+flag));
                    break;
                case "L":
                default:
                    insnList.add(new VarInsnNode(ALOAD, integer+flag));
                    break;

            }
        }
        return insnList;
    }

    public InjectProcessor(ClassNode classNode){
        this.classNode = classNode;
    }
}
