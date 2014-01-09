/* Copyright (c) 2014 Karol Stasiak
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*/

package io.github.karols.hocr4j.utils;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of a read-only queue
 * that returns consecutive elements from the given list.
 * @param <E> element type
 */
public class ListWrappingQueue<E> extends AbstractQueue<E> {

    private int traversed = 0;
    private List<E> underlying;

    public ListWrappingQueue(List<E> list) {
        underlying = list;
    }

    @Override
    public Iterator<E> iterator() {
        Iterator<E> iter = underlying.iterator();
        for (int i = 0; i < traversed; i++) {
            iter.next();
        }
        return iter;
    }//tested

    /**
     * @throws UnsupportedOperationException
     */
    public boolean offer(E e) {
        throw new UnsupportedOperationException();
    }//tested

    public E peek() {
        return underlying.get(traversed);
    }//tested

    public E poll() {
        E elem = underlying.get(traversed);
        traversed++;
        return elem;
    }//tested

    @Override
    public int size() {
        return underlying.size() - traversed;
    }//tested
}
