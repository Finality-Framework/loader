package org.objectweb.asm;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @Description 字节码测试类
 * @Author Greyeon
 * @Date 2024/12/06 18:22
 */

class ClassWriterTest {

    @Test
    public void testClassWriterCreation() {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        assertNotNull(classWriter);
    }

    @Test
    public void testVisit() {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Test", null, "java/lang/Object", null);
        byte[] bytecode = classWriter.toByteArray();
        assertNotNull(bytecode);
    }

}
