package mypackage;

public interface IJaxRPCWsWithWsdl {
  void doIt();
}
public class JaxRPCWsWithWsdl implements IJaxRPCWsWithWsdl {
  void <error>doIt</error>() {}
}