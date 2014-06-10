function foo()
{
    while (true)
        try
        {
            throw 3
        } finally
        {
            break;
        }
    if(true) x = 3;
    while(true){ x = 3; if(true) break; }
}