import javax.decorator.Decorator;
import javax.decorator.Decorates;

@Decorator
public abstract class FooDecorator implements Account {

    @Decorates
    Account account;

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