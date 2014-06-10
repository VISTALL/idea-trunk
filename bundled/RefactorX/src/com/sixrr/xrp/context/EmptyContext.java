package com.sixrr.xrp.context;

import com.intellij.psi.xml.XmlFile;
import com.intellij.util.containers.EmptyIterator;

import java.util.Iterator;

public class EmptyContext implements Context {
    public Iterator<XmlFile> iterator() {
        return EmptyIterator.getInstance();
    }
}
