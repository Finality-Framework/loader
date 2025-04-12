package team.rainfall.finality.luminosity2.annotations;

import team.rainfall.finality.luminosity2.utils.InjectPosition;

/**
 * Represents a method callback should be injected into the target method.
 * @since 1.4.0
 * @author RedreamR
 */
public @interface Inject {
    String methodName();
    InjectPosition position() default InjectPosition.HEAD;
    String locator() default "";
    boolean returnWithValue() default false;
}
