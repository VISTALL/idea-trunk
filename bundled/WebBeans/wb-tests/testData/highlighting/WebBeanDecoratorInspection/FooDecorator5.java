import javax.decorator.Decorator;
import javax.decorator.Decorates;

@Decorator
public abstract class FooDecorator4 implements Account, java.util.List {

    @Decorates
    Account <error>account</error>;

    public void withdraw(String amount) {
        account.withdraw(amount);
        doLog(amount);
    }

    public void deposit(String amount) {
        account.deposit(amount);
        doLog(amount);
    }

    private void doLog(String amount) {
    }
}