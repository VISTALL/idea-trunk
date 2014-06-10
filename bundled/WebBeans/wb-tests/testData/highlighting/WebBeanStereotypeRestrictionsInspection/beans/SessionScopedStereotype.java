package beans;

import javax.annotation.Stereotype;
import javax.context.SessionScoped;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

@Stereotype
@SessionScoped
@Target( { TYPE, METHOD, FIELD })
@Retention(RUNTIME)
@Documented
public @interface SessionScopedStereotype {
}