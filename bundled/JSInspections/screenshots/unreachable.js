function linkIt() {
	if (this.linkText.indexOf("javascript:")!=-1) eval(this.linkText)
	else menuLoc.location.href = this.linkText;
}

function popDown(menuName){
    if (!isLoaded || !areCreated) {
        if (!clickKill) whichEl.hideTop();
        return;
    }
    else {
        return;
    }
    whichEl = eval(menuName);
	whichEl.isOn = false;

}

function hideAll() {
	for(i=1; i<topCount; i++) {
		temp = eval("elMenu" + i + ".startChild");
		temp.isOn = false;
		if (temp.hasChildVisible) temp.hideChildren();
		temp.showIt(false);
	}
}

function hideTree() {
	allTimer = null;
	if (isOverMenu) return;
	if (this.hasChildVisible) {
		this.hideChildren();
	}
	this.hideParents();
}