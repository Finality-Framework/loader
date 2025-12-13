package team.rainfall.finality.luminosity2.processor;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.luminosity2.Processor;
import team.rainfall.finality.luminosity2.utils.AnnotationUtil;
import team.rainfall.finality.luminosity2.utils.MethodUtil;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class SetterProcessor implements Processor {
    ClassNode sourceNode;
    ClassNode targetNode;

    @Override
    public void process() {
        for (MethodNode method : sourceNode.methods) {
            if (AnnotationUtil.annotationExists("Lteam/rainfall/finality/luminosity2/annotations/Setter;", method)) {
                String fieldName = (String) AnnotationUtil.getAnnotationValue("fieldName", Objects.requireNonNull(AnnotationUtil.getAnnotation("Lteam/rainfall/finality/luminosity2/annotations/Setter;", method)));
                if ((method.access & ACC_STATIC) != 0) {
                    try {
                        rewriteMethodToSetField(method, targetNode, fieldName);
                    } catch (NoSuchFieldException e) {
                        FinalityLogger.warn("L2 Setter Method " + (sourceNode.name + "." + MethodUtil.getFullName(method)) + " inject failed: Field not found");
                    } catch (IllegalArgumentException e) {
                        FinalityLogger.warn("L2 Setter Method " + (sourceNode.name + "." + MethodUtil.getFullName(method)) + " inject failed: " + e.getMessage());
                    }
                } else {
                    FinalityLogger.warn("L2 Setter Method " + (sourceNode.name + "." + MethodUtil.getFullName(method)) + " is a virtual method.");
                }
            }
        }
    }

    public SetterProcessor(ClassNode sourceNode, ClassNode targetNode) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
    }

    /**
     * Rewrite the method to set the field value.<br>
     * If the field is virtual (non-static), the method should have two parameters:
     * the first is an instance of the target class, and the second is the value to set.<br>
     * e.g. static void foo(TargetClass clz, int value)<br>
     * If the field is static, the method should have only one parameter: the value to set.<br>
     *
     * @param methodNode represents the setter method
     * @param targetClassNode the class which includes the field
     * @param fieldName field name
     * @throws NoSuchFieldException Occurred when the field doesn't exist
     * @throws IllegalArgumentException Occurred when the method signature is invalid
     */
    public static void rewriteMethodToSetField(MethodNode methodNode, ClassNode targetClassNode, String fieldName) throws NoSuchFieldException, IllegalArgumentException {
        methodNode.instructions.clear();
        methodNode.localVariables = null;

        FieldNode field = targetClassNode.fields.stream()
                .filter(f -> f.name.equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new NoSuchFieldException("Field '" + fieldName + "' not found in class " + targetClassNode.name));
        boolean isFieldStatic = (field.access & Opcodes.ACC_STATIC) != 0;
        InsnList instructions = new InsnList();
        if (!isFieldStatic) {
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        }
        int valueParamIndex = isFieldStatic ? 0 : 1;
        instructions.add(new VarInsnNode(getLoadOpcode(field.desc.charAt(0)), valueParamIndex));
        instructions.add(new FieldInsnNode(
                isFieldStatic ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD,
                targetClassNode.name,
                fieldName,
                field.desc
        ));
        instructions.add(new InsnNode(Opcodes.RETURN));

        methodNode.instructions.add(instructions);
    }

    private static int getLoadOpcode(char typeChar) {
        switch (typeChar) {
            case 'L':
            case '[':
                return Opcodes.ALOAD;
            case 'J':
                return Opcodes.LLOAD;
            case 'D':
                return Opcodes.DLOAD;
            case 'F':
                return Opcodes.FLOAD;
            default: // I, Z, S, C, B
                return Opcodes.ILOAD;
        }
    }

}
