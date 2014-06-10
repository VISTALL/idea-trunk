package com.sixrr.xrp.context;

import com.intellij.psi.xml.XmlFile;

import java.util.Iterator;

public class RegexFilteredContext implements Context {
    private final Context context;
    private final String regex;

    public RegexFilteredContext(Context context, String regex) {
        super();
        this.context = context;
        this.regex = regex;
    }

    public Iterator<XmlFile> iterator() {
        final Iterator<XmlFile> iterator = context.iterator();
        return new URIFilteredIterator(iterator, regex);
    }
}
