//test for inspection UnnecessaryReturn
function foo()
{
    return;
}

function foo()
{
    if (true)
        return;
    else
    {
        with (x)
        {
            return;
        }
    }

}
//this return is necessary
function test() {
    for (var i=0; i<10000; i++) {
        if (i == 5) return;
    }
}

function test2() {
    var foo = 1;
    switch (foo) {
        case 1: alert(1); return;
        case 4: alert(1);
    }
}