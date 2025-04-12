package team.rainfall.finality.luminosity2.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation describes a class which mixin to the target class.
 * @see team.rainfall.finality.luminosity2.processor.MixinProcessor
 * @see team.rainfall.finality.luminosity2.processor.NewFieldProcessor
 * @author RedreamR
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Mixin {
    String mixinClass();
}
