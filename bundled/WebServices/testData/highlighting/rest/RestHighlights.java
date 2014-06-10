package rest;

import javax.ws.rs.*;

@Path("/")
public class Hello {
  @GET
  @Produces(<error>"wrong/mime type"</error>)
  @Consumes(<error>"another one/wrong mime"</error>)
  //Method annotated with @GET must return non-void value
  public <error>void</error> sayHello() {
  }

  @GET
  @Produces("correct/mime+type")
  @Consumes("and+this/correct-too")
  public String sayHello2() {
    return "Hello";
  }

  public static class Foo {
    @GET
    public String sayFoo() {
      return "foo";
    }
  }

  // Same @Path's value in different classes must be highlighted
  <warning>@Path("/hello")</warning>
  public static class Bar {
    @GET public String bar() {return "";}
  }
  <warning>@Path("/hello")</warning>
  public static class Bar2 {
    @GET public String bar() {return "";}
  }

  @Path("/A")
  public static class A {}

  public static class B extends A {
    @GET public String foo() {return "";}
  }
}