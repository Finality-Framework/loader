package team.rainfall.finality.luminosity2.processor;

import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import team.rainfall.finality.FinalityLogger;
import team.rainfall.finality.luminosity2.Processor;
import team.rainfall.finality.luminosity2.utils.AnnotationUtil;
import team.rainfall.finality.luminosity2.utils.MethodUtil;

import java.util.ArrayList;
import java.util.Iterator;

public class MapBackProcessor implements Processor {
    ClassNode sourceNode;
    String name;
    public ClassNode destNode;
    public void process(){
        FinalityLogger.debug("MapBack Process S "+sourceNode.name+" T "+name);
        Remapper remapper = new SimpleRemapper(sourceNode.name,name);
        sourceNode.accept(new ClassRemapper(destNode,remapper));
    }

    public MapBackProcessor(ClassNode sourceNode , String name){
        this.sourceNode = sourceNode;
        this.name = name;
        destNode = new ClassNode();
    }
}
