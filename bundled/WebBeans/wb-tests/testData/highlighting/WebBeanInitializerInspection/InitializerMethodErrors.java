import javax.inject.*;
import javax.event.*;

public class InitializerMethodErrors {

   @Initializer
   public static void <error>method</error>() {}

   @Initializer
   @Produces
   public void <error>method2</error>() {}

  @Initializer
   public void <error>method4</error>(@Disposes String str) {}

  @Initializer
   public void <error>method5</error>(@Observes String str) {}
}