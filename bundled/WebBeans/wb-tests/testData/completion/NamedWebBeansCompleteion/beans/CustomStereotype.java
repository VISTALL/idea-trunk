package beans;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.annotation.Named;
import javax.annotation.Stereotype;

@Named
@Stereotype
@Target( { TYPE, METHOD, FIELD })
@Retention(RUNTIME)
@Documented
public @interface CustomStereotype { }
