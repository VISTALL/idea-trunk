import org.springframework.beans.factory.annotation.*;
public class FooRename3 {
    @Autowired
    public void inject(FooService2 inj2, @QualifierAnnotatedChild("f<caret>3") FooInjection inj) {};
}