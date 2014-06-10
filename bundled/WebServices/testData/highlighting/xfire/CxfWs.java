package xfire;

public class InputClass {}
public class OutputClass {}

public class <warning>GreeterImpl</warning> {
    public InputClass <weak_warning>greetMe</weak_warning>(OutputClass me) {
        return new InputClass();
    }
}
