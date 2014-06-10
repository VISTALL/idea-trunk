import javax.inject.*;

public class SpecializedClazz {
     @Produces
     static public int getMax() {return 0;}
}


public class SpecializesClazz extends SpecializedClazz {
     @Produces
     <error>@Specializes</error>
     static  public int getMax() {return 0;}
}

