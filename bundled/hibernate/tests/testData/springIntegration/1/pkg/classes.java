package pkg;

import javax.persistence.*;
import org.hibernate.*;


@MappedSuperclass
@Table(name="\"user\"")
public class UserBase {
  @Basic
  int i;

}

public class UserBaseEx extends UserBase {

}

@NamedQueries({
@NamedQuery(name="q1", query="from User u where u.i = :ival"),
@NamedQuery(name="q1error", query="from <error>BadBadEntity</error>"),
@NamedQuery(name="q2", query="from User u where u.i+u.j+u.<error>unknown</error> < :max")
})
@Table(name="\"<error>wrongTable</error>\"")
public class User extends UserBaseEx{
  @Basic
  int j;
}

public class Main {

  private static void listHoney(Session session) {
          Transaction tx = null;
          try {
                  tx = session.beginTransaction();
                  java.util.List honeys = session.createQuery("from <error>OMG</error>").list();
                  tx.commit();
          } catch (HibernateException e) {
                  e.printStackTrace();
                  if (tx != null && tx.isActive())
                          tx.rollback();

          }
  }

  public void func(org.springframework.orm.jpa.JpaTemplate ops) {
    ops.findByNamedQuery("q1");
    ops.findByNamedQuery("<error>missingQ</error>");
    ops.find("from User");
    ops.find("from <error>OMG</error>");
  }

  public void func(org.springframework.orm.hibernate3.HibernateTemplate ops) {
    ops.findByNamedQuery("q1");
    ops.findByNamedQuery("<error>missingQ</error>");
    ops.find("from User");
    ops.find("from <error>OMG</error>");

    ops.findByNamedParam("from User u where u.i = :para", "para", "10");
    ops.findByNamedParam("from User u where u.i = :para", "<error>missingParam</error>", "10");
    ops.findByNamedQueryAndNamedParam("q1", "ival", "10");
    ops.findByNamedQueryAndNamedParam("q1", "<error>missingParam</error>", "10");
  }
}

public class Honey {
	private Integer id;
	private String name;
	private String taste;
}