package pkg;

import java.util.*;
import java.io.Serializable;
import org.hibernate.*;
import javax.persistence.*;
import org.intellij.lang.annotations.Language;

@Entity
class <error>NoArgConstructor</error> {
  NoArgConstructor(int i) { }

}

@MappedSuperclass
@Table(name="\"user\"")
public class UserBase {
  @Basic
  int i;

  int id;
}

public class UserBaseEx extends UserBase {

}

@NamedQueries({
@NamedQuery(name="q1", query="from User"),
@NamedQuery(name="qOrderBy", query="from User order by i"),
@NamedQuery(name="qOrderBy", query="from FubarEntity order by id"),
@NamedQuery(name="qOrderBy2", query="from User order by fellow.i"),
@NamedQuery(name="qOrderBy3", query="from User u order by u.fellow.i"),
@NamedQuery(name="q1", query="from <error descr="Class isn't an entity">java.lang.String</error>"),
@NamedQuery(name="q1error", query="from <error>BadBadEntity</error>"),
@NamedQuery(name="q3", query="from FubarEntity"),
@NamedQuery(name="q4", query="from Fubar"),
@NamedQuery(name="q2", query="from User u where u.i+" + "u.j+u.<error>unknown</error> < :max"),
@NamedQuery(name="myQuery", query=User.QUERY_STRING)
})
@Table(name="\"<error>wrongTable</error>\"")
public class User extends UserBaseEx{

  public static final String QUERY_STRING = "from User u where" + " u.i+u.j = :param1 + :param2 + u.<error>badValue</error>";

  @Basic
  int j;

  @OneToOne
  User fellow;
}

public class SubUser1 extends User {
}

public class SubUser2 extends User {
}

public class SubSubUser1 extends SubUser1 {
}

public class NotSubUser {
}




public class Main {

  private static void listHoney(Session session) {
          Transaction tx = null;
          try {
                  tx = session.beginTransaction();
                  java.util.List honeys = session.createQuery("from <error>OMG</error>").list();
                  final String queryString = "from <error>OMG</error>";
                  honeys = session.createQuery(queryString).list();
                  tx.commit();
          } catch (HibernateException e) {
                  e.printStackTrace();
                  if (tx != null && tx.isActive())
                          tx.rollback();

          }
  }
  private static void func1(Session session) throws HibernateException {
      final String queryString;
      queryString = "from <error>OMG</error>";
      org.hibernate.Query q1 = session.createQuery(queryString);
  }
  
  private static void func2(Session session) throws HibernateException {
      org.hibernate.Query q1 = session.getNamedQuery("myQuery");
      q1.setParameter("param1", 1);
      q1.setParameter("param2", 2);
      q1.setParameter("<error>badParam</error>", null);
      q1 = session.getNamedQuery("pkg.User.hbmQuery");
      q1.setParameter("hbmParam", 1);
  }

  private static void func3(Session session) throws HibernateException {
      org.hibernate.Query q1 = session.getNamedQuery("myQuery")
        .setParameter("param1", 1)
        .setParameter("param2", 2)
        .setParameter("<error>badParam</error>", null);
  }

  private static void func4(Session session) throws HibernateException {
      org.hibernate.Query q1 = session.createQuery(User.QUERY_STRING)
        .setParameter("param1", 1)
        .setParameter("<error>badParam</error>", null)
        .setParameter("param2", 2);
  }

  private static void func5(Session session) throws HibernateException {
      org.hibernate.Query q1 = session.createQuery(User.QUERY_STRING);
      q1.setParameter("param1", 1)
        .setParameter("<error>badParam</error>", null)
        .setParameter("param2", 2)
        .setParameter(3, null);
  }

  private static void func7(Session session) throws HibernateException {
      org.hibernate.SQLQuery q1 = session.createSQLQuery("select<error> </error><error>select</error>");
      q1.setInteger("param1", 1); // should not be highlighted
  }
}

@Entity
public class Honey {
        @Basic
	private Integer id;
	private String name;
	private String taste;
}

@Entity(name="FubarEntity")
public class Fubar {
  @Id
  int a;
}

public class IDEADEV_18929_A implements Serializable {

    private boolean enabled;
    private Set assignedBees;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Basic // in order to init anno mappings and test setAssignedBees attribute merging
    public boolean getEnabled() {
        return enabled;
    }

    public void setAssignedBees(Set assignedBees) {
        this.assignedBees = assignedBees;
    }

    public Set getAssignedBees() {
        return assignedBees;
    }
}

public class IDEADEV_18929_B implements Serializable {

}

public class IDEADEV_22684 {
  private IDEADEV_22684_C c;
  public IDEADEV_22684_C getC() {return c;}
  public void setC(IDEADEV_22684_C c) {this.c = c;}
}

class IDEADEV_22684_C {
  private String p;
  public String getP() {return p;}
  public void setP(String p) {this.p = p;}
}

class IDEADEV_22810 {
  private Set<String> set;
}

class IDEADEV_24493 {
  IDEADEV_24493 c1;
  IDEADEV_24493 <error>c2</error>;
}

@Entity
public class IDEADEV_24825 {
    @OneToMany(mappedBy = "parent")
    public SortedSet<IDEADEV_24825> subs;

    @ManyToOne(optional = false)
    private IDEADEV_24825 parent;

}

@Entity
public class IDEADEV_25801 {
    @Basic
    @Version
    long <error>multipleMapping</error>;

    @Basic
    @Lob
    byte[] lob;

    @Basic
    @Temporal(TemporalType.DATE)
    Date checkinDate;
}

public class IntelliLangInjectionTest {
  void f(@Language("HQL") String param) { }
  void g() {
    f("from User order by i");
    f("from <error>BadBadEntity</error>");
    String query = "from User order by i";
    String badQuery = "from <error>BadBadEntity</error>";
    f(query);
    f(badQuery);
  }
}

@Entity
@NamedQuery(name = "CompositeIdWithIdAndNotEmbeddedId", query = "from CompositeIdWithIdAndNotEmbeddedIdEntity where embeddedId.id = :param")
public class CompositeIdWithIdAndNotEmbeddedIdEntity {
  @Id
  CompositeIdWithIdAndNotEmbeddedId embeddedId;
}

@Embeddable
public class CompositeIdWithIdAndNotEmbeddedId {
 @Basic
 int id;
}
