//test for inspection UnnecessaryLabelOnContinueStatement
label:
for(var x = 1;x<100;x++)
{
    continue label;
}
