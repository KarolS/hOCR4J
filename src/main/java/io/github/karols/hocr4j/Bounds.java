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

package io.github.karols.hocr4j;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.lang.Math.*;

/**
 * A rectangle, used to represent bounds surrounding elements of the page.
 * <br/>
 * The rectangles are specified using integer coordinates
 * and usually the unit used is pixels.
 */
public class Bounds implements Comparable<Bounds>, Bounded {

    /**
     * Creates bounds from a value of the <code>title</code>
     * attribute on an HOCR tag.
     * <br/>
     * The <code>title</code> attribute contains a comma separated list
     * of various properities, the bounds are expressed as follows:
     * <br/>
     * <code>bbox LEFT TOP RIGHT BOTTOM</code>
     * <br/>
     * If the value does not contain any bounds, <code>null</code> is returned.
     *
     * @param titleValue value of the <code>title</code> attribute
     * @return bounds found in the value, or <code>null</code> if none found
     */
    @Nullable
    public static Bounds fromHocrTitleValue(@Nonnull String titleValue) {
        int defStart = titleValue.indexOf("bbox ");
        if (defStart < 0) {
            return null;
        }
        titleValue = titleValue.substring(defStart);
        int descEnd = titleValue.indexOf(';');
        if (descEnd >= 0) {
            titleValue = titleValue.substring(0, descEnd);
        }
        String[] parts = titleValue.split(" ");
        int left = Integer.parseInt(parts[1]);
        int top = Integer.parseInt(parts[2]);
        int right = Integer.parseInt(parts[3]);
        int bottom = Integer.parseInt(parts[4]);
        return new Bounds(left, top, right, bottom);
    }

