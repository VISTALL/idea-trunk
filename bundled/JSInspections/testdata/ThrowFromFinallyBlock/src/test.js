//test for inspection ThrowFromFinallyBlock
function foo()
{
    try
    {
        throw 3;
    } finally
    {
        throw 4;
    }
}
