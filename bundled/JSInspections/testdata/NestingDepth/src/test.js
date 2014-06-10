//test for inspection NestingDepth
function foo()
{
    if (true)
    {
        if (true)
        {
            if (true)
            {
                if (true)
                {
                    if (true)
                    {
                        if (true)
                        {
                            a = a + b;
                            return;
                        }
                        a = a + b;
                        return;
                    }
                    a = a + b;
                    return;
                }
                a = a + b;
                return;
            }
            a = a + b;
            return;
        }
    }
}
