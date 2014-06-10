package com.sixrr.xrp.context;

import com.intellij.psi.xml.XmlDoctype;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlProlog;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class URIFilteredIterator implements Iterator<XmlFile> {
    private final Iterator<XmlFile> baseIterator;
    private final String uri;
    private XmlFile nextFile = null;
    private boolean advanced = false;
    private boolean completed = false;

    public URIFilteredIterator(Iterator<XmlFile> baseIterator, String uri) {
        super();
        this.baseIterator = baseIterator;
        this.uri = uri;
    }

    public boolean hasNext() {
        if (completed) {
            return false;
        }
        if (!advanced) {
            advance();
        }
        return !completed;
    }

    public XmlFile next() {
        if (completed) {
            throw new NoSuchElementException();
        }
        if (!advanced) {
            advance();
        }
        advanced = false;
        return nextFile;
    }

    private void advance() {
        if (completed) {
            return;
        }
        do {
            if (baseIterator.hasNext()) {
                nextFile = baseIterator.next();
                final String testURI = calculateDocTypeURI(nextFile);
                if(uri.equals(testURI))
                {
                    advanced = true;
                }
            } else {
                completed = true;
            }

        } while (!completed && !advanced);
    }

    private static String calculateDocTypeURI(XmlFile file) {
        final XmlDocument document = file.getDocument();
        if (document == null) {
            return null;
        }
        final XmlProlog prolog = document.getProlog();
        if (prolog == null) {
            return null;
        }
        final XmlDoctype doctype = prolog.getDoctype();
        if (doctype == null) {
            return null;
        }
        return doctype.getDtdUri();
    }

    public void remove() {
    }
}
