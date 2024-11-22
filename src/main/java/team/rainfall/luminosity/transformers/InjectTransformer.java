package team.rainfall.luminosity.transformers;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import team.rainfall.luminosity.utils.AsmUtil;
import team.rainfall.luminosity.utils.NumberUtil;
import team.rainfall.luminosity.tweakMethods.InjectMethod;

import java.util.ArrayList;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ARETURN;

public class InjectTransformer {
    LabelNode afterReturnLabel = new LabelNode();
    LabelNode beforeReturnLabel = new LabelNode();
    InsnNode returnNode;
    public void transform(String sourceOwner, InjectMethod[] injectMethods, MethodNode targetNode) {
        afterReturnLabel = new LabelNode();
        beforeReturnLabel = new LabelNode();
        targetNode.instructions.insert(buildReturnOpcodes(targetNode.desc, targetNode));
        for (InjectMethod method : injectMethods) {
            if (method.position.equals("HEAD")) {
                targetNode.instructions.insert(afterReturnLabel, buildInjectOpcodes(sourceOwner, method, targetNode));
            }
            if (method.position.equals("RETURN")) {
                ArrayList<AbstractInsnNode> returnNodes = new ArrayList<>();
                for (AbstractInsnNode node : targetNode.instructions) {
                    if(node.equals(returnNode)){
                        continue;
                    }
                    if (node.getOpcode() == ARETURN || node.getOpcode() == IRETURN || node.getOpcode() == LRETURN || node.getOpcode() == FRETURN || node.getOpcode() == DRETURN || node.getOpcode() == RETURN) {
                        returnNodes.add(node);
                    }
                }
                for (AbstractInsnNode node : returnNodes) {
                    //System.out.println(node);
                    targetNode.instructions.insertBefore(node, buildInjectOpcodes(sourceOwner, method, targetNode));
                }
            }
            if (method.position.equals("BEFOREINVOKE")) {
                ArrayList<AbstractInsnNode> invokeNodes = new ArrayList<>();
                for (AbstractInsnNode node : targetNode.instructions) {
                    if (node.getOpcode() == INVOKEVIRTUAL || node.getOpcode() == INVOKESTATIC || node.getOpcode() == INVOKESPECIAL || node.getOpcode() == INVOKEINTERFACE) {
                        invokeNodes.add(node);
                    }
                }
                String locateMethodName = AsmUtil.getAnnotationValue("methodName", AsmUtil.getAnnotation("Lteam/rainfall/luminosity/annotations/MethodLocate;", method.sourceMethodNode)).toString();
                int foundCounter = -1;
                int locateMethodOrder = (int) AsmUtil.getAnnotationValue("order", AsmUtil.getAnnotation("Lteam/rainfall/luminosity/annotations/MethodLocate;", method.sourceMethodNode));
                for (AbstractInsnNode node : invokeNodes) {
                    if(node instanceof MethodInsnNode){
                        MethodInsnNode methodInsnNode = (MethodInsnNode) node;
                        if(AsmUtil.getFullInvokeName(methodInsnNode).equals(locateMethodName)) {
                            foundCounter++;
                            if(foundCounter == locateMethodOrder) {
                                targetNode.instructions.insertBefore(node, buildInjectOpcodes(sourceOwner, method, targetNode));
                                break;
                            }
                        }
                    }
                }
            }
            if (method.position.equals("AFTERINVOKE")) {
                ArrayList<AbstractInsnNode> invokeNodes = new ArrayList<>();
                for (AbstractInsnNode node : targetNode.instructions) {
                    if (node.getOpcode() == INVOKEVIRTUAL || node.getOpcode() == INVOKESTATIC || node.getOpcode() == INVOKESPECIAL || node.getOpcode() == INVOKEINTERFACE) {
                        invokeNodes.add(node);
                    }
                }
                String locateMethodName = AsmUtil.getAnnotationValue("methodName", AsmUtil.getAnnotation("Lteam/rainfall/luminosity/annotations/MethodLocate;", method.sourceMethodNode)).toString();
                int foundCounter = -1;
                int locateMethodOrder = (int) AsmUtil.getAnnotationValue("order", AsmUtil.getAnnotation("Lteam/rainfall/luminosity/annotations/MethodLocate;", method.sourceMethodNode));
                for (AbstractInsnNode node : invokeNodes) {
                    if(node instanceof MethodInsnNode){
                        MethodInsnNode methodInsnNode = (MethodInsnNode) node;
                        if(AsmUtil.getFullInvokeName(methodInsnNode).equals(locateMethodName)) {
                            foundCounter++;
                            if(foundCounter == locateMethodOrder) {
                                targetNode.instructions.insert(node, buildInjectOpcodes(sourceOwner, method, targetNode));
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    InsnList buildInjectOpcodes(String sourceOwner, InjectMethod method, MethodNode targetNode) {
        InsnList insnList = new InsnList();
        MethodNode sourceNode = method.sourceMethodNode;
        insnList.add(new TypeInsnNode(NEW, "team/rainfall/luminosity/CallbackInfo"));
        insnList.add(new InsnNode(DUP));
        if (NumberUtil.isBitSet(targetNode.access, 3)) {
            insnList.add(new MethodInsnNode(INVOKESPECIAL, "team/rainfall/luminosity/CallbackInfo", "<init>", "()V", false));
        } else {
            insnList.add(new VarInsnNode(ALOAD, 0));
            insnList.add(new MethodInsnNode(INVOKESPECIAL, "team/rainfall/luminosity/CallbackInfo", "<init>", "(Ljava/lang/Object;)V", false));
        }
        insnList.add(new VarInsnNode(ASTORE, targetNode.maxLocals + 1));
        insnList.add(getLocalVarIndexOfParams(targetNode));
        insnList.add(new VarInsnNode(ALOAD, targetNode.maxLocals + 1));
        insnList.add(buildReturnOpcodes2(targetNode.desc));
        insnList.add(new VarInsnNode(ALOAD, targetNode.maxLocals + 1));
        insnList.add(new MethodInsnNode(INVOKESTATIC, sourceOwner.replaceAll("\\.", "/"), sourceNode.name, sourceNode.desc, false));
        insnList.add(new VarInsnNode(ALOAD, targetNode.maxLocals + 1));
        insnList.add(new FieldInsnNode(GETFIELD, "team/rainfall/luminosity/CallbackInfo", "isCancelled", "Z"));
        insnList.add(new JumpInsnNode(IFNE, beforeReturnLabel));
        return insnList;
    }

    InsnList buildReturnOpcodes(String desc, MethodNode targetNode) {
        InsnList insnList = new InsnList();
        insnList.add(new JumpInsnNode(GOTO, afterReturnLabel));
        insnList.add(beforeReturnLabel);
        insnList.add(new VarInsnNode(ALOAD, targetNode.maxLocals + 1));
        insnList.add(new FieldInsnNode(GETFIELD, "team/rainfall/luminosity/CallbackInfo", "retValue", "Ljava/lang/Object;"));
        switch (desc.charAt(desc.length() - 1)) {
            case 'V':
                insnList.add(new InsnNode(POP));
                returnNode = new InsnNode(RETURN);
                insnList.add(returnNode);
                break;
            case 'D':
                insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Double"));
                insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false));
                returnNode = new InsnNode(DRETURN);
                insnList.add(returnNode);
                break;
            case 'F':
                insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Float"));
                insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false));
                returnNode = new InsnNode(FRETURN);
                insnList.add(returnNode);
                break;
            case 'I':
                insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Integer"));
                insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
                returnNode = new InsnNode(IRETURN);
                insnList.add(returnNode);
                break;
            case 'J':
                insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Long"));
                insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false));
                returnNode = new InsnNode(LRETURN);
                insnList.add(returnNode);
                break;
            case 'Z':
                insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Boolean"));
                insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false));
                returnNode = new InsnNode(IRETURN);
                insnList.add(returnNode);
                break;
            case 'C':
                insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Character"));
                insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false));
                returnNode = new InsnNode(IRETURN);
                insnList.add(returnNode);
                break;
            case 'S':
                insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Short"));
                insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false));
                returnNode = new InsnNode(IRETURN);
                insnList.add(returnNode);
                break;
            case 'B':
                insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Byte"));
                insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false));
                returnNode = new InsnNode(IRETURN);
                insnList.add(returnNode);
                break;
            default:
                insnList.add(new TypeInsnNode(CHECKCAST, desc.split("\\)")[1].substring(1, desc.split("\\)")[1].length() - 1)));
                returnNode = new InsnNode(ARETURN);
                insnList.add(returnNode);
        }
        insnList.add(afterReturnLabel);
        return insnList;
    }

    InsnList buildReturnOpcodes2(String desc) {
        InsnList insnList = new InsnList();
        if (desc.charAt(desc.length() - 1) == 'V') {
            insnList.add(new InsnNode(ICONST_1));
            insnList.add(new FieldInsnNode(PUTFIELD, "team/rainfall/luminosity/CallbackInfo", "isRetVoid", "Z"));
        } else {
            insnList.add(new InsnNode(ICONST_0));
            insnList.add(new FieldInsnNode(PUTFIELD, "team/rainfall/luminosity/CallbackInfo", "isRetVoid", "Z"));
        }
        return insnList;
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
}
