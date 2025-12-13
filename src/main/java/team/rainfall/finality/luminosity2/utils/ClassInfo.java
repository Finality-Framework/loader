package team.rainfall.finality.luminosity2.utils;

import org.objectweb.asm.tree.ClassNode;

public class ClassInfo {
    public ClassNode node = null;
    public String name;
    public byte[] bytes;
    public ClassInfo(ClassNode node, String name) {
        this.node = node;
        this.name = name;
    }
    public ClassInfo(){

    }
}
