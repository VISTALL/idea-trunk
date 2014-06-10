public class MyDomain {
  int intX;
  int intY;
  String s;
  boolean bool;

  public static void doSomething() {
    MyDomain.findAllByIntX(4);
    MyDomain.findAllByIntXOrIntY(4, 6);
    MyDomain.count();
    MyDomain.listOrderByS();
    MyDomain.listOrderById();
    MyDomain.<warning descr="Can not resolve symbol 'findAllByasdfsdfasd'">findAllByasdfsdfasd</warning>();
    MyDomain.<warning descr="Can not resolve symbol 'findAllByS'">findAllByS</warning><warning descr="'findAllByS' cannot be applied to '()'">()</warning>;
    MyDomain.<warning descr="Can not resolve symbol 'findAllByIntX'">findAllByIntX</warning><warning descr="'findAllByIntX' cannot be applied to '(java.lang.Integer, java.lang.Integer)'">(3, 4)</warning>;
    MyDomain.findAllByIntX(3);
    MyDomain.findAllByIntXAndS(3, "", [max: 3,
                           offset: 2,
                           sort: "s",
                           order: "desc"]);
  }
}