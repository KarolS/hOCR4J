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

import com.google.common.collect.Iterators;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nonnull;

/**
 * An immutable wrapper around a mutable list.
 *
 * @param <T> element type
 */
public abstract class DelegatingUnmodifiableList<T> implements List<T> {

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is immutable");
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void add(int i, T t) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is immutable");
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean addAll(Collection<? extends T> ts) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is immutable");
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean addAll(int i, Collection<? extends T> ts) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is immutable");
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is immutable");
    }

    @Override
    public boolean contains(Object o) {
        return getUnderlying().contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> objects) {
        return getUnderlying().containsAll(objects);
    }

    @Override
    public T get(int i) {
        return getUnderlying().get(i);
    }

    /**
     * The underlying mutable list.
     * @return mutable list
     */
    protected abstract List<T> getUnderlying();

    @Override
    public int indexOf(Object o) {
        return getUnderlying().indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        return getUnderlying().isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.unmodifiableIterator(getUnderlying().iterator());
    }

    @Override
    public int lastIndexOf(Object o) {
        return getUnderlying().lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return CollectionUtils.unmodifiableListIterator(getUnderlying().listIterator());
    }

    @Override
    public ListIterator<T> listIterator(int i) {
        return CollectionUtils.unmodifiableListIterator(getUnderlying().listIterator(i));
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is immutable");
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public T remove(int i) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is immutable");
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean removeAll(Collection<?> objects) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is immutable");
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean retainAll(Collection<?> objects) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is immutable");
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    public T set(int i, T t) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is immutable");
    }

    @Override
    public int size() {
        return getUnderlying().size();
    }

    /**
     * Creates a sublist. It's an abstract method, so that the implementations can provide custom behaviour.
     * The sublist is unmodifiable, so the implementations are free to create copies that do not hold a reference to the original list.
     *
     * @param i starting index
     * @param j ending index
     * @return unmodifiable sublist
     */
    @Nonnull
    @Override
    public abstract DelegatingUnmodifiableList<T> subList(int i, int j);

    @Nonnull
    @Override
    public Object[] toArray() {
        return getUnderlying().toArray();
    }

    @Nonnull
    @Override
    public <U> U[] toArray(U[] target) {
        return getUnderlying().toArray(target);
    }
}
