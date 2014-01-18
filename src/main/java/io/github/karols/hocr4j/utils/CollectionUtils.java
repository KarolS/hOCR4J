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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import java.util.*;
import javax.annotation.Nonnull;

/**
 * Utility functions for dealing with collections.
 */
public final class CollectionUtils {

    private CollectionUtils(){}

    /**
     * Creates a new list with those elements of the original list
     * that satisfy the predicate.
     * @param xs original list
     * @param f the predicate to filter on
     * @param <T> element type
     * @return filtered list
     */
    @Nonnull
    public static <T> List<T> listFilter(@Nonnull List<T> xs, @Nonnull Predicate<T> f) {
        ArrayList<T> result = new ArrayList<T>(xs.size());
        for (T x : xs) {
            if (f.apply(x)) {
                result.add(x);
            }
        }
        return result;
    }

    /**
     * Creates a new list with elements being results of mapping
     * the function over the original list.
     * @param xs original list
     * @param f function
     * @param <T> original list element type, also function parameter type
     * @param <U> function result type
     * @return list of results of mapping the function
     */
    @Nonnull
    public static <T, U> List<U> listMap(@Nonnull List<T> xs, @Nonnull Function<T, U> f) {
        ArrayList<U> result = new ArrayList<U>(xs.size());
        for (T x : xs) {
            result.add(f.apply(x));
        }
        return result;
    }

    /**
     * Creates a new immutable list with an element added to the end.
     * @param list original list
     * @param elem element to add
     * @param <T> element type
     * @return list with added element
     */
    @Nonnull
    public static <T> ImmutableList<T> pushBack(@Nonnull Iterable<T> list, T elem) {
        ImmutableList.Builder<T> b = ImmutableList.builder();
        b.addAll(list);
        b.add(elem);
        return b.build();
    }

    /**
     * Creates a new set with elements being results of mapping
     * the function over the original set.
     * @param xs original set
     * @param f function
     * @param <T> original set element type, also function parameter type
     * @param <U> function result type
     * @return set of results of mapping the function
     */
    @Nonnull
    public static <T, U> Set<U> setMap(@Nonnull Set<T> xs, @Nonnull Function<T, U> f) {
        HashSet<U> result = new HashSet<U>(xs.size());
        for (T x : xs) {
            result.add(f.apply(x));
        }
        return result;
    }

    /**
     * Wraps an <code>ListIterator</code> and returns a <code>ListIterator</code>
     * that cannot modify the underlying list.
     * All methods that could be used to modify the list throw
     * <code>UnsupportedOperationException</code>
     * @param underlying original list iterator
     * @param <T> element type
     * @return unmodifiable list iterator
     */
    @Nonnull
    public static <T> ListIterator<T> unmodifiableListIterator(final @Nonnull ListIterator<T> underlying) {
        return new ListIterator<T>() {
            public boolean hasNext() {
                return underlying.hasNext();
            }

            public T next() {
                return underlying.next();
            }

            public boolean hasPrevious() {
                return underlying.hasPrevious();
            }

            public T previous() {
                return underlying.previous();
            }

            public int nextIndex() {
                return underlying.nextIndex();
            }

            public int previousIndex() {
                return underlying.previousIndex();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public void set(T t) {
                throw new UnsupportedOperationException();
            }

            public void add(T t) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
