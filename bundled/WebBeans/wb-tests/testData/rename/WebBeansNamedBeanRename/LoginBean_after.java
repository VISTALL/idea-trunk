import javax.annotation.Named;
import javax.inject.Current;
import javax.inject.Produces;

import org.apache.webbeans.sample.bindings.LoggedInUser;

@RequestScoped
@Named
public class LoginBean {
   @Produces @Current @Named(value= "currentUser_new")
   public User getLoggedInUser(){return null;}
}