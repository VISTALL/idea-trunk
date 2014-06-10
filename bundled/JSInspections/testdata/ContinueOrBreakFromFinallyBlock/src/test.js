//test for inspection ContinueOrBreakFromFinallyBlock
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

function bar()
{
    while (true)
    {
        try
        {
            throw 3;
        } finally
        {
            break;
        }
    }
}
