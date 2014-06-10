package com.sixrr.xrp.context;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;

import java.util.Iterator;
import java.util.NoSuchElementException;

class DirectoryIterator implements Iterator<XmlFile> {
    private final PsiFile[] files;
    private int currentIndex = 0;
    private XmlFile nextFile = null;

    DirectoryIterator(PsiDirectory directory) {
        super();
        files = directory.getFiles();
        fetchNextFile();
    }

    public boolean hasNext() {
        return nextFile != null;
    }

    public XmlFile next() {
        if (nextFile == null) {
            throw new NoSuchElementException();
        }
        final XmlFile out = nextFile;
        fetchNextFile();
        return out;
    }

    public void remove() {
    }

    private void fetchNextFile() {
        while (currentIndex < files.length) {
            if (files[currentIndex] instanceof XmlFile) {
                nextFile = (XmlFile) files[currentIndex];
                currentIndex++;
                return;
            }
            currentIndex++;
        }
        nextFile = null;
    }
}
