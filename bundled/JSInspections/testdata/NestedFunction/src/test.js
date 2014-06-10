//test for inspection NestedFunction
function foo()
{
    function bar()
    {

    }

    x = function(){
        bar();
    };
}