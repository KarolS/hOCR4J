/* Copyright (c) 2014 Karol Stasiak, All Rights Reserved
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 3 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*/

package io.github.karols.hocr4j;

import io.github.karols.hocr4j.dom.HocrElement;
import io.github.karols.hocr4j.dom.HocrTag;
import io.github.karols.hocr4j.utils.CollectionUtils;
import io.github.karols.hocr4j.utils.DelegatingUnmodifiableList;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Paragraph extends DelegatingUnmodifiableList<Line> implements Bounded {

    private final Bounds bounds;
    private final List<Line> lines;

    /**
     * Creates a paragraph from the corresponding HOCR &lt;p&gt; tag
     *
     * @param e HOCR tag
     * @throws IllegalArgumentException if not a valid &lt;p&gt; tag
     */
    public Paragraph(@Nonnull HocrElement e) {
        lines = new ArrayList<Line>();
        if (e instanceof HocrTag) {
            HocrTag tag = (HocrTag) e;
            if (tag.name.equals("p")) {
                for (HocrElement k : tag.elements) {
                    if (k.isNotBlank()) {
                        lines.add(new Line(k));
                    }
                }
                Bounds b = Bounds.fromHocrTitleValue(tag.title);
                if (b == null) {
                    b = Bounds.ofAll(lines);
                }
                bounds = b;
                return;
            }
        }
        throw new IllegalArgumentException(e.mkString());
    }

    /**
     * Creates a paragraph containing given lines.
     * The paragraph bounds are calculated.
     *
     * @param l list of lines (not empty)
     */
    public Paragraph(@Nonnull List<Line> l) {
        if (l.isEmpty()) throw new IllegalArgumentException();
        lines = new ArrayList<Line>(l);
        bounds = Bounds.ofAll(l);
    }


    /**
     * Creates a paragraph containing given lines.
     *
     * @param l list of lines
     * @param b bounds of the paragraph
     */
    public Paragraph(List<Line> l, Bounds b) {
        lines = new ArrayList<Line>(l);
        bounds = b;
    }

    private Paragraph(Void v, List<Line> l) {
        if (l.isEmpty()) throw new IllegalArgumentException();
        lines = l;
        bounds = Bounds.ofAll(l);
    }

    private Paragraph(Void v, List<Line> l, Bounds b) {
        lines = l;
        bounds = b;
    }

    private Paragraph(Void v, Bounds b) {
        lines = new ArrayList<Line>();
        bounds = b;
    }

    /**
     * Creates copy of this paragraph containing
     * only the words that are contained in given rectangle.
     *
     * @param rectangle bounding rectangle
     * @return paragraph cropped to the bounding rectangle
     */
    @Nonnull
    public Paragraph createBounded(@Nonnull Bounds rectangle) {
        Paragraph p = new Paragraph((Void) null, bounds.intersection(rectangle));
        for (Line l : lines) {
            Line l2 = l.createBounded(rectangle);
            if (!l2.isBlank()) {
                p.lines.add(l2);
            }
        }
        return p;
    }

    /**
     * Creates copy of this paragraph containing
     * only the lines that are contained in and/or touch given rectangle.
     *
     * @param rectangle bounding rectangle
     * @return paragraph lines words touching the bounding rectangle
     */
    @Nonnull
    public Paragraph createTouching(Bounds rectangle) {
        Paragraph p = new Paragraph((Void) null, bounds); //TODO
        for (Line l : lines) {
            if (l.bounds != null && l.bounds.touches(rectangle)) {
                p.lines.add(l);
            }
        }
        return p;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Paragraph other = (Paragraph) obj;
        return ObjectUtils.equals(this.lines, other.lines) && ObjectUtils.equals(this.bounds, other.bounds);
    }

    /**
     * Finds a line that satisfies given predicate and according to the given comparator is the "largest".
     * If not found, returns <code>null</code>.
     *
     * @param comparatorForMaximizing comparator to choose the "largest" line
     * @param predicate               predicate the found line has to satisfy
     * @return a line that satisfies the predicate, or <code>null</code> if there are none
     */
    @Nullable
    public Line findLine(@Nonnull Comparator<Line> comparatorForMaximizing, @Nonnull Predicate<Line> predicate) {
        Line result = null;
        for (Line l : lines) {
            if (predicate.apply(l)) {
                if (result == null || comparatorForMaximizing.compare(l, result) > 0) {
                    result = l;
                }
            }
        }
        return result;
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    /**
     * Calculates the number of lines in this paragraph.
     *
     * @return number of lines
     */
    public int getLineCount() {
        return lines.size();
    }

    @Override
    protected List<Line> getUnderlying() {
        return lines;
    }

    /**
     * Calculates the number of words in this paragraph, calculated as the sum of numbers of words in all lines.
     *
     * @return number of words
     */
    public int getWordCount() {
        int sum = 0;
        for (Line l : lines) {
            sum += l.words.size();
        }
        return sum;
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hashCodeMulti(lines, bounds);
    }

    /**
     * Checks if this paragraph is blank, i.e. all of its lines are blank.
     *
     * @return <code>true</code> if this paragraph is blank, <code>false</code> otherwise
     */
    public boolean isBlank() {
        for (Line l : lines) {
            if (!l.isBlank()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a new paragraph with all lines modified by the given function.
     * Bounds are recalculated unless this paragraph contains no lines.
     * @param f line-modifying function
     * @return modified paragraph
     */
    @Nonnull
    public Paragraph map(@Nonnull Function<Line, Line> f) {
        List<Line> lineList = CollectionUtils.listMap(lines, f);
        if (lineList.isEmpty()) {
            return new Paragraph(null, lineList, bounds);
        } else {
            return new Paragraph(null, lineList);
        }
    }

    /**
     * Creates a new paragraph with all bounds modified by the given function.
     * Bounds are recalculated unless this paragraph contains no lines;
     * If there are no lines, the bounds of this paragraph
     * are modified using the given function.
     * @param f bounds-modifying function
     * @return modified paragraph
     */
    @Nonnull
    public Paragraph mapBounds(@Nonnull final Function<Bounds, Bounds> f) {
        List<Line> lineList = CollectionUtils.listMap(lines, new Function<Line, Line>() {
            @Nullable
            public Line apply(@Nullable Line line) {
                assert line != null;
                return line.mapBounds(f);
            }
        });
        if (lineList.isEmpty()) {
            return new Paragraph(null, lineList, f.apply(bounds));
        } else {
            return new Paragraph(null, lineList);
        }
    }

    @Override
    @Nonnull
    public Paragraph subList(int i, int j) {
        return new Paragraph(lines.subList(i, j));
    }

    @Nonnull
    public String toString() {
        return "PARAGRAPH: " + lines;
    }

}
