//test for inspection AssignmentToForLoopParameter
for (var i = 0; i < 3; i++)
{
    var j;
    i = 4;
    i += 4;
    i++;
    i--;
    j++;
}

for (var j in x)
{
    j++;
    j = 4;
    j--;
    --j;
    j += 4;
    i++;
}
