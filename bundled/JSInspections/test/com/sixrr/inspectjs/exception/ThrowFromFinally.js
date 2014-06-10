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