package team.rainfall.finality.luminosity2.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation describes a method should not be replaced by MixinProcessor.
 * @see team.rainfall.finality.luminosity2.processor.MixinProcessor
 * @author RedreamR
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
public @interface Shadow {
}
