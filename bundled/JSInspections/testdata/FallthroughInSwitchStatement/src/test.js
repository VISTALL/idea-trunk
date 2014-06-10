//test for inspection FallthroughInSwitchStatement
if (IE4) menuLocBod = menuLoc.document.body;
if (!isFrames) {
    xPos = (currentMenu.menuLeft) ? currentMenu.menuLeft : (NS4) ? e.pageX : (event.clientX + menuLocBod.scrollLeft);
    yPos = (currentMenu.menuTop) ? currentMenu.menuTop : (NS4) ? e.pageY : (event.clientY + menuLocBod.scrollTop);
}
else {
    switch (navFrLoc) {
        case "left":
            xPos = (currentMenu.menuLeft) ? currentMenu.menuLeft : (NS4) ? menuLoc.pageXOffset : menuLocBod.scrollLeft;
            yPos = (currentMenu.menuTop) ? currentMenu.menuTop : (NS4) ? (e.pageY-pageYOffset)+menuLoc.pageYOffset : event.clientY + menuLocBod.scrollTop;
            break;
        case "top":
            xPos = (currentMenu.menuLeft) ? currentMenu.menuLeft : (NS4) ? (e.pageX-pageXOffset)+menuLoc.pageXOffset : event.clientX + menuLocBod.scrollLeft;
            yPos = (currentMenu.menuTop) ? currentMenu.menuTop : (NS4) ? menuLoc.pageYOffset : menuLocBod.scrollTop;
        case "bottom":
            xPos = (currentMenu.menuLeft) ? currentMenu.menuLeft : (NS4) ? (e.pageX-pageXOffset)+menuLoc.pageXOffset : event.clientX + menuLocBod.scrollLeft;
            yPos = (currentMenu.menuTop) ? currentMenu.menuTop : (NS4) ? menuLoc.pageYOffset+menuLoc.innerHeight : menuLocBod.scrollTop + menuLocBod.clientHeight;
            break;
        case "right":
            xPos = (currentMenu.menuLeft) ? currentMenu.menuLeft : (NS4) ? menuLoc.pageXOffset+menuLoc.innerWidth : menuLocBod.scrollLeft+menuLocBod.clientWidth;
            yPos = (currentMenu.menuTop) ? currentMenu.menuTop : (NS4) ? (e.pageY-pageYOffset)+menuLoc.pageYOffset : event.clientY + menuLocBod.scrollTop;
            break;
    }
