package beans;

import javax.inject.Produces;
import javax.context.SessionScoped;

<error>@SessionScoped</error>
@StereotypeWithSupportedScopes
public class UnsupportedScopesWebBean  {

   @Produces
   <error>@SessionScoped</error>
   @StereotypeWithSupportedScopes
   public String producesMethod(){
     return null;
   }
}