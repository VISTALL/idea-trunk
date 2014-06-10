import javax.inject.Initializer;

public class TwoInitializerConstructors {

   <error>@Initializer</error>
   public TwoInitializerConstructors() {}

   <error>@Initializer</error>
   public TwoInitializerConstructors(int a) {}
}