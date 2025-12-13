package team.rainfall.finality.luminosity2.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Get the value of a field.<br>
 * This annotation should only be used in a Mixin Class.<br>
 * @see team.rainfall.finality.luminosity2.processor.GetterProcessor
 * @author RedreamR
 * @since 1.4.5
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Getter {
    String fieldName();
}
