package beans;

import javax.annotation.Named;
import javax.webbeans.Model;
import javax.inject.Current;
import javax.inject.Produces;

import org.apache.webbeans.sample.bindings.LoggedInUser;

@RequestScoped
@Named
public class LoginBean {
   @Model @Current private String fieldModel;

   @Produces @Current @Named(value= "currentUser")
   public User getLoggedInUser(){return null;}

    @Produces @Current @Model
    public User getProducedUser(){return null;}
}