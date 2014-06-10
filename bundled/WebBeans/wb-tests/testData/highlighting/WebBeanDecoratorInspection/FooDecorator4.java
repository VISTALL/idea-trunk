import javax.decorator.Decorator;
import javax.decorator.Decorates;

@Decorator
public abstract class FooDecorator4 implements Account {

    @Decorates
    String <error>account</error>;

    public void withdraw(String amount) {
    }

    public void deposit(String amount) {
    }

    private void doLog(String amount) {
    }
}