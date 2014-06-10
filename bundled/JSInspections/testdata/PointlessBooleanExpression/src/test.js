//test for inspection PointlessBooleanExpression
x = y && true;
x = y || false; // y can be undefined so explicit false is needed
x = false || y;
x = true || y;
x = true ^ y;
x = !true ^ y;

var var_name;
if (var_name != false)
{
}

if (var_name == true)
{
}

if (a === false) {}
if (a !== false) {}