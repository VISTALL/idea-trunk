const int = 3;
foo:
for (i in x)
{
    with (x) {
        continue foo;
    }
}