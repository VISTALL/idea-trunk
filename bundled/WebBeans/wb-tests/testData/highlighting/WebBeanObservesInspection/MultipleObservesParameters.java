import javax.event.Observes;

public class MultipleObservesParameters {

   public void afterDocumentUpdate(@Observes Document d) {
   }

   public void <error>afterDocumentUpdate2</error>(@Observes Document d, @Observes String s) {
   }
}