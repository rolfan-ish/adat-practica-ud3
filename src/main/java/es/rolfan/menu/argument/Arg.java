package es.rolfan.menu.argument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Arg {
    String value() default "";
    Class<? extends ArgGetterFactory> getter() default DefaultArgGetterFactory.class;
}
