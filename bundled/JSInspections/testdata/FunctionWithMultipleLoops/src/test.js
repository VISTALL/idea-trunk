//test for inspection FunctionWithMultipleLoops
function foo()
{
    for(x in y)
    {
        function baz(){
            for(x in y)
            {

            }
        }
    }
    for(x in y)
    {

    }
}