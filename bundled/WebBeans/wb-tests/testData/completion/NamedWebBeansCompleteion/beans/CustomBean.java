package beans;

@CustomStereotype
public class CustomBean {

   @Produces @CustomStereotype @Current
   public User getProducesCustomStereotype(){return null;}

}
