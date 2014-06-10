//test for inspection UnreachableCode
const int = 3;
{
    for (i in x)
    {
        continue;
    }
    while (true)
    {

    }
    bar();
}
