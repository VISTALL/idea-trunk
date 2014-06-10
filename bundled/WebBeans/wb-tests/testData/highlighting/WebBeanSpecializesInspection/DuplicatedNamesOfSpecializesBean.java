import javax.inject.*;
import javax.annotation.Named;

@Named
public class SpecializedClazz {}

@Named
<error>@Specializes</error>
public class SpecializesClazz extends SpecializedClazz {
}

