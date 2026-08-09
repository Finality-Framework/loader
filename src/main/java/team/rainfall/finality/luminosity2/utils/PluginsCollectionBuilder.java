package team.rainfall.finality.luminosity2.utils;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import team.rainfall.finality.PluginMetadata;
import team.rainfall.finality.loader.plugin.PluginData;

import java.util.List;

/**
 * Generates the bytecode of {@code team.rainfall.finality.generated.PluginsCollection},
 * which contains a list of {@link PluginMetadata} of all loaded plugins.
 * @author RedreamR
 */
public class PluginsCollectionBuilder {
    public static final String CLASS_NAME = "team.rainfall.finality.generated.PluginsCollection";
    private static final String CLASS_INTERNAL_NAME = CLASS_NAME.replace('.', '/');
    private static final String PLUGIN_METADATA_INTERNAL_NAME = Type.getInternalName(PluginMetadata.class);

    public static ClassNode build(List<PluginData> pluginDataList) {
        ClassNode classNode = new ClassNode();
        classNode.version = Opcodes.V1_8;
        classNode.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER;
        classNode.name = CLASS_INTERNAL_NAME;
        classNode.superName = "java/lang/Object";

        FieldNode fieldNode = new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                "plugins", "Ljava/util/ArrayList;", null, null);
        classNode.fields.add(fieldNode);

        MethodNode clinit = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        InsnList insnList = new InsnList();
        insnList.add(new TypeInsnNode(Opcodes.NEW, "java/util/ArrayList"));
        insnList.add(new InsnNode(Opcodes.DUP));
        insnList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false));
        insnList.add(new FieldInsnNode(Opcodes.PUTSTATIC, CLASS_INTERNAL_NAME, "plugins", "Ljava/util/ArrayList;"));
        if (pluginDataList != null) {
            for (PluginData pluginData : pluginDataList) {
                if (pluginData.manifest == null) {
                    continue;
                }
                insnList.add(new FieldInsnNode(Opcodes.GETSTATIC, CLASS_INTERNAL_NAME, "plugins", "Ljava/util/ArrayList;"));
                insnList.add(new TypeInsnNode(Opcodes.NEW, PLUGIN_METADATA_INTERNAL_NAME));
                insnList.add(new InsnNode(Opcodes.DUP));
                pushString(insnList, pluginData.manifest.id);
                pushString(insnList, pluginData.manifest.name);
                pushString(insnList, pluginData.manifest.version);
                pushString(insnList, pluginData.manifest.mainClass);
                insnList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, PLUGIN_METADATA_INTERNAL_NAME, "<init>",
                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false));
                insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "add",
                        "(Ljava/lang/Object;)Z", false));
                insnList.add(new InsnNode(Opcodes.POP));
            }
        }
        insnList.add(new InsnNode(Opcodes.RETURN));
        clinit.instructions.add(insnList);
        classNode.methods.add(clinit);
        return classNode;
    }

    private static void pushString(InsnList insnList, String value) {
        if (value == null) {
            insnList.add(new InsnNode(Opcodes.ACONST_NULL));
        } else {
            insnList.add(new LdcInsnNode(value));
        }
    }

    private PluginsCollectionBuilder() {
    }
}
