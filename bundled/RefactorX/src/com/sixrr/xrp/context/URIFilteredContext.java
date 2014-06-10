package com.sixrr.xrp.context;

import com.intellij.psi.xml.XmlFile;

import java.util.Iterator;

public class URIFilteredContext implements Context {
    private final Context context;
    private final String uri;

    public URIFilteredContext(Context context, String uri) {
        super();
        this.context = context;
        this.uri = uri;
    }

    public Iterator<XmlFile> iterator() {
        final Iterator<XmlFile> iterator = context.iterator();
        return new URIFilteredIterator(iterator,uri );
    }
}
