package example;

import javax.ws.rs.Path;
import javax.ws.rs.GET;

@Path("/helloworld")
public class Aaa {
    @GET
    public <caret>void foo() {
        System.out.println("test");
    }
}