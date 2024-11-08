package team.rainfall.luminosity.transformers;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class AccessTransformer {
    public static void transformMethods(MethodNode targetNode) {
        int access = targetNode.access;

        // 清除public以外的访问级别标志（ACC_PRIVATE, ACC_PROTECTED, ACC_PUBLIC）
        // 这些标志分别对应二进制的 0x0002, 0x0004, 0x0001
        access &= ~(0x0002 | 0x0004); // 清除private和protected标志

        // 添加public标志
        access |= 0x0001; // ACC_PUBLIC

        // 更新ClassNode的访问标志
        targetNode.access = access;
    }
    public static void transform(ClassNode targetNode) {
        int access = targetNode.access;

        // 清除public以外的访问级别标志（ACC_PRIVATE, ACC_PROTECTED, ACC_PUBLIC）
        // 这些标志分别对应二进制的 0x0002, 0x0004, 0x0001
        access &= ~(0x0002 | 0x0004); // 清除private和protected标志

        // 添加public标志
        access |= 0x0001; // ACC_PUBLIC

        // 更新ClassNode的访问标志
        targetNode.access = access;
        for(MethodNode methodNode :targetNode.methods){
            transformMethods(methodNode);
        }
        for(FieldNode fieldNode :targetNode.fields){
            transformFields(fieldNode);
        }
    }
    public static void transformFields(FieldNode targetNode) {
        int access = targetNode.access;

        // 清除public以外的访问级别标志（ACC_PRIVATE, ACC_PROTECTED, ACC_PUBLIC）
        // 这些标志分别对应二进制的 0x0002, 0x0004, 0x0001
        access &= ~(0x0002 | 0x0004); // 清除private和protected标志

        // 添加public标志
        access |= 0x0001; // ACC_PUBLIC

        // 更新ClassNode的访问标志
        targetNode.access = access;
    }
}
