package beans;

import javax.context.RequestScoped;
import javax.annotation.Stereotype;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

@Stereotype
@RequestScoped
@Target( { TYPE, METHOD, FIELD })
@Retention(RUNTIME)
@Documented
public @interface RequestScopedStereotype {
}