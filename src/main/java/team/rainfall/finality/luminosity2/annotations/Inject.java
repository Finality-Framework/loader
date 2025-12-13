package team.rainfall.finality.luminosity2.annotations;

import team.rainfall.finality.luminosity2.utils.InjectPosition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a method callback should be injected into the target method.
 * @since 1.4.0
 * @author RedreamR
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
    String methodName();
    InjectPosition position() default InjectPosition.HEAD;
    String locator() default "";
    boolean returnWithValue() default false;
}
