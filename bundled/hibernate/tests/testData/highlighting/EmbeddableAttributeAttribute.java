package bar;

import javax.persistence.*;
import java.util.*;

class User {
   @Embedded
   Embxed embedded;

   @Basic Embxed smth;

   Embxable embeddable;
}

class Embxed {
   @Basic Date date;
}

@Embeddable
class Embxable {
   @Basic Date date;
}