package team.rainfall.finality.luminosity2.processor;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.luminosity2.Processor;
import team.rainfall.finality.luminosity2.utils.AnnotationUtil;
import team.rainfall.finality.luminosity2.utils.InjectPosition;
import team.rainfall.finality.luminosity2.utils.MethodUtil;
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
        if (classNode == null) {
            FinalityLogger.warn("InjectProcessor: classNode is null");
            return;
        }
        if (classNode.methods == null) {
            return;
        }

        for (MethodNode method : classNode.methods) {
            if (method == null) continue;

            if (AnnotationUtil.annotationExists("Lteam/rainfall/finality/luminosity2/annotations/Inject;", method)) {
                FinalityLogger.debug("INJECT FROM " + classNode.name + " " + method.name);
                AnnotationNode injectAnnotation = AnnotationUtil.getAnnotation("Lteam/rainfall/finality/luminosity2/annotations/Inject;", method);
                if (injectAnnotation != null) {
                    String targetMethodName = (String) AnnotationUtil.getAnnotationValue("methodName", injectAnnotation);
                    String[] positionRaw = (String[])AnnotationUtil.getAnnotationValue("position", injectAnnotation);
                    InjectPosition position = InjectPosition.HEAD;
                    if (positionRaw != null && positionRaw.length > 1) {
                        position = InjectPosition.valueOf(positionRaw[1]);
                    }
                    String locator = (String) AnnotationUtil.getAnnotationValue("locator", injectAnnotation);
                    Boolean returnWithValue = (Boolean) AnnotationUtil.getAnnotationValue("returnWithValue", injectAnnotation);

                    if (targetMethodName == null || targetMethodName.isEmpty()) {
                        FinalityLogger.warn("InjectProcessor: targetMethodName is empty for method " + method.name);
                        continue;
                    }

                    if (position == null) {
                        position = InjectPosition.HEAD;
                    }

                    if (returnWithValue == null) {
                        returnWithValue = false;
                    }

                    boolean targetFound = false;
                    for (MethodNode targetMethod : classNode.methods) {
                        if (targetMethod != null && targetMethod.name.equals(targetMethodName)) {
                            targetFound = true;
                            FinalityLogger.debug("L2 Find Inject " + targetMethodName + " at position " + position);

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
                                default:
                                    FinalityLogger.warn("InjectProcessor: unknown position " + position + " for method " + method.name);
                                    break;
                            }
                        }
                    }

                    if (!targetFound) {
                        FinalityLogger.warn("InjectProcessor: target method " + targetMethodName + " not found for injection in class " + classNode.name);
                    }
                }
            }
        }
    }

    void injectHead(MethodNode sourceNode, MethodNode targetNode, boolean returnWithValue) {
        if (targetNode.instructions == null) {
            FinalityLogger.warn("InjectProcessor: target method " + targetNode.name + " has no instructions");
            return;
        }

        InsnList callback = createCallbackInsnList(sourceNode, targetNode, returnWithValue);
        targetNode.instructions.insert(callback);
    }

    void injectReturn(MethodNode sourceNode, MethodNode targetNode, boolean returnWithValue) {
        if (targetNode.instructions == null) {
            FinalityLogger.warn("InjectProcessor: target method " + targetNode.name + " has no instructions");
            return;
        }

        boolean returnFound = false;
        for (AbstractInsnNode insn : targetNode.instructions) {
            if (isReturnInstruction(insn)) {
                returnFound = true;
                InsnList callback = createCallbackInsnList(sourceNode, targetNode, returnWithValue);
                targetNode.instructions.insertBefore(insn, callback);
            }
        }

        if (!returnFound) {
            FinalityLogger.warn("InjectProcessor: no return instruction found in method " + targetNode.name);
        }
    }

    void injectBeforeInvoke(MethodNode sourceNode, MethodNode targetNode, String locator, boolean returnWithValue) {
        if (targetNode.instructions == null) {
            FinalityLogger.warn("InjectProcessor: target method " + targetNode.name + " has no instructions");
            return;
        }

        if (locator == null || locator.isEmpty()) {
            FinalityLogger.warn("Locator is empty for BEFORE_INVOKE injection in method " + sourceNode.name);
            return;
        }

        boolean invokeFound = false;
        for (AbstractInsnNode insn : targetNode.instructions) {
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                if (methodInsn.name.equals(locator)) {
                    invokeFound = true;
                    InsnList callback = createCallbackInsnList(sourceNode, targetNode, returnWithValue);
                    targetNode.instructions.insertBefore(insn, callback);
                }
            }
        }

        if (!invokeFound) {
            FinalityLogger.warn("InjectProcessor: no invoke instruction with locator '" + locator + "' found in method " + targetNode.name);
        }
    }

    void injectAfterInvoke(MethodNode sourceNode, MethodNode targetNode, String locator, boolean returnWithValue) {
        if (targetNode.instructions == null) {
            FinalityLogger.warn("InjectProcessor: target method " + targetNode.name + " has no instructions");
            return;
        }

        if (locator == null || locator.isEmpty()) {
            FinalityLogger.warn("Locator is empty for AFTER_INVOKE injection in method " + sourceNode.name);
            return;
        }

        boolean invokeFound = false;
        for (AbstractInsnNode insn : targetNode.instructions) {
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode methodInsn = (MethodInsnNode) insn;
                if (methodInsn.name.equals(locator)) {
                    invokeFound = true;
                    InsnList callback = createCallbackInsnList(sourceNode, targetNode, returnWithValue);
                    targetNode.instructions.insert(insn, callback);
                }
            }
        }

        if (!invokeFound) {
            FinalityLogger.warn("InjectProcessor: no invoke instruction with locator '" + locator + "' found in method " + targetNode.name);
        }
    }

    private InsnList createCallbackInsnList(MethodNode sourceNode, MethodNode targetNode, boolean returnWithValue) {
        InsnList insnList = new InsnList();
        insnList.add(new TypeInsnNode(NEW, "team/rainfall/finality/luminosity2/CallbackInfo"));
        insnList.add(new InsnNode(DUP));
        insnList.add(new MethodInsnNode(INVOKESPECIAL, "team/rainfall/finality/luminosity2/CallbackInfo", "<init>", "()V", false));
        insnList.add(new VarInsnNode(ASTORE, targetNode.maxLocals + 1));

        if (!MethodUtil.isStatic(sourceNode)) {
            insnList.add(new VarInsnNode(ALOAD, 0));
        }

        insnList.add(getLocalVarIndexOfParams(targetNode));
        insnList.add(new VarInsnNode(ALOAD, targetNode.maxLocals + 1));

        if (MethodUtil.isStatic(sourceNode)) {
            insnList.add(new MethodInsnNode(INVOKESTATIC, classNode.name, sourceNode.name, sourceNode.desc, false));
        } else {
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, classNode.name, sourceNode.name, sourceNode.desc, false));
        }

        if (returnWithValue) {
            // 检查 isCancelled 标志，只有当 isCancelled 为 true 时才劫持返回
            insnList.add(new VarInsnNode(ALOAD, targetNode.maxLocals + 1));
            insnList.add(new FieldInsnNode(GETFIELD, "team/rainfall/finality/luminosity2/CallbackInfo", "isCancelled", "Z"));
            
            // 创建标签用于条件跳转
            LabelNode continueLabel = new LabelNode();
            insnList.add(new JumpInsnNode(IFEQ, continueLabel)); // if isCancelled == false, jump to continue
            
            // isCancelled == true, 执行返回逻辑
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
            
            // isCancelled == false, 继续执行原方法
            insnList.add(continueLabel);
        }

        return insnList;
    }

    private boolean isReturnInstruction(AbstractInsnNode insn) {
        if (insn == null) return false;
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
        for (int i = 0; i < types.length; i++) {
            indexes.add(i);
        }
        for (Integer integer : indexes) {
            switch (types[integer].getDescriptor()) {
                case "I":
                case "Z":
                    insnList.add(new VarInsnNode(ILOAD, integer + flag));
                    break;
                case "J":
                    insnList.add(new VarInsnNode(LLOAD, integer + flag));
                    break;
                case "F":
                    insnList.add(new VarInsnNode(FLOAD, integer + flag));
                    break;
                case "D":
                    insnList.add(new VarInsnNode(DLOAD, integer + flag));
                    break;
                case "L":
                default:
                    insnList.add(new VarInsnNode(ALOAD, integer + flag));
                    break;
            }
        }
        return insnList;
    }

    public InjectProcessor(ClassNode classNode) {
        this.classNode = classNode;
    }
}
