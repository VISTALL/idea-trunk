import javax.decorator.Decorator;
import javax.decorator.Decorates;

<error>@Decorator</error>
public abstract class FooDecorator3 implements Account {

    @Decorates
    Account account;

    @Decorates
    Account account2;

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