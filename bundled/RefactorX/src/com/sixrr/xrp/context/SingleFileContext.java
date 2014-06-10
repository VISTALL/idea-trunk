package com.sixrr.xrp.context;

import com.intellij.psi.xml.XmlFile;

import java.util.Iterator;

public class SingleFileContext implements Context {
    private final XmlFile file;

    public SingleFileContext(XmlFile file) {
        super();
        this.file = file;
    }
    public Iterator<XmlFile> iterator() {
        return new UnitIterator<XmlFile>(file);
    }
}
