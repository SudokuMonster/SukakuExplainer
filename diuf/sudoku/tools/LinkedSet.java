/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package diuf.sudoku.tools;

import java.util.*;

/**
 * Reflective set implementation. Like a <code>LinkedHashSet</code>
 * but with a weird method:
 * {@link #get(Object)}, which returns the element
 * of the set that is equal to the given element.
 * This is especially usefull when the implementation
 * of <code>equals</code> does not compare all fields.
 */
public class LinkedSet<T> extends AbstractSet<T> {

    /*
     * This implementation uses the wrapper pattern on the following map:
     */
    private final LinkedHashMap<T,T> target = new LinkedHashMap<T,T>();


    @Override
    public boolean add(T o) {
        return (target.put(o, o) != null);
    }

    @Override
    public void clear() {
        target.clear();
    }

    @Override
    public boolean contains(Object o) {
        return target.containsKey(o);
    }

    public T get(T o) {
        return target.get(o);
    }

    @Override
    public Iterator<T> iterator() {
        return target.keySet().iterator();
    }

    @Override
    public boolean remove(Object o) {
        return (target.remove(o) != null);
    }

    @Override
    public int size() {
        return target.size();
    }

}
