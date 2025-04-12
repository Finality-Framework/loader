package team.rainfall.finality.luminosity2.processor;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import team.rainfall.finality.luminosity2.Processor;

/** If a new field is added to the mixin class,this processor will inject the field into the target class.
 * @since 1.3.2
 * @author RedreamR
*/
public class NewFieldProcessor implements Processor {
    ClassNode sourceNode;
    ClassNode targetNode;

    @Override
    public void process() {
        for (FieldNode field : sourceNode.fields) {
            String name = getFullName(field);
            if (targetNode.fields.stream().noneMatch(f -> getFullName(f).equals(name))) targetNode.fields.add(field);
        }
    }

    public static String getFullName(FieldNode fieldNode){
        return fieldNode.name+fieldNode.desc;
    }

    public NewFieldProcessor(ClassNode sourceNode, ClassNode targetNode) {
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
    }
}
