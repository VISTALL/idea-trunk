//test for inspection UnnecessaryLocalVariable
function foo()
{
    var x = 3;
    var y = x;
    print(y);

    var saveCounter = gCounter;
  
    (function () {
      if (saveCounter == gCounter) alert();
    })();
}
var gCounter
