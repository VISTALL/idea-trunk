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