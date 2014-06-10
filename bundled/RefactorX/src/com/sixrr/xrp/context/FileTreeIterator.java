package com.sixrr.xrp.context;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.xml.XmlFile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class FileTreeIterator implements Iterator<XmlFile> {
    private final Iterator<XmlFile> multiEnumeration;

    FileTreeIterator(PsiDirectory directory) {
        super();
        final PsiDirectory[] subdirectories = directory.getSubdirectories();
        final List<Iterator<XmlFile>> enumerations =
                new ArrayList<Iterator<XmlFile>>(subdirectories.length + 1);
        enumerations.add(new DirectoryIterator(directory));
        for (PsiDirectory subdirectory : subdirectories) {
            enumerations.add(new FileTreeIterator(subdirectory));
        }
        multiEnumeration = new MultiIterator<XmlFile>(enumerations);
    }

    public boolean hasNext() {
        return multiEnumeration.hasNext();
    }

    public XmlFile next() {
        return multiEnumeration.next();
    }

    public void remove() {
    }
}
