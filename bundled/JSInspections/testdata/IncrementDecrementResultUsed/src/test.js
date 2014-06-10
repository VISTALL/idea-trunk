//test for inspection IncrementDecrementResultUsed
if (a++)
{
    a = b;
}
if (a--)
{
    a = b;
}
if (++a)
{
    a = b;
}
if (--a)
{
    a = b;
    --a;
}