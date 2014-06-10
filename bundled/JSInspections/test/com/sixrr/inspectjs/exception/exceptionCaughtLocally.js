try
{
    try
    {
        throw e;
    }
    finally
    {
        a = 3;
    }
}
catch(e)
{
    a = 3;
}
finally
{
    a = 5;
}