//test for inspection ThisExpressionReferencesGlobalObject
this;

function barzoom()
{
    this;
}

var myObject = {
	property : value ,
	property : value ,
	property : this ,
	property : value
}