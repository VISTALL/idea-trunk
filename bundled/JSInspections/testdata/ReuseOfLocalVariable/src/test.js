//test for inspection ReuseOfLocalVariable
function foobar()
{
    var x = 3;
    for(  i = 0;i<4;i++)
    {
        x = 4;
       print(x);
    }
    x = 4;
    print(x);
}
