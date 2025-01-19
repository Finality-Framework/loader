package team.rainfall.luminosity.transformers;

import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class MixinTransformer {
    public static void transform(ClassNode sourceNode,ClassNode targetNode){
        Remapper remapper = new SimpleRemapper(sourceNode.name,targetNode.name);
        ClassNode classNode = new ClassNode();
        sourceNode.accept(new ClassRemapper(classNode,remapper));

    }
}
