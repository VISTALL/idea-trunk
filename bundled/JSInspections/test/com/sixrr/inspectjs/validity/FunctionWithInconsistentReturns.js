function foo()
{
    if (true)
        return 3;
    return;
}

function f1() {
    function f2() {
        return null;
    }
}

function f() {
    with (this) {
        try {
            return null;
        } catch (x) {
            return null;
        }
    }
}