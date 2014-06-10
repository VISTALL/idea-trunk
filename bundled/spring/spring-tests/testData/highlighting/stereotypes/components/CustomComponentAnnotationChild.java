package components;

import org.springframework.stereotype.Component;

@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Inherited
@java.lang.annotation.Documented

@CustomComponentAnnotation
public @interface CustomComponentAnnotationChild {
    java.lang.String value() default "";
}