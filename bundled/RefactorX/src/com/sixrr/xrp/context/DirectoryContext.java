package com.sixrr.xrp.context;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.xml.XmlFile;

import java.util.Iterator;

public class DirectoryContext implements Context {
    private final PsiDirectory directory;
    private final boolean recursive;

    public DirectoryContext(PsiDirectory directory, boolean recursive) {
        super();
        this.directory = directory;
        this.recursive = recursive;
    }

    public Iterator<XmlFile> iterator() {
        if (recursive) {
            return new FileTreeIterator(directory);
        } else {
            return new DirectoryIterator(directory);
        }
    }
}
