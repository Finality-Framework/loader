package team.rainfall.finality.luminosity2.processor;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.luminosity2.Processor;
import team.rainfall.finality.luminosity2.utils.AnnotationUtil;
import team.rainfall.finality.luminosity2.utils.MethodUtil;

import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class FieldOpProcessor implements Processor {
    ClassNode sourceNode;
    ClassNode targetNode;

    @Override
    public void process() {
        for (MethodNode method : sourceNode.methods) {
            if (AnnotationUtil.annotationExists("Lteam/rainfall/finality/luminosity2/annotations/Getter;", method)) {
                String fieldName = (String) AnnotationUtil.getAnnotationValue("fieldName", AnnotationUtil.getAnnotation("Lteam/rainfall/finality/luminosity2/annotations/Getter;", method));
                if ((method.access & ACC_STATIC) != 0) {
                    try {
                        rewriteMethodToReturnField(method,targetNode,fieldName);
                    } catch (NoSuchFieldException e) {
                        FinalityLogger.warn("L2 Getter Method" + (sourceNode.name + "." + MethodUtil.getFullName(method)) + " inject failed:Field not found");
                    }
                } else {
                    FinalityLogger.warn("L2 Getter Method " + (sourceNode.name + "." + MethodUtil.getFullName(method)) + "is a virtual method.");
                }
            }
        }
    }

    public FieldOpProcessor(ClassNode sourceNode, ClassNode targetNode) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;

    }

    public static void rewriteMethodToReturnField(MethodNode methodNode, ClassNode targetClassNode, String fieldName) throws NoSuchFieldException {
        methodNode.instructions.clear();
        methodNode.localVariables = null;
        FieldNode field = targetClassNode.fields.stream()
                .filter(f -> f.name.equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new NoSuchFieldException("Field not found"));

        boolean isStatic = (field.access & Opcodes.ACC_STATIC) != 0;
        InsnList instructions = new InsnList();
        if (!isStatic) {
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        }
        instructions.add(new FieldInsnNode(isStatic ? Opcodes.GETSTATIC : Opcodes.GETFIELD, targetClassNode.name, fieldName, field.desc));
        instructions.add(new InsnNode(getReturnOpcode(methodNode.desc.charAt(0))));
        methodNode.instructions.add(instructions);
    }

    private static int getReturnOpcode(char typeChar) {
        switch (typeChar) {
            case 'V':
                return Opcodes.RETURN;
            case 'L':
            case '[':
                return Opcodes.ARETURN;
            case 'J':
                return Opcodes.LRETURN;
            case 'D':
                return Opcodes.DRETURN;
            case 'F':
                return Opcodes.FRETURN;
            default:
                return Opcodes.IRETURN;
        }
    }
}
