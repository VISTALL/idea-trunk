//test for inspection LoopStatementThatDoesntLoop
while (true)
{
    break;
}

function foo()
{
    do
    {
        return;
    }
    while (true);
}