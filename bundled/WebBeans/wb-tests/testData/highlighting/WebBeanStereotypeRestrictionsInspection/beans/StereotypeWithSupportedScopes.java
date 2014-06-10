package beans;

import javax.annotation.Stereotype;
import javax.context.RequestScoped;
import javax.context.ApplicationScoped;
import javax.inject.Production;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;

@Stereotype(supportedScopes = {RequestScoped.class, ApplicationScoped.class})
@Target( {TYPE, METHOD, FIELD})
@Retention(RUNTIME)
@Documented
public @interface StereotypeWithSupportedScopes {
}
