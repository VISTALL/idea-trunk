import javax.inject.*;

public class SpecializedClazz {
     @Produces
     public int getMax() {return 0;}
}


public class SpecializesClazz extends SpecializedClazz {
     
     <error>@Specializes</error>
     public int getMax() {return 0;}
}

