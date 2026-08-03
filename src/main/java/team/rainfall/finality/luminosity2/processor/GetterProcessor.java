package team.rainfall.finality.luminosity2.processor;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.loader.util.Localization;
import team.rainfall.finality.luminosity2.Processor;
import team.rainfall.finality.luminosity2.annotations.Getter;
import team.rainfall.finality.luminosity2.utils.AnnotationUtil;
import team.rainfall.finality.luminosity2.utils.MethodUtil;

import java.util.Objects;

import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class GetterProcessor implements Processor {
    ClassNode sourceNode;
    ClassNode targetNode;

    @Override
    public void process() {
        for (MethodNode method : sourceNode.methods) {
            if (AnnotationUtil.annotationExists(Getter.class, method)) {
                String fieldName = (String) AnnotationUtil.getAnnotationValue("fieldName", Objects.requireNonNull(AnnotationUtil.getAnnotation(Getter.class, method)));
                if ((method.access & ACC_STATIC) == 0) {
                    FinalityLogger.warn(String.format(Localization.bundle.getString("luminosity2_error"), "Getter Method " + (sourceNode.name + "." + MethodUtil.getFullName(method)) + " is not static."));
                    continue;
                }

                FieldNode field = targetNode.fields.stream()
                        .filter(f -> f.name.equals(fieldName))
                        .findFirst()
                        .orElse(null);
                if (field == null) {
                    FinalityLogger.warn(String.format(Localization.bundle.getString("luminosity2_error"), "Getter Method " + (sourceNode.name + "." + MethodUtil.getFullName(method)) + " inject failed: field '" + fieldName + "' not found"));
                    continue;
                }

                Type[] argTypes = Type.getMethodType(method.desc).getArgumentTypes();
                Type returnType = Type.getReturnType(method.desc);
                boolean isFieldStatic = (field.access & Opcodes.ACC_STATIC) != 0;

                if (isFieldStatic) {
                    if (argTypes.length != 0) {
                        FinalityLogger.warn(String.format(Localization.bundle.getString("luminosity2_error"), "Getter Method " + (sourceNode.name + "." + MethodUtil.getFullName(method)) + " for static field '" + fieldName + "' should have 0 parameters, got " + argTypes.length));
                        continue;
                    }
                } else {
                    if (argTypes.length != 1) {
                        FinalityLogger.warn(String.format(Localization.bundle.getString("luminosity2_error"), "Getter Method " + (sourceNode.name + "." + MethodUtil.getFullName(method)) + " for instance field '" + fieldName + "' should have 1 parameter (target instance), got " + argTypes.length));
                        continue;
                    }
                    if (!argTypes[0].getInternalName().equals(targetNode.name)) {
                        FinalityLogger.warn(String.format(Localization.bundle.getString("luminosity2_error"), "Getter Method " + (sourceNode.name + "." + MethodUtil.getFullName(method)) + " first parameter type '" + argTypes[0].getInternalName() + "' does not match target class '" + targetNode.name + "'"));
                        continue;
                    }
                }

                if (!returnType.getDescriptor().equals(field.desc)) {
                    FinalityLogger.warn(String.format(Localization.bundle.getString("luminosity2_error"), "Getter Method " + (sourceNode.name + "." + MethodUtil.getFullName(method)) + " return type '" + returnType.getDescriptor() + "' does not match field type '" + field.desc + "'"));
                    continue;
                }

                try {
                    rewriteMethodToReturnField(method, targetNode, fieldName);
                } catch (NoSuchFieldException e) {
                    FinalityLogger.warn(String.format(Localization.bundle.getString("luminosity2_error"), "Getter Method " + (sourceNode.name + "." + MethodUtil.getFullName(method)) + " inject failed: field not found"));
                }
            }
        }
    }

    public GetterProcessor(ClassNode sourceNode, ClassNode targetNode) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;

    }

    /**
     * Rewrite the method to return the field value.<br>
     * If the field is virtual,method should have a parameter which is an instance of target class.<br>
     * e.g. int foo(TargetClass clz)<br>
     * No parameter is needed when the field is static.<br>
     * @param methodNode represents getter method
     * @param targetClassNode the class which included the field
     * @param fieldName field name
     * @throws NoSuchFieldException Occurred when the field doesn't exist
     */
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
            //Load an instance of target class from PARAMETER.
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        }
        instructions.add(new FieldInsnNode(isStatic ? Opcodes.GETSTATIC : Opcodes.GETFIELD, targetClassNode.name, fieldName, field.desc));
        instructions.add(new InsnNode(getReturnOpcode(Type.getReturnType(methodNode.desc).getDescriptor().charAt(0))));
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
