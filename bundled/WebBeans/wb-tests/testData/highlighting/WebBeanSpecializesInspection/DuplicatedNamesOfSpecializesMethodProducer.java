import javax.inject.*;
import javax.annotation.Named;

public class SpecializedClazz {
     @Produces
     @Named
     public int getMax() {return 0;}
}


public class SpecializesClazz extends SpecializedClazz {

     @Named
     @Produces
     <error>@Specializes</error>
     public int getMax() {return 0;}
}

