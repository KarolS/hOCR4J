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
import javax.annotation.concurrent.Immutable;

/**
 * Represents an area in the OCR'd document.
 *
 * Corresponding hOCR class: <code>ocr_carea</code>.
 */
@Immutable
public class Area extends DelegatingUnmodifiableList<Paragraph> implements Bounded {
    private final Bounds bounds;
    private final List<Paragraph> paragraphs;

    /**
     * Creates an area from the corresponding HOCR &lt;div&gt; tag
     * @param e HOCR tag
     * @throws IllegalArgumentException if not a valid &lt;div&gt; tag
     */
    public Area(@Nonnull HocrElement e) {
        paragraphs = new ArrayList<Paragraph>();
        if (e instanceof HocrTag) {
            HocrTag tag = (HocrTag) e;
            if (tag.name.equals("div")) {
                for (HocrElement k : tag.elements) {
                    if (k.isNotBlank()) {
                        paragraphs.add(new Paragraph(k));
                    }
                }
                Bounds b = Bounds.fromHocrTitleValue(tag.title);
                if (b == null) {
                    b = Bounds.ofAll(paragraphs);
                }
                bounds = b;
                return;
            }
        }
        throw new IllegalArgumentException(e.mkString());
    }

    /**
     * Creates an area containing given lines.
     * The area bounds are calculated.
     *
     * @param p list of paragraphs (not empty)
     */
    public Area(@Nonnull List<Paragraph> p) {
        if (p.isEmpty()) throw new IllegalArgumentException();
        paragraphs = new ArrayList<Paragraph>(p);
        bounds = Bounds.ofAll(p);
    }

    /**
     * Creates an area containing given paragraphs.
     *
     * @param p list of paragraphs
     * @param b bounds of the paragraph
     */
    public Area(@Nonnull List<Paragraph> p, Bounds b) {
        paragraphs = new ArrayList<Paragraph>(p);
        bounds = b;
    }

    private Area(Bounds b) {
        paragraphs = new ArrayList<Paragraph>();
        bounds = b;
    }

    private Area(Void v, @Nonnull List<Paragraph> p) {
        paragraphs = p;
        bounds = Bounds.ofAll(paragraphs);
    }

    private Area(Void v, @Nonnull List<Paragraph> p, Bounds b) {
        paragraphs = p;
        bounds = b;
    }

    /**
     * Creates copy of this area containing
     * only the words that are contained in given rectangle.
     * @param rectangle bounding rectangle
     * @return area cropped to the bounding rectangle
     */
    @Nonnull
    public Area createBounded(@Nonnull Bounds rectangle) {
        Area a = new Area(bounds.intersection(rectangle));
        for (Paragraph p : paragraphs) {
            Paragraph p2 = p.createBounded(rectangle);
            if (!p2.isBlank()) {
                a.paragraphs.add(p2);
            }
        }
        return a;
    }

    /**
     * Creates copy of this area containing
     * only the lines that are contained in and/or touch given rectangle.
     * @param rectangle bounding rectangle
     * @return area with lines touching the bounding rectangle
     */
    @Nonnull
    public Area createTouching(@Nonnull Bounds rectangle) {
        ArrayList<Paragraph> result = new ArrayList<Paragraph>();
        for (Paragraph p : paragraphs) {
            Paragraph p2 = p.createTouching(rectangle);
            if (!p2.isBlank()) {
                result.add(p2);
            }
        }
        return new Area(null, result);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Area other = (Area) obj;
        return ObjectUtils.equals(this.paragraphs, other.paragraphs) && ObjectUtils.equals(this.bounds, other.bounds);
    }

    /**
     * Finds a line that satisfies given predicate and according to the given comparator is the "largest".
     * If not found, returns <code>null</code>.
     * @param comparatorForMaximizing comparator to choose the "largest" line
     * @param predicate predicate the found line has to satisfy
     * @return a line that satisfies the predicate, or <code>null</code> if there are none
     */
    @Nullable
    public Line findLine(@Nonnull Comparator<Line> comparatorForMaximizing, @Nonnull Predicate<Line> predicate) {
        Line result = null;
        for (Paragraph p : paragraphs) {
            Line l = p.findLine(comparatorForMaximizing, predicate);
            if (l != null) {
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
     * Calculates the number of lines in this area.
     * @return number of lines
     */
    public int getLineCount() {
        int sum = 0;
        for (Paragraph p : paragraphs) {
            sum += p.getLineCount();
        }
        return sum;
    }

    /**
     * Calculates the number of paragraphs in this area.
     * @return number of paragraphs
     */
    public int getParagraphCount() {
       return paragraphs.size();
    }

    @Override
    protected List<Paragraph> getUnderlying() {
        return paragraphs;
    }

    /**
     * Calculates the number of words in this area, calculated as the sum of numbers of words in all paragraphs.
     * @return number of words
     */
    public int getWordCount() {
        int sum = 0;
        for (Paragraph p : paragraphs) {
            sum += p.getWordCount();
        }
        return sum;
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hashCodeMulti(paragraphs, bounds);
    }

    /**
     * Checks if this area is blank, i.e. all of its paragraphs are blank.
     * @return <code>true</code> if this area is blank, <code>false</code> otherwise
     */
    public boolean isBlank() {
        for (Paragraph p : paragraphs) {
            if (!p.isBlank()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a new area with all paragraphs modified by the given function.
     * Bounds are recalculated unless this area contains no paragraphs.
     * @param f paragraph-modifying function
     * @return modified area
     */
    @Nonnull
    public Area map(@Nonnull Function<Paragraph, Paragraph> f) {
        List<Paragraph> paragraphList = CollectionUtils.listMap(paragraphs, f);
        if(paragraphList.isEmpty()) {
            return new Area(null, paragraphList, bounds);
        } else {
            return new Area(null, paragraphList);
        }
    }

    /**
     * Creates a new area with all bounds modified by the given function.
     * Bounds are recalculated unless this area contains no paragraphs;
     * If there are no paragraphs, the bounds of this area
     * are modified using the given function.
     * @param f bounds-modifying function
     * @return modified area
     */
    @Nonnull
    public Area mapBounds(@Nonnull final Function<Bounds,Bounds> f) {
        List<Paragraph> paragraphList = CollectionUtils.listMap(paragraphs, new Function<Paragraph, Paragraph>() {
            @Nullable
            public Paragraph apply(@Nullable Paragraph paragraph) {
                assert paragraph != null;
                return paragraph.mapBounds(f);
            }
        });
        if(paragraphList.isEmpty()) {
            return new Area(null, paragraphList, f.apply(bounds));
        } else {
            return new Area(null, paragraphList);
        }
    }

    @Nonnull
    @Override
    public Area subList(int i, int j) {
        return new Area(paragraphs.subList(i, j));
    }

    @Nonnull
    @Override
    public String toString() {
        return "AREA: " + paragraphs;
    }

    /**
     * Translates the area by given vector.
     * @param dx x displacement
     * @param dy y displacement
     * @return translated area
     */
    public Area translate(int dx, int dy) {
        List<Paragraph> ps = new ArrayList<Paragraph>(paragraphs.size());
        for(Paragraph p: paragraphs){
            ps.add(p.translate(dx,dy));
        }
        return new Area(null, ps, bounds.translate(dx,dy));
    }
}
