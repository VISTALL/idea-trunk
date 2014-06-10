import javax.event.Observes;
import javax.inject.Produces;

public class ProducesWithObservesParameter {

   @Produces public Document <error>produces</error>(@Observes Document d) {
      return null;
   }
}