import javax.event.Observes;
import javax.inject.Initializer;

public class InitializerWithObservesParameter {

   @Initializer public Document <error>initMethod</error>(@Observes Document d) {
     return null;
   }
}