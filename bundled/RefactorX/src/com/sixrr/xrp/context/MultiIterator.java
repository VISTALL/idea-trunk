package com.sixrr.xrp.context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

class MultiIterator<E> implements Iterator<E> {

    private final List<Iterator<E>> enumerations;
    private int currentIndex = 0;
    private E nextElement = null;

    MultiIterator(List<Iterator<E>> enumerations) {
        super();
        this.enumerations = new ArrayList<Iterator<E>>(enumerations);
        fetchNextElement();
    }

    public boolean hasNext() {
        return nextElement != null;
    }

    public E next() {
        if (nextElement == null) {
            throw new NoSuchElementException();
        }
        final E out = nextElement;
        fetchNextElement();
        return out;
    }

    public void remove() {
    }

    private void fetchNextElement() {
        while (currentIndex < enumerations.size()) {
            final Iterator<E> currentEnumerator = enumerations.get(currentIndex);
            if (currentEnumerator.hasNext()) {
                nextElement = currentEnumerator.next();
                return;
            } else {
                currentIndex++;
            }
        }
        nextElement = null;
    }
}
