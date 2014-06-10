import javax.event.Observes;
import javax.inject.Disposes;

public class DisposeObservesParameters {

   public void <error>afterDocumentUpdate2</error>(@Observes Document d, @Disposes Document s) {
   }
}