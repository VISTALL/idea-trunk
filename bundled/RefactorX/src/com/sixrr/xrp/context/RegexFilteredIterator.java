package com.sixrr.xrp.context;

import com.intellij.psi.xml.XmlFile;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

public class RegexFilteredIterator implements Iterator<XmlFile>{
    private final Iterator<XmlFile> baseIterator;
    private final String regex;
    private XmlFile nextFile = null;
    private boolean advanced = false;
    private boolean completed = false;
    private Pattern pattern;

    public RegexFilteredIterator(Iterator<XmlFile> baseIterator, String regex){
        super();
        this.baseIterator = baseIterator;
        this.regex = regex;
        try{
            pattern = Pattern.compile(regex);
        } catch(Exception e){
            pattern = null;
        }
    }

    public boolean hasNext(){
        if(completed){
            return false;
        }
        if(!advanced){
            advance();
        }
        return !completed;
    }

    public XmlFile next(){
        if(completed){
            throw new NoSuchElementException();
        }
        if(!advanced){
            advance();
        }
        advanced = false;
        return nextFile;
    }

    private void advance(){
        if(completed){
            return;
        }
        do{
            if(baseIterator.hasNext()){
                nextFile = baseIterator.next();
                final String fileName = nextFile.getName();
                if(pattern == null || pattern.matcher(fileName).matches()){
                    advanced = true;
                }
            } else{
                completed = true;
            }
        } while(!completed && !advanced);
    }

    public void remove(){
    }
}
