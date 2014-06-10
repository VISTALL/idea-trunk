package example;

import javax.ws.rs.Path;
import javax.ws.rs.GET;

@Path("/helloworld")
public class Aaa {
    @GET
    public String foo() {
        System.out.println("test");
        return null; //TODO replace this stub to something useful
    }
}