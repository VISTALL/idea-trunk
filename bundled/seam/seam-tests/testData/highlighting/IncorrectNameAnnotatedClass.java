import org.jboss.seam.annotations.Name;

<error>@Name("interface")</error>
public interface IncorrectInterface {
}

<error>@Name("abstractClass")</error>
public abstract class IncorrectAbstarctClass {
}

<error>@Name("noEmptyConstructor")</error>
public class NoEmptyConstructorClass {
  public NoEmptyConstructorClass(int a) {}
}
