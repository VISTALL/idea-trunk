//test for inspection DuplicateCondition
if (foo())
{
    bar();
} else if (foo())
{
    bar();
} else if (bar())
{
    bar();
}else if (foo())
{
    bar();
}
