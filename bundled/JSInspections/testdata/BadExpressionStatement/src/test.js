//test for inspection BadExpressionStatement
 1 + 2;
 i++;
 debugger;
 x && x();
result += source, source = '';
var o = { a:1, b:2 };
delete o.a;

(function(){
  alert();
}());

var x;
function foo() {}
x ? foo(1):foo(2);

x || foo();