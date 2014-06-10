function foo()
{
    while (true)
    {
        try
        {
            throw 3;
        } finally
        {
            continue;
        }
    }
}