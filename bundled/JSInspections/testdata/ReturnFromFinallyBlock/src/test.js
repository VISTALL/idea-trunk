//test for inspection ReturnFromFinallyBlock
function foo()
{
    try
    {
        throw 3;
    } finally
    {
        return;
    }
}