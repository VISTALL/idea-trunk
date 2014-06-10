import javax.inject.Specializes;

public class SpecializedClazz {}

<error>@Specializes</error>
public class SpecializesClazz1 extends SpecializedClazz {
}

<error>@Specializes</error>
public class SpecializesClazz2 extends SpecializedClazz {
}