    /**
     * Returns the largest possible bounds.
     *
     * @return the entire plane
     */
    public static Bounds getEntirePlane() {
        return new Bounds(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Returns the semiplane on the bottom of the horizontal line with the given coordinate.
     *
     * @param y the top edge coordinate
     * @return the semiplane
     */
    public static Bounds getBottomSemiplane(int y) {
        return new Bounds(Integer.MIN_VALUE, y, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Returns the semiplane on the left of the vertical line with the given coordinate.
     *
     * @param x the right edge coordinate
     * @return the semiplane
     */
    public static Bounds getLeftSemiplane(int x) {
        return new Bounds(Integer.MIN_VALUE, Integer.MIN_VALUE, x, Integer.MAX_VALUE);
    }

    /**
     * Returns the semiplane on the right of the vertical line with the given coordinate.
     *
     * @param x the left edge coordinate
     * @return the semiplane
     */
    public static Bounds getRightSemiplane(int x) {
        return new Bounds(x, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Returns the semiplane on the top of the horizontal line with the given coordinate.
     *
     * @param y the bottom edge coordinate
     * @return the semiplane
     */
    public static Bounds getTopSemiplane(int y) {
        return new Bounds(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, y);
    }


    /**
     * Checks if the bounds are empty.
     * <br/>
     * Treats <code>null</code> as empty.
     *
     * @param bounds bounds to check
     * @return <code>true</code> if given bounds are empty, false otherwise.
     * @see Bounds#isEmpty()
     */
    public static boolean isEmpty(@Nullable Bounds bounds) {
        return bounds == null || bounds.isEmpty();
    }

    /**
     * A null-safe union of bounds. Treats <code>null</code> as empty bounds.
     * Returns <code>null</code> if any argument is <code>null</code>.
     * <br/>
     * An intesection of several bounding rectangles
     * is the largest bounding rectangle that is contained by all the input rectangles.
     *
     * @param b1 first bounds
     * @param b2 second bounds
     * @return intersection of bounds
     * @see Bounds#intersection(Bounds)
     */
    @Nullable
    public static Bounds nullSafeIntersection(@Nullable Bounds b1, @Nullable Bounds b2) {
        if (b1 == null || b2 == null) {
            return null;
        }
        return b1.intersection(b2);
    }

    /**
     * A null-safe union of bounds. Treats <code>null</code> as empty bounds.
     * Returns <code>null</code> if both arguments are <code>null</code>.
     * <br/>
     * A union of several bounding rectangles
     * is the smallest bounding rectangle that contains all the input rectangles.
     *
     * @param b1 first bounds
     * @param b2 second bounds
     * @return union of bounds
     * @see Bounds#union(Bounds)
     */
    @Nullable
    public static Bounds nullSafeUnion(@Nullable Bounds b1, @Nullable Bounds b2) {
        if (b1 == null) {
            return b2;
        }
        if (b2 == null) {
            return b1;
        }
        return b1.union(b2);
    }

    /**
     * The smallest rectangle containing all the elements of the collection.
     * The collection and all its elements have to be non-null.
     * If the collections is empty, returns <code>null</code>.
     *
     * @param thingies collection of bounded objects
     * @return union of bounds of all objects,
     *         or <code>null</code> if the collection was empty
     * @see Bounds#ofAll(Bounded...)
     * @see Bounds#nullSafeUnion(Bounds, Bounds)
     * @see Bounds#union(Bounds)
     * @see Bounds#ofAllLeft(Collection)
     * @see Bounds#ofAllRight(Collection)
     */
    @Nullable
    public static Bounds ofAll(@Nonnull Collection<? extends Bounded> thingies) {
        Bounds acc = null;
        for (Bounded thingy : thingies) {
            acc = nullSafeUnion(acc, thingy.getBounds());
        }
        return acc;
    }

    /**
     * The smallest rectangle containing all the elements of the parameter list/array.
     * All objects have to be non-null.
     * If the collections is empty, returns <code>null</code>.
     *
     * @param thingies parameter list/array of bounded objects
     * @return union of bounds of all objects,
     *         or <code>null</code> if the parameter list/array was empty
     * @see Bounds#ofAll(Collection)
     * @see Bounds#nullSafeUnion(Bounds, Bounds)
     * @see Bounds#union(Bounds)
     */
    @Nullable
    public static Bounds ofAll(@Nonnull Bounded... thingies) {
        Bounds acc = thingies[0].getBounds();
        for (Bounded thingy : thingies) {
            acc = acc.union(thingy.getBounds());
        }
        return acc;
    }

    /**
     * The smallest rectangle containing the first elements of all pairs of the collection.
     * The collection, all the pairs and all the first elements have to be non-null.
     * If the collections is empty, returns <code>null</code>.
     *
     * @param thingies collection of pairs
     * @param <T>      type of the second element of the pair
     * @param <B>      type of the first element of the pair
     * @return union of bounds of all first elements,
     *         or <code>null</code> if the collection was empty
     * @see Bounds#ofAll(Collection)
     * @see Bounds#ofAllRight(Collection)
     */
    @Nullable
    public static <T, B extends Bounded> Bounds ofAllLeft(@Nonnull Collection<Pair<B, T>> thingies) {
        Bounds acc = null;
        for (Pair<B, T> thingy : thingies) {
            acc = nullSafeUnion(acc, thingy.getLeft().getBounds());
        }
        return acc;
    }

    /**
     * The smallest rectangle containing the second elements of all pairs of the collection.
     * The collection, all the pairs and all the second elements have to be non-null.
     * If the collections is empty, returns <code>null</code>.
     *
     * @param thingies collection of pairs
     * @param <T>      type of the first element of the pair
     * @param <B>      type of the second element of the pair
     * @return union of bounds of all second elements,
     *         or <code>null</code> if the collection was empty
     * @see Bounds#ofAll(Collection)
     * @see Bounds#ofAllLeft(Collection)
     */
    @Nullable
    public static <T, B extends Bounded> Bounds ofAllRight(@Nonnull Collection<Pair<T, B>> thingies) {
        Bounds acc = null;
        for (Pair<T, B> thingy : thingies) {
            acc = nullSafeUnion(acc, thingy.getRight().getBounds());
        }
        return acc;
    }

    private final int bottom;
    private final int left;
    private final int right;
    private final int top;

    /**
     * Creates bounds given its dimensions.
     *
     * @param l x coordinate of the left edge
     * @param t y coordinate of the top edge
     * @param r x coordinate of the right edge
     * @param b y coordinate of the bottom edge
     */
    public Bounds(int l, int t, int r, int b) {
        left = l;
        top = t;
        right = r;
        bottom = b;
    }

    public int compareTo(@Nonnull Bounds that) {
        return new CompareToBuilder()
                .append(this.left, that.left)
                .append(this.top, that.top)
                .append(this.right, that.right)
                .append(this.bottom, that.bottom)
                .toComparison();
    }

    /**
     * Checks if these bounds contain given bounds.
     * Interprets <code>null</code> as empty bounds, returning <code>true</code>.
     *
     * @param b bounds to check
     * @return <code>true</code> if <code>b</code> is contained within these bounds,
     *         <code>false</code> otherwise
     * @see Bounds#in(Bounds)
     */
    public boolean contains(@Nullable Bounds b) {
        return b == null || b.in(this);
    }

    /**
     * Checks if these bounds cut the other bounds.
     * <br/>
     * "X cuts Y" means:
     * <ul>
     * <li>intersection of X and Y has non-zero area</li>
     * <li>X is not contained within Y</li>
     * <li>Y is not contained within X</li>
     * </ul>
     *
     * @param b the other bounds
     * @return <code>true</code> if the bounds cut, <code>false</code> otherwise
     * @see Bounds#intersects(Bounds)
     * @see Bounds#touches(Bounds)
     */
    public boolean cuts(@Nullable Bounds b) {
        return intersects(b) && !in(b) && !contains(b);
    }

    /**
     * Returns taxicab distance between the centers of two bounds.
     *
     * @param b the other bounds (not <code>null</code>)
     * @return taxicab distance
     */
    public int distance(@Nonnull Bounds b) {
        return abs(getMiddle() - b.getMiddle()) + abs(getLeftish() - b.getLeftish());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bounds bounds = (Bounds) o;

        if (bottom != bounds.bottom) return false;
        if (left != bounds.left) return false;
        if (right != bounds.right) return false;
        if (top != bounds.top) return false;

        return true;
    }

    /**
     * Creates new bounds that have the same y coordinates as these ones,
     * but have the x coordinates containing both x coordinates of these bounds
     * as the coordinates of the other bounds (<code>heightSource</code>).
     * <br/>
     * Interprets <code>null</code> as empty bounds, returning these bounds unmodified.
     *
     * @param heightSource bounds with interesting y coordinates
     * @return bounds with extended height
     */
    @Nonnull
    public Bounds extendHeight(@Nullable Bounded heightSource) {
        if (heightSource == null || heightSource == this) {
            return this;
        }
        return new Bounds(
                left,
                min(heightSource.getBounds().top, top),
                right,
                max(heightSource.getBounds().bottom, bottom));
    }

    /**
     * Creates new bounds that have the same y coordinates as these ones,
     * but have the x coordinates containing both x coordinates of these bounds
     * as the coordinates of the other bounds (<code>widthSource</code>).
     * <br/>
     * Interprets <code>null</code> as empty bounds, returning these bounds unmodified.
     *
     * @param widthSource bounds with interesting x coordinates
     * @return bounds with extended width
     */
    @Nonnull
    public Bounds extendWidth(@Nullable Bounded widthSource) {
        if (widthSource == null || widthSource == this) {
            return this;
        }
        return new Bounds(
                min(widthSource.getBounds().left, left),
                top,
                max(widthSource.getBounds().right, right),
                bottom);
    }

    /**
     * Returns the area of these bounds.
     *
     * @return area
     */
    public int getArea() {
        return getWidth() * getHeight();
    }

    /**
     * Y coordinate of the bottom edge
     *
     * @return y coordinate of the bottom edge
     */
    public int getBottom() {
        return bottom;
    }

    /**
     * Returns zero-height bounds corresponding to the bottom edge of these bounds.
     *
     * @return bounds of the bottom edge itself
     */
    @Nonnull
    public Bounds getBottomEdge() {
        return new Bounds(left, bottom, right, bottom);
    }

    /**
     * Returns these bounds.
     *
     * @return <code>this</code>
     */
    @Nonnull
    @Override
    public Bounds getBounds() {
        return this;
    }

    /**
     * Returns the average of the x coordinates of the left and right edges, rounded down.
     *
     * @return average of the x coordinates of the left and right edges
     */
    public int getCenter() {
        return (right + left) / 2;
    }

    /**
     * Returns the height to these bounds.
     *
     * @return height
     */
    public int getHeight() {
        return bottom - top;
    }

    /**
     * X coordinate of the left edge
     *
     * @return x coordinate of the left edge
     */
    public int getLeft() {
        return left;
    }

    /**
     * Returns zero-width bounds corresponding to the left edge of these bounds.
     *
     * @return bounds of the left edge itself
     */
    @Nonnull
    public Bounds getLeftEdge() {
        return new Bounds(left, top, left, bottom);
    }

    /**
     * Return the x coordinate on 20% of the way from the left edge to the right edge, rounding down.
     *
     * @return x coordinate 20% width to the right of the left edge
     */
    public int getLeftish() {
        return (right + 4 * left) / 5;
    }

    /**
     * Returns the average of the y coordinates of the top and bottom edges, rounded down.
     *
     * @return average of the y coordinates of the top and bottom edges
     */

    public int getMiddle() {
        return (top + bottom) / 2;
    }

    /**
     * X coordinate of the right edge
     *
     * @return x coordinate of the right edge
     */
    public int getRight() {
        return right;
    }

    /**
     * Returns zero-width bounds corresponding to the right edge of these bounds.
     *
     * @return bounds of the right edge itself
     */
    @Nonnull
    public Bounds getRightEdge() {
        return new Bounds(right, top, right, bottom);
    }

    /**
     * Y coordinate of the top edge
     *
     * @return y coordinate of the top edge
     */
    public int getTop() {
        return top;
    }

    /**
     * Returns zero-height bounds corresponding to the top edge of these bounds.
     *
     * @return bounds of the top edge itself
     */
    @Nonnull
    public Bounds getTopEdge() {
        return new Bounds(left, top, right, top);
    }

    /**
     * Returns the width of these bounds.
     *
     * @return width
     */
    public int getWidth() {
        return right - left;
    }

    /**
     * Creates bounds by expanding these bounds
     * by the given number of pixels in all four directions.
     *
     * @param pixels number of pixels to expand
     * @return larger bounds
     */
    @Nonnull
    public Bounds grow(int pixels) {
        return new Bounds(left - pixels, top - pixels, right + pixels, bottom + pixels);
    }

    @Override
    public int hashCode() {
        int result = left;
        result = 31 * result + top;
        result = 31 * result + right;
        result = 31 * result + bottom;
        return result;
    }

    /**
     * Checks if these bounds are contained within given bounds.
     * <br/>
     * Interprets <code>null</code> as empty bounds, returning <code>false</code>.
     *
     * @param b bounds to check
     * @return <code>true</code> if these bounds are contained inside <code>b</code>,
     *         <code>false</code> otherwise
     * @see Bounds#contains(Bounds)
     */
    public boolean in(@Nullable Bounds b) {
        return b != null && left >= b.left && right <= b.right && top >= b.top && bottom <= b.bottom;
    }

    /**
     * Checks if x coordinates of these bounds and the other bounds overlap.
     * This can be used to check if two sets of bounds belong to text in the same column.
     * <br/>
     * Interprets <code>null</code> as empty bounds, returning <code>false</code>.
     *
     * @param bounds other bounds
     * @return <code>true</code> if the bounds are in the same column,
     *         <code>false</code> otherwise
     */
    public boolean inTheSameColumnAs(@Nullable Bounds bounds) {
        return bounds != null && left <= bounds.right && right >= bounds.left;
    }

    /**
     * Returns the intersection of these bounds and the other bounds.
     * <br/>
     * Interprets <code>null</code> as empty bounds, returning <code>null</code>.
     * May return bounds with negative width or height. It may be fixed later.
     * <br/>
     * An intesection of two bounding rectangles
     * is the largest bounding rectangle that is contained by both the input rectangles.
     *
     * @param b the other bounds
     * @return intersection of bounds
     * @see Bounds#intersects(Bounds)
     * @see Bounds#nullSafeIntersection(Bounds, Bounds)
     */
    @Nullable
    public Bounds intersection(@Nullable Bounds b) {
        if (b == null) {
            return null;
        }
        if (b == this) {
            return this;
        }
        return new Bounds(max(left, b.left), max(top, b.top), min(right, b.right), min(bottom, b.bottom));
    }

    /**
     * Checks if the bounds intersect and the intersection has non-zero area.
     *
     * @param b other bounds
     * @return <code>true</code> if bounds intersect, <code>false</code> otherwise
     * @see Bounds#intersection(Bounds)
     * @see Bounds#cuts(Bounds)
     * @see Bounds#touches(Bounds)
     */
    public boolean intersects(@Nullable Bounds b) {
        Bounds intersection = intersection(b);
        return intersection != null && !intersection.isEmpty();
    }

    /**
     * Checks if these bounds are above given bounds.
     *
     * @param b given bounds (not <code>null</code>)
     * @return <code>true</code> if these bounds are above the given bounds,
     *         <code>false</code> otherwise
     * @throws NullPointerException if <code>b</code> is <code>null</code>
     */
    public boolean isAbove(@Nonnull Bounds b) {
        return b.isBelow(this);
    }

    /**
     * Checks if these bounds are below given bounds.
     *
     * @param b given bounds (not <code>null</code>)
     * @return <code>true</code> if these bounds are below the given bounds,
     *         <code>false</code> otherwise
     * @throws NullPointerException if <code>b</code> is <code>null</code>
     */
    public boolean isBelow(@Nonnull Bounds b) {
        return this.top >= b.bottom;
    }

    /**
     * Checks if the bounds have zero area or have negative width or height.
     * Such bounds are considered empty.
     *
     * @return <code>true</code> if the bounds are empty, <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return left >= right || top >= bottom;
    }

    /**
     * Checks if these bounds are to the left of given bounds.
     *
     * @param b given bounds (not <code>null</code>)
     * @return <code>true</code> if these bounds are to the left of the given bounds,
     *         <code>false</code> otherwise
     * @throws NullPointerException if <code>b</code> is <code>null</code>
     */
    public boolean isToTheLeft(@Nonnull Bounds b) {
        return this.right <= b.left;
    }

    /**
     * Checks if these bounds are to the right of given bounds.
     *
     * @param b given bounds (not <code>null</code>)
     * @return <code>true</code> if these bounds are to the right of the given bounds,
     *         <code>false</code> otherwise
     * @throws NullPointerException if <code>b</code> is <code>null</code>
     */
    public boolean isToTheRight(@Nonnull Bounds b) {
        return b.isToTheLeft(this);
    }

    /**
     * Creates bounds translated by the given amount down the y axis.
     *
     * @param amount amount to translate
     * @return translated bounds
     */
    @Nonnull
    public Bounds moveDown(int amount) {
        if (amount == 0) {
            return this;
        }
        return new Bounds(left, top + amount, right, bottom + amount);
    }

    /**
     * Creates bounds translated by the given amount leftwards the x axis.
     *
     * @param amount amount to translate
     * @return translated bounds
     */
    @Nonnull
    public Bounds moveToTheLeft(int amount) {
        return moveToTheRight(-amount);
    }

    /**
     * Creates bounds translated by the given amount rightwards the x axis.
     *
     * @param amount amount to translate
     * @return translated bounds
     */
    @Nonnull
    public Bounds moveToTheRight(int amount) {
        if (amount == 0) {
            return this;
        }
        return new Bounds(left + amount, top, right + amount, bottom);
    }

    /**
     * Creates bounds translated by the given amount up the y axis.
     *
     * @param amount amount to translate
     * @return translated bounds
     */
    @Nonnull
    public Bounds moveUp(int amount) {
        return moveDown(-amount);
    }

    /**
     * Creates new bounds which are a scaled copy of these bounds.
     *
     * @param scale scale of the new bounds
     * @return scaled bounds
     */
    @Nonnull
    public Bounds scale(double scale) {
        return new Bounds(
                (int) Math.round(left * scale),
                (int) Math.round(top * scale),
                (int) Math.round(right * scale),
                (int) Math.round(bottom * scale)
        );
    }

    /**
     * Divides this bounds vertically into <code>noOfSections</code> sections
     * of roughly the same width
     * and returns the section number <code>sectionIndex</code>.
     * <br/>
     * Sections are numbered from the left, starting from 0.
     *
     * @param sectionIndex section number to return
     * @param noOfSections total number of sections
     * @return bounds of the section
     * @throws IllegalArgumentException  if <code>noOfSections</code> is not positive
     * @throws IndexOutOfBoundsException if <code>sectionIndex</code> &lt; 0
     *                                   or <code>sectionIndex</code> &gt;= <code>noOfSections</code>
     */
    public Bounds section(int sectionIndex, int noOfSections) {
        return section(sectionIndex, sectionIndex + 1, noOfSections);
    }

    /**
     * Divides this bounds vertically into <code>noOfSections</code> sections
     * of roughly the same width
     * and returns the bounds of sections with numbers from range
     * [<code>startIndex</code>, <code>endIndex</code>).
     * <br/>
     * Sections are numbered from the left, starting from 0.
     *
     * @param startIndex   starting section number
     * @param endIndex     final section number (not included in the result)
     * @param noOfSections total number of sections
     * @return bounds of the sections
     * @throws IllegalArgumentException  if <code>noOfSections</code> is not positive
     *                                   or <code>startIndex</code> &gt; <code>endIndex</code>
     * @throws IndexOutOfBoundsException if <code>startIndex</code> &lt; 0
     *                                   or <code>endIndex</code> &lt; 0
     *                                   or <code>startIndex</code> &gt; <code>noOfSections</code>
     *                                   or <code>endIndex</code> &gt; <code>noOfSections</code>
     */
    public Bounds section(int startIndex, int endIndex, int noOfSections) {
        if (noOfSections < 1) {
            throw new IllegalArgumentException("noOfSections");
        }
        if (startIndex < 0 || startIndex > noOfSections) {
            throw new IndexOutOfBoundsException();
        }
        if (endIndex < 0 || endIndex > noOfSections) {
            throw new IndexOutOfBoundsException();
        }
        if (endIndex < startIndex) {
            throw new IllegalArgumentException("startIndex has to be smaller than or equal to startIndex");
        }
        if (noOfSections == 1 && startIndex == 0 && endIndex == 1) {
            return this;
        }
        return new Bounds(
                left + (startIndex * (right - left) / noOfSections),
                top,
                left + (int) Math.ceil(endIndex * (right - left) / (double) noOfSections),
                bottom
        );
    }

    /**
     * Returns an HOCR-compatible representation of these bounds.
     * The format is: <code>bbox LEFT TOP RIGHT BOTTOM</code>
     *
     * @return HOCR-compatible string
     */
    @Nonnull
    public String toHocrSpec() {
        return "bbox " + left + " " + top + " " + right + " " + bottom;
    }

    /**
     * Returns an ImageMagick-compatible representation of these bounds.
     * The format is: <code>WIDTHxHEIGHT+LEFT+TOP</code>
     *
     * @return ImageMagick-compatible string
     */
    @Nonnull
    @Override
    public String toString() {
        return getWidth() + "x" + getHeight() +
                (left >= 0 ? "+" : 0) + left +
                (top >= 0 ? "+" : "") + top;
    }

    /**
     * Checks if two sets of bounds touch each other.
     *
     * @param b other bounds
     * @return <code>true</code> if they touch, <code>false</code> otherwise
     * @see Bounds#cuts(Bounds)
     */
    public boolean touches(Bounds b) {
        return b != null && top <= b.bottom && b.top <= bottom && left <= b.right && b.left <= right;
    }

    /**
     * Returns bounds created from these bounds,
     * optionally trimmed from below to the bottom edge of the given limiting bounds.
     * <br/>
     * Interprets <code>null</code> as empty bounds, returning <code>null</code>.
     *
     * @param limitingBounds bounds with the new bottom edge
     * @return new bounds, or <code>null</code> if the trimmed bounds are empty
     */
    @Nullable
    public Bounds trimFromBottom(@Nullable Bounds limitingBounds) {
        if (limitingBounds == null) {
            return null;
        }
        return trimFromBottom(limitingBounds.getBottom());
    }

    /**
     * Returns bounds created from these bounds,
     * optionally trimmed from below to the new bottom edge.
     *
     * @param newEdge y coordinate of the new bottom edge
     * @return new bounds, or <code>null</code> if the trimmed bounds are empty
     */
    @Nullable
    public Bounds trimFromBottom(int newEdge) {
        if (newEdge < top) {
            return null;
        }
        if (newEdge >= bottom) {
            return this;
        }
        return new Bounds(left, top, right, newEdge);
    }

    /**
     * Returns bounds created from these bounds,
     * optionally trimmed from the left to the left edge of the given limiting bounds.
     * <br/>
     * Interprets <code>null</code> as empty bounds, returning <code>null</code>.
     *
     * @param limitingBounds bounds with the new left edge
     * @return new bounds, or <code>null</code> if the trimmed bounds are empty
     */
    @Nullable
    public Bounds trimFromLeft(@Nullable Bounds limitingBounds) {
        if (limitingBounds == null) {
            return null;
        }
        return trimFromLeft(limitingBounds.getLeft());
    }

    /**
     * Returns bounds created from these bounds,
     * optionally trimmed from the left to the new left edge.
     *
     * @param newEdge x coordinate of the new left edge
     * @return new bounds, or <code>null</code> if the trimmed bounds are empty
     */
    @Nullable
    public Bounds trimFromLeft(int newEdge) {
        if (newEdge > right) {
            return null;
        }
        if (newEdge <= left) {
            return this;
        }
        return new Bounds(newEdge, top, right, bottom);
    }

    /**
     * Returns bounds created from these bounds,
     * optionally trimmed from the right to the right edge of the given limiting bounds.
     * <br/>
     * Interprets <code>null</code> as empty bounds, returning <code>null</code>.
     *
     * @param limitingBounds bounds with the new right edge
     * @return new bounds, or <code>null</code> if the trimmed bounds are empty
     */
    @Nullable
    public Bounds trimFromRight(@Nullable Bounds limitingBounds) {
        if (limitingBounds == null) {
            return null;
        }
        return trimFromRight(limitingBounds.getRight());
    }

    /**
     * Returns bounds created from these bounds,
     * optionally trimmed from the right to the new right edge.
     *
     * @param newEdge x coordinate of the new right edge
     * @return new bounds, or <code>null</code> if the trimmed bounds are empty
     */
    @Nullable
    public Bounds trimFromRight(int newEdge) {
        if (newEdge < left) {
            return null;
        }
        if (newEdge >= right) {
            return this;
        }
        return new Bounds(left, top, newEdge, bottom);
    }

    /**
     * Returns bounds created from these bounds,
     * optionally trimmed from above to the top edge of the given limiting bounds.
     * <br/>
     * Interprets <code>null</code> as empty bounds, returning <code>null</code>.
     *
     * @param limitingBounds bounds with the new top edge
     * @return new bounds, or <code>null</code> if the trimmed bounds are empty
     */
    @Nullable
    public Bounds tripFromTop(@Nullable Bounds limitingBounds) {
        if (limitingBounds == null) {
            return null;
        }
        return trimFromBottom(limitingBounds.getBottom());
    }

    /**
     * Returns bounds created from these bounds,
     * optionally trimmed from above to the new top edge.
     *
     * @param newEdge y coordinate of the new top edge
     * @return new bounds, or <code>null</code> if the trimmed bounds are empty
     */
    @Nullable
    public Bounds tripFromTop(int newEdge) {
        if (newEdge > bottom) {
            return null;
        }
        if (newEdge <= top) {
            return this;
        }
        return new Bounds(left, newEdge, right, bottom);
    }

    /**
     * Creates new bounds that have the same y coordinates as these ones,
     * but have the x coordinates contained in both x coordinates of these bounds
     * as the coordinates of the other bounds (<code>heightSource</code>).
     * <br/>
     * Interprets <code>null</code> as empty bounds, returning <code>null</code>.
     *
     * @param heightSource bounds with interesting y coordinates
     * @return bounds with trimmed height, or <code>null</code> if they would be empty
     */
    @Nullable
    public Bounds trimHeight(@Nullable Bounded heightSource) {
        if (heightSource == null) {
            return null;
        }
        if (heightSource == this) {
            return this;
        }
        return new Bounds(
                left,
                max(heightSource.getBounds().top, top),
                right,
                min(heightSource.getBounds().bottom, bottom));
    }

    /**
     * Creates new bounds that have the same y coordinates as these ones,
     * but have the x coordinates contained in both x coordinates of these bounds
     * as the coordinates of the other bounds (<code>widthSource</code>).
     * <br/>
     * Interprets <code>null</code> as empty bounds, returning <code>null</code>.
     *
     * @param widthSource bounds with interesting x coordinates
     * @return bounds with trimmed width, or <code>null</code> if they would be empty
     */
    @Nullable
    public Bounds trimWidth(@Nullable Bounded widthSource) {
        if (widthSource == null) {
            return null;
        }
        if (widthSource == this) {
            return this;
        }
        return new Bounds(
                max(widthSource.getBounds().left, left),
                top,
                min(widthSource.getBounds().right, right),
                bottom);
    }

    /**
     * A union of bounds.
     * <br/>
     * Interprets <code>null</code> as empty bounds, returning these bounds unmodified.
     * <br/>
     * A union of two bounding rectangles
     * is the smallest bounding rectangle that contains both the input rectangles.
     *
     * @param b other bounds
     * @return union of bounds
     * @see Bounds#nullSafeUnion(Bounds, Bounds)
     */
    public Bounds union(Bounds b) {
        if (b == null) {
            return this;
        }
        if (b == this) {
            return this;
        }
        return new Bounds(min(left, b.left), min(top, b.top), max(right, b.right), max(bottom, b.bottom));
    }

    /**
     * Returns a copy of these bounds with a new bottom edge.
     *
     * @param newBottom y coordinate of the bottom edge
     * @return bounds with new edge
     */
    @Nonnull
    public Bounds withBottom(int newBottom) {
        return new Bounds(left, top, right, newBottom);
    }

    /**
     * Returns a copy of these bounds with a new left edge.
     *
     * @param newLeft x coordinate of the left edge
     * @return bounds with new edge
     */
    @Nonnull
    public Bounds withLeft(int newLeft) {
        return new Bounds(newLeft, top, right, bottom);
    }

    /**
     * Returns a copy of these bounds with a new right edge.
     *
     * @param newRight x coordinate of the right edge
     * @return bounds with new edge
     */
    @Nonnull
    public Bounds withRight(int newRight) {
        return new Bounds(left, top, newRight, bottom);
    }

    /**
     * Returns a copy of these bounds with a new top edge.
     *
     * @param newTop y coordinate of the top edge
     * @return bounds with new edge
     */
    @Nonnull
    public Bounds withTop(int newTop) {
        return new Bounds(left, newTop, right, bottom);
    }
}
