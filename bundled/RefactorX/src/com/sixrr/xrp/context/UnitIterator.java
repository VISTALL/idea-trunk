package com.sixrr.xrp.context;

import java.util.Iterator;
import java.util.NoSuchElementException;

class UnitIterator<T> implements Iterator<T> {
    private final T value;

    private boolean hasAdvanced = false;

    UnitIterator(T value) {
        super();
        this.value = value;
    }

    public boolean hasNext() {
        return !hasAdvanced;
    }

    public T next() throws NoSuchElementException {
        if (hasAdvanced) {
            throw new NoSuchElementException();
        }
        hasAdvanced = true;
        return value;
    }

    public void remove() {
    }
}
