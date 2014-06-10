import javax.webbeans.Model;

@Model
public class User {
  private String userName;
  private String password;

  public User() {}

  public String getUserName() {return userName;}

  public void setUserName(String userName) {this.userName = userName;}
}
