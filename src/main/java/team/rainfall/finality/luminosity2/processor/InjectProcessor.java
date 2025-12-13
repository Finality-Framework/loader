package team.rainfall.finality.luminosity2.processor;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.luminosity2.Processor;
import team.rainfall.finality.luminosity2.utils.AnnotationUtil;
import team.rainfall.finality.luminosity2.utils.InjectPosition;
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
            if (AnnotationUtil.annotationExists("Lteam/rainfall/finality/luminosity2/annotations/Inject;", method)) {
                FinalityLogger.debug("INJECT FROM "+classNode.name+" "+method.name);
                AnnotationNode injectAnnotation = AnnotationUtil.getAnnotation("Lteam/rainfall/finality/luminosity2/annotations/Inject;", method);
                if (injectAnnotation != null) {
                    String targetMethodName = (String) AnnotationUtil.getAnnotationValue("methodName", injectAnnotation);
                    InjectPosition position = (InjectPosition) AnnotationUtil.getAnnotationValue("position", injectAnnotation);
                    String locator = (String) AnnotationUtil.getAnnotationValue("locator", injectAnnotation);
                    boolean returnWithValue = true;//(Boolean) AnnotationUtil.getAnnotationValue("returnWithValue", injectAnnotation);

                    if (position == null) {
                        position = InjectPosition.HEAD; // 默认值
                    }

                    for (MethodNode targetMethod : classNode.methods) {
                        if (targetMethod.name.equals(targetMethodName)) {
                            FinalityLogger.debug("L2 Find Inject "+targetMethodName+" at position "+position);

                            switch (position) {
                                case HEAD:
                                    injectHead(method, targetMethod, returnWithValue);
                                    break;
                                case RETURN:
                                    injectReturn(method, targetMethod, returnWithValue);
                                    break;
                                case BEFORE_INVOKE:
                                    injectBeforeInvoke(method, targetMethod, locator, returnWithValue);
                                    break;
                                case AFTER_INVOKE:
                                    injectAfterInvoke(method, targetMethod, locator, returnWithValue);
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }

    void injectHead(MethodNode sourceNode, MethodNode targetNode, boolean returnWithValue) {
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

    void injectReturn(MethodNode sourceNode, MethodNode targetNode, boolean returnWithValue) {
        // 遍历目标方法的所有指令，找到所有返回指令
        for (AbstractInsnNode insn : targetNode.instructions) {
            // 检查是否是返回指令
            if (isReturnInstruction(insn)) {
                // 在返回指令前插入回调
                InsnList callback = createCallbackInsnList(sourceNode, targetNode, returnWithValue);
                targetNode.instructions.insertBefore(insn, callback);
            }
        }
    }

    void injectBeforeInvoke(MethodNode sourceNode, MethodNode targetNode, String locator, boolean returnWithValue) {
        if (locator == null || locator.isEmpty()) {
            FinalityLogger.warn("Locator is empty for BEFORE_INVOKE injection in method " + sourceNode.name);
            return;
        }

        // 遍历目标方法的所有指令，找到指定的方法调用
        for (AbstractInsnNode insn : targetNode.instructions) {
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                // 检查方法名是否匹配locator
                if (methodInsn.name.equals(locator)) {
                    // 在方法调用前插入回调
                    InsnList callback = createCallbackInsnList(sourceNode, targetNode, returnWithValue);
                    targetNode.instructions.insertBefore(insn, callback);
                }
            }
        }
    }

    void injectAfterInvoke(MethodNode sourceNode, MethodNode targetNode, String locator, boolean returnWithValue) {
        if (locator == null || locator.isEmpty()) {
            FinalityLogger.warn("Locator is empty for AFTER_INVOKE injection in method " + sourceNode.name);
            return;
        }

        // 遍历目标方法的所有指令，找到指定的方法调用
        for (AbstractInsnNode insn : targetNode.instructions) {
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                // 检查方法名是否匹配locator
                if (methodInsn.name.equals(locator)) {
                    // 在方法调用后插入回调
                    InsnList callback = createCallbackInsnList(sourceNode, targetNode, returnWithValue);
                    targetNode.instructions.insert(insn, callback);
                }
            }
        }
    }

    private InsnList createCallbackInsnList(MethodNode sourceNode, MethodNode targetNode, boolean returnWithValue) {
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
        } else {
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, classNode.name, sourceNode.name, sourceNode.desc, false));
        }

        if (returnWithValue) {
            insnList.add(new VarInsnNode(ALOAD, targetNode.maxLocals + 1));
            insnList.add(new FieldInsnNode(GETFIELD, "team/rainfall/finality/luminosity2/CallbackInfo", "returnValue", "Ljava/lang/Object;"));

            Type returnType = Type.getMethodType(targetNode.desc).getReturnType();

            switch (returnType.getDescriptor()) {
                case "I":
                    insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Integer"));
                    insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
                    insnList.add(new InsnNode(IRETURN));
                    break;
                case "J":
                    insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Long"));
                    insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false));
                    insnList.add(new InsnNode(LRETURN));
                    break;
                case "F":
                    insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Float"));
                    insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false));
                    insnList.add(new InsnNode(FRETURN));
                    break;
                case "D":
                    insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Double"));
                    insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false));
                    insnList.add(new InsnNode(DRETURN));
                    break;
                case "Z":
                    insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Boolean"));
                    insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false));
                    insnList.add(new InsnNode(IRETURN));
                    break;
                case "V":
                    insnList.add(new InsnNode(RETURN));
                    break;
                default:
                    insnList.add(new TypeInsnNode(CHECKCAST, returnType.getInternalName()));
                    insnList.add(new InsnNode(ARETURN));
                    break;
            }
        }

        return insnList;
    }

    private boolean isReturnInstruction(AbstractInsnNode insn) {
        int opcode = insn.getOpcode();
        return opcode >= IRETURN && opcode <= RETURN;
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
