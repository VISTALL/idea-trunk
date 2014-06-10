import javax.inject.*;

public class SpecializedClazz {
     @Produces
     public int getMax() {return 0;}
}


public class SpecializesClazz1 extends SpecializedClazz {
     @Produces
     <error>@Specializes</error>
     public int getMax() {return 0;}
}


public class SpecializesClazz2 extends SpecializedClazz {
     @Produces
     <error>@Specializes</error>
     public int getMax() {return 0;}
}


