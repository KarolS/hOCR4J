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
import io.github.karols.hocr4j.dom.HocrParser;
import io.github.karols.hocr4j.dom.HocrTag;
import io.github.karols.hocr4j.utils.CollectionUtils;
import io.github.karols.hocr4j.utils.DelegatingUnmodifiableList;
import io.github.karols.hocr4j.utils.TextUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Page extends DelegatingUnmodifiableList<Area> implements Bounded {

    /**
     * Creates a list of pages from a list of HOCR documents.
     * A document can contain several pages.
     * The pages are numbered consecutively, starting from 1.
     * @param hocr list of HOCR documents
     * @return list of pages
     */
    public static List<Page> fromHocr(@Nonnull List<String> hocr) {
        List<Page> pages = new ArrayList<Page>();
        int pageNo = 1;
        for (String h : hocr) {
            List<HocrElement> parseResult = HocrParser.createAst(h);
            List<Page> ps = HocrParser.parse(parseResult, pageNo);
            pages.addAll(ps);
            pageNo += ps.size();
        }
        return pages;
    }

    /**
     * Creates a list of pages with new page numbers
     * @param startFrom new page number for the first page
     * @param pages list of pages
     * @return list of pages with new page numbers
     */
    @Nonnull
    public static List<Page> changePageNumbers(int startFrom, @Nonnull List<Page> pages){
        List<Page> result = new ArrayList<Page>();
        int pageNo = startFrom;
        for(Page p: pages) {
            result.add(p.changePageNumber(pageNo));
            pageNo ++;
        }
        return result;
    }

    /**
     * Creates a list of pages with new page numbers
     * @param startFrom new page number for the first page
     * @param pages pages
     * @return list of pages with new page numbers
     */
    @Nonnull
    public static List<Page> changePageNumbers(int startFrom, @Nonnull Page... pages){
        List<Page> result = new ArrayList<Page>();
        int pageNo = startFrom;
        for(Page p: pages) {
            result.add(p.changePageNumber(pageNo));
            pageNo ++;
        }
        return result;
    }

    private final List<Area> areas;
    private final Bounds bounds;
    private final int pageNo;

    /**
     * Creates a page from the corresponding HOCR &lt;div&gt; tag
     *
     * @param pageNo page number
     * @param e HOCR tag
     * @throws IllegalArgumentException if not a valid &lt;div&gt; tag
     */
    public Page(int pageNo, @Nonnull HocrElement e) {
        this.pageNo = pageNo;
        areas = new ArrayList<Area>();
        if (e instanceof HocrTag) {
            HocrTag tag = (HocrTag) e;
            if (tag.name.equals("div")) {
                for (HocrElement k : tag.elements) {
                    if (k.isNotBlank()) {
                        areas.add(new Area(k));
                    }
                }
                Bounds b = Bounds.fromHocrTitleValue(tag.title);
                if (b == null) {
                    b = Bounds.ofAll(areas);
                }
                bounds = b;
                return;
            }
        }
        throw new IllegalArgumentException(e.mkString());
    }

    /**
     * Creates a page containing given areas.
     * The page bounds are calculated.
     * @param pageNo page number
     * @param a list of areas (not empty)
     */
    public Page(int pageNo, @Nonnull List<Area> a) {
        if (a == null || a.isEmpty()) throw new IllegalArgumentException();
        this.pageNo = pageNo;
        areas = new ArrayList<Area>(a);
        bounds = Bounds.ofAll(a);
    }

    /**
     * Creates a page containing given areas.
     * @param pageNo page number
     * @param a list of areas (can be empty)
     * @param b bounds of the page
     */
    public Page(int pageNo, @Nonnull List<Area> a, Bounds b) {
        if (a == null) throw new IllegalArgumentException();
        this.pageNo = pageNo;
        areas = new ArrayList<Area>(a);
        bounds = b;
    }

    private Page(Void v, int pageNo, @Nonnull List<Area> a) {
        if (a == null || a.isEmpty()) throw new IllegalArgumentException();
        this.pageNo = pageNo;
        areas = a;
        bounds = Bounds.ofAll(a);
    }

    private Page(Void v, int pageNo, Bounds b) {
        this.pageNo = pageNo;
        areas = new ArrayList<Area>();
        bounds = b;
    }

    private Page(Void v, int pageNo, @Nonnull List<Area> a, Bounds b) {
        if (a == null) throw new IllegalArgumentException();
        this.pageNo = pageNo;
        areas = a;
        bounds = b;
    }

    /**
     * Returns a copy of this page with all tiny print removed.
     * If the page contains less that 10 words, nothing is removed.
     * Tiny print is defined as having height lower than
     * 1/6th of median word height, or 1/12th if the word is smaller.
     * @return page without tiny print
     * @see TextUtils#isSmaller(String)
     */
    @Nonnull
    public Page cleanTinyPrint() {
        List<Integer> heightList = new ArrayList<Integer>();
        int bad = 0;
        for (Word w : getAllWords()) {
            Bounds b = w.getBounds();
            if (b != null) {
                heightList.add(b.getHeight());
            } else {
                bad++;
            }
        }
        if (heightList.size() < 10) {
            return this;
        }
        if (bad > heightList.size()) {
            return this;
        }

        Collections.sort(heightList);
        int medianHeight = heightList.get(heightList.size() / 2);
        final int cutoffHeight = medianHeight / 6;

        final Predicate<Word> requirement =
                new Predicate<Word>() {
                    public boolean apply(Word input) {
                        if (TextUtils.isSmaller(input.getText())) {
                            return input.getBounds().getHeight() * 2 > cutoffHeight;
                        } else {
                            return input.getBounds().getHeight() > cutoffHeight;
                        }
                    }
                };
        final Function<Line, Line> lineCleaner =
                new Function<Line, Line>() {
                    public Line apply(@Nullable Line line) {
                        if (line == null) throw new IllegalStateException();
                        return new Line(CollectionUtils.listFilter(line, requirement), line.bounds);
                    }
                };
        final Function<Paragraph, Paragraph> paragraphCleaner =
                new Function<Paragraph, Paragraph>() {
                    public Paragraph apply(@Nullable Paragraph paragraph) {
                        if (paragraph == null) throw new IllegalStateException();
                        return paragraph.map(lineCleaner);
                    }
                };
        final Function<Area, Area> areaCleaner =
                new Function<Area, Area>() {
                    public Area apply(@Nullable Area area) {
                        if (area == null) throw new IllegalStateException();
                        return area.map(paragraphCleaner);
                    }
                };
        return map(areaCleaner);
    }

    /**
     * Creates copy of this page containing
     * only the words that are contained in given rectangle.
     * @param rectangle bounding rectangle
     * @return page cropped to the bounding rectangle
     */
    @Nonnull
    public Page createBounded(@Nonnull Bounds rectangle) {
        Page p = new Page((Void) null, pageNo, bounds.intersection(rectangle));
        for (Area a : areas) {
            Area a2 = a.createBounded(rectangle);
            if (!a2.isBlank()) {
                p.areas.add(a2);
            }
        }
        return p;
    }

    /**
     * Creates copy of this page containing
     * only the lines that are contained in and/or touch given rectangle.
     * @param rectangle bounding rectangle
     * @return page with lines touching the bounding rectangle
     */
    @Nonnull
    public Page createTouching(@Nonnull Bounds rectangle) {
        ArrayList<Area> result = new ArrayList<Area>();
        for (Area a : areas) {
            Area a2 = a.createTouching(rectangle);
            if (!a2.isBlank()) {
                result.add(a2);
            }
        }
        return new Page(null, pageNo, result);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Page other = (Page) obj;
        return ObjectUtils.equals(this.areas, other.areas) && ObjectUtils.equals(this.bounds, other.bounds);
    }

    /**
     * Finds all lines that satisfy the given predicate.
     * The line order is unspecified.
     * @param predicate predicate to satisfy
     * @return lines that satisfy the predicate
     */
    @Nonnull
    public List<Line> findAllLines(@Nonnull Predicate<Line> predicate) {
        ArrayList<Line> result = new ArrayList<Line>();
        for (Area a : areas) {
            for (Paragraph p : a) {
                for (Line l : p) {
                    if (predicate.apply(l)) {
                        result.add(l);
                    }
                }
            }
        }
        return result;
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
        for (Area a : areas) {
            Line l = a.findLine(comparatorForMaximizing, predicate);
            if (l != null) {
                if (result == null || comparatorForMaximizing.compare(l, result) > 0) {
                    result = l;
                }
            }
        }
        return result;
    }

    /**
     * Finds a line in this page that when applied to the given function
     * gives the highest <b>non-negative</b> result.
     * Lines that yield negative or <code>null</code> result are considered not matching.
     * If no line is found, returns <code>null</code>.
     * @param scoreFunction score function
     * @return
     * a line with the highest non-negative score,
     * or <code>null</code> if none found
     */
    @Nullable
    public Line findLineMaximizing(@Nonnull Function<Line, Double> scoreFunction) {
        return findLineMaximizingImpl(scoreFunction, null, false);
    }

    /**
     * Finds a line in this page that when applied to the given function
     * gives one of the highest <b>non-negative</b> results,
     * biased towards lines that are closer to <code>header</code>.
     * Lines that yield negative or <code>null</code> result are considered not matching.
     * If no line is found, returns <code>null</code>.
     * @param scoreFunction score function
     * @param header object in proximity of which the result is preferred
     * @return
     * a line with one of the highest non-negative scores near <code>header</code>,
     * or <code>null</code> if none found
     */
    @Nullable
    public Line findLineMaximizingCloseTo(Function<Line, Double> scoreFunction, @Nonnull Bounds header) {
        return findLineMaximizingImpl(scoreFunction, header, false);
    }

    @Nullable
    private Line findLineMaximizingImpl(@Nonnull Function<Line, Double> scoreFunction, @Nullable Bounded header, boolean slightlyBelow) {
        Line result = null;
        double maxScore = Double.NEGATIVE_INFINITY;
        for (Area a : areas) {
            for (Paragraph p : a) {
                for (Line l : p) {
                    Double thisScore = scoreFunction.apply(l);
                    if (thisScore == null || thisScore < 0) {
                        continue;
                    }
                    if (header != null) {
                        thisScore += 0.1;
                        double multiplier =
                                header.getBounds().distance(l.bounds) +
                                        bounds.getHeight() / 10.0;
                        multiplier = Math.sqrt(multiplier);
                        multiplier = Math.sqrt(multiplier);
                        thisScore /= multiplier;
                        if (slightlyBelow && header.getBounds().isBelow(l.bounds)) {
                            thisScore /= 2;
                        }
                    }
                    if (thisScore > maxScore) {
                        result = l;
                        maxScore = thisScore;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Finds a line in this page that when applied to the given function
     * gives one of the highest <b>non-negative</b> results,
     * biased towards lines that are close to and below <code>header</code>.
     * Lines that yield negative or <code>null</code> result are considered not matching.
     * If no line is found, returns <code>null</code>.
     * @param scoreFunction score function
     * @param header object in proximity of which the result is preferred
     * @return
     * a line with one of the highest non-negative scores near <code>header</code>,
     * or <code>null</code> if none found
     */
    @Nullable
    public Line findLineMaximizingPreferablySlightlyBelow(@Nonnull Function<Line, Double> scoreFunction, @Nonnull Bounds header) {
        return findLineMaximizingImpl(scoreFunction, header, true);
    }

    /**
     * Finds a line in this page that when applied to the given function
     * gives the lowest <b>non-negative</b> result.
     * Lines that yield negative or <code>null</code> result are considered not matching.
     * If no line is found, returns <code>null</code>.
     * @param scoreFunction score function
     * @return
     * a line with the lowest non-negative score,
     * or <code>null</code> if none found
     */
    @Nullable
    public Line findLineMinimizing(@Nonnull Function<Line, Double> scoreFunction) {
        return findLineMinimizingImpl(scoreFunction, null, false);
    }

    /**
     * Finds a line in this page that when applied to the given function
     * gives one of the lowest <b>non-negative</b> results,
     * biased towards lines that are closer to <code>header</code>.
     * Lines that yield negative or <code>null</code> result are considered not matching.
     * If no line is found, returns <code>null</code>.
     * @param scoreFunction score function
     * @param header object in proximity of which the result is preferred
     * @return
     * a line with one of the lowest non-negative scores near <code>header</code>,
     * or <code>null</code> if none found
     */
    @Nullable
    public Line findLineMinimizingCloseTo(Function<Line, Double> scoreFunction, @Nonnull Bounds header) {
        return findLineMinimizingImpl(scoreFunction, header, false);
    }

    @Nullable
    private Line findLineMinimizingImpl(@Nonnull Function<Line, Double> scoreFunction, @Nullable Bounded header, boolean slightlyBelow) {
        Line result = null;
        double minScore = Double.POSITIVE_INFINITY;
        for (Area a : areas) {
            for (Paragraph p : a) {
                for (Line l : p) {
                    Double thisScore = scoreFunction.apply(l);
                    if (thisScore == null || thisScore < 0) {
                        continue;
                    }
                    if (header != null) {
                        thisScore += 0.1;
                        double multiplier =
                                header.getBounds().distance(l.bounds) +
                                        bounds.getHeight() / 10.0;
                        multiplier = Math.sqrt(multiplier);
                        multiplier = Math.sqrt(multiplier);
                        thisScore *= multiplier;
                        if (slightlyBelow && header.getBounds().isBelow(l.bounds)) {
                            thisScore *= 2;
                        }
                    }
                    if (thisScore < minScore) {
                        result = l;
                        minScore = thisScore;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Finds a line in this page that when applied to the given function
     * gives one of the lowest <b>non-negative</b> results,
     * biased towards lines that are close to and below <code>header</code>.
     * Lines that yield negative or <code>null</code> result are considered not matching.
     * If no line is found, returns <code>null</code>.
     * @param scoreFunction score function
     * @param header object in proximity of which the result is preferred
     * @return
     * a line with one of the lowest non-negative scores near <code>header</code>,
     * or <code>null</code> if none found
     */
    @Nullable
    public Line findLineMinimizingPreferablySlightlyBelow(@Nonnull Function<Line, Double> scoreFunction, @Nonnull Bounds header) {
        return findLineMinimizingImpl(scoreFunction, header, true);
    }

    /**
     * Returns the list of all lines in the page
     * in the natural left-to-right reading order.
     * @return all lines
     */
    @Nonnull
    public List<Line> getAllLines() {
        ArrayList<Line> result = new ArrayList<Line>();
        for (Area a : areas) {
            for (Paragraph p : a) {
                result.addAll(p);
            }
        }
        Collections.sort(result, OrderedBy.flowOrder());
        return result;
    }

    /**
     * Returns the list of all lines in the page
     * in the natural left-to-right reading order,
     * converted to mutable lists of words.
     * @return all lines as lists of words
     */
    @Nonnull
    public List<List<Word>> getAllLinesAsListsOfWords() {
        ArrayList<Line> result = new ArrayList<Line>();
        for (Area a : areas) {
            for (Paragraph p : a) {
                result.addAll(p);
            }
        }
        Collections.sort(result, OrderedBy.flowOrder());
        return CollectionUtils.listMap(result, Line.GET_WORDS);
    }

    /**
     * Returns the list of all lines in the page
     * in the natural left-to-right reading order,
     * converted to strings.
     * @return all lines as strings
     * @see Line#mkString()
     */
    @Nonnull
    public List<String> getAllLinesAsStrings() {
        ArrayList<Line> result = new ArrayList<Line>();
        for (Area a : areas) {
            for (Paragraph p : a) {
                result.addAll(p);
            }
        }
        Collections.sort(result, OrderedBy.flowOrder());
        return CollectionUtils.listMap(result, Line.MK_STRING);
    }

    /**
     * Returns the list of all words in the page.
     * The order is unspecified.
     * @return list of all the words
     */
    @Nonnull
    public List<Word> getAllWords() {
        ArrayList<Word> words = new ArrayList<Word>();
        for (Area a : areas) {
            for (Paragraph p : a) {
                for (Line l : p) {
                    for (Word w : l.words) {
                        words.add(w);
                    }
                }
            }
        }
        return words;
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    /**
     * Returns height of this page, or -1 if not known.
     *
     * @return height of this page
     */
    public double getHeight() {
        if (bounds != null) {
            return bounds.getHeight();
        }
        return -1;
    }

    /**
     * Calculates the number of lines in this page.
     *
     * @return number of lines
     */
    public int getLineCount() {
        int sum = 0;
        for (Area a : areas) {
            sum += a.getLineCount();
        }
        return sum;
    }

    /**
     * Returns the number of this page.
     * @return page number
     */
    public int getPageNo() {
        return pageNo;
    }

    /**
     * Calculates the number of paragraphs in this page.
     *
     * @return number of paragraphs
     */
    public int getParagraphCount() {
        int sum = 0;
        for (Area a : areas) {
            sum += a.getParagraphCount();
        }
        return sum;
    }

    @Override
    protected List<Area> getUnderlying() {
        return areas;
    }

    /**
     * Returns width of this page, or -1 if not known.
     *
     * @return width of this page
     */
    public double getWidth() {
        if (bounds != null) {
            return bounds.getWidth();
        }
        return -1;
    }

    /**
     * Calculates the number of words in this page,
     * calculated as the sum of numbers of words in all areas.
     *
     * @return number of words
     */
    public int getWordCount() {
        int sum = 0;
        for (Area a : areas) {
            sum += a.getWordCount();
        }
        return sum;
    }

    /**
     * Creates bounds for the entire left-aligned column
     * given bounds that span its entire height.
     * The resulting bounds do not cut words.
     * If there are few lines longer than the rest of the column,
     * the bounds are expanded to the right until they meet the page edge
     * or another column of text.
     * May behave unexpectedly if the column has only few lines.
     * @param b bounds that span the entire height of the column
     * @return bounds of the column
     * @see Page#growBoundsUntilTheyStopCuttingWords(Bounds)
     */
    @Nullable
    public Bounds growBoundsForLeftAlignedColumn(@Nullable final Bounds b) {
        if (b == null) {
            return null;
        }
        Bounds rect = growBoundsUntilTheyStopCuttingWords(b);
        assert rect != null;
        List<Line> lines = findAllLines(LineThat.hasWordsIntersecting(rect));
        double sum = 0;
        int count = 0;
        for (Line l : lines) {
            for (int i = 1; i < l.words.size(); i++) {

                Word leftE = l.words.get(i - 1);
                Word rightE = l.words.get(i);
                if (leftE.getBounds().in(rect) && rightE.getBounds().in(rect)) {
                    sum += rightE.getBounds().getLeft() - leftE.getBounds().getRight();
                    count++;
                }
            }
        }
        double spaceWidth = sum / count;
        Bounds movingEdge = rect.getRightEdge();
        Bounds nearTheRightEdge = movingEdge.moveToTheLeft((int) (spaceWidth / 4));
        int noofLongLines = findAllLines(LineThat.hasWordsIntersecting(nearTheRightEdge)).size();
        if (noofLongLines < 2) {
            noofLongLines = 2;
        }
        Bounds lastGoodPosition = movingEdge;
        int step = (int) (spaceWidth / 2);
        if (step < 1) {
            step = 1;
        }
        while (movingEdge.in(this.bounds) && movingEdge.getLeft() - rect.getRight() < rect.getWidth()) {
            movingEdge = movingEdge.moveToTheRight(step);
            int cutLinesCount = findAllLines(LineThat.hasWordsIntersecting(movingEdge)).size();
            if (cutLinesCount > noofLongLines) {
                break;
            }
            if (cutLinesCount == 0) {
                lastGoodPosition = movingEdge;
            }
        }
        // TODO: move left edge until it gets into the gap between columns
        return rect.union(lastGoodPosition);
    }

    /**
     * Returns the smallest possible bounds
     * that contain the given bounds and do not cut any word,
     * Bounds cut a word if the word is neither inside the bounds nor outside the bounds.
     * Returns <code>null</code> if given <code>null</code>.
     * @param b bounds to expand
     * @return expanded bounds, or <code>null</code> if <code>b</code> is <code>null</code>
     */
    @Nullable
    public Bounds growBoundsUntilTheyStopCuttingWords(@Nullable final Bounds b) {
        if (b == null) {
            return null;
        }
        List<Word> words = getAllWords();
        boolean modified = true;
        Bounds rect = b;
        while (modified) {
            modified = false;
            for (Word w : words) {
                if (rect.cuts(w.getBounds())) {
                    rect = rect.union(w.getBounds());
                    modified = true;
                }
            }
        }
        return rect;
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hashCodeMulti(areas, bounds);
    }

    /**
     * Checks if this page is blank, i.e. all of its areas are blank.
     *
     * @return <code>true</code> if this page is blank, <code>false</code> otherwise
     */
    public boolean isBlank() {
        for (Area a : areas) {
            if (!a.isBlank()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a new page with all areas modified by the given function.
     * Bounds are recalculated unless this page contains no areas.
     * @param f area-modifying function
     * @return modified page
     */
    @Nonnull
    public Page map(@Nonnull Function<Area, Area> f) {
        List<Area> areaList = CollectionUtils.listMap(areas, f);
        if (areaList.isEmpty()) {
            return new Page(null, pageNo, areaList, bounds);
        } else {
            return new Page(null, pageNo, areaList);
        }
    }

    /**
     * Creates a new page with all bounds modified by the given function.
     * Bounds are recalculated unless this page contains no areas;
     * If there are no areas, the bounds of this page
     * are modified using the given function.
     * @param f bounds-modifying function
     * @return modified page
     */
    @Nonnull
    public Page mapBounds(@Nonnull final Function<Bounds, Bounds> f) {
        List<Area> areaList = CollectionUtils.listMap(areas, new Function<Area, Area>() {
            @Nullable
            public Area apply(@Nullable Area area) {
                assert area != null;
                return area.mapBounds(f);
            }
        });
        if (areaList.isEmpty()) {
            return new Page(null, pageNo, areaList, f.apply(bounds));
        } else {
            return new Page(null, pageNo, areaList);
        }
    }

    /**
     * Creates a new page with all lines modified by the given function.
     * Bounds are recalculated unless this page contains no lines.
     * @param lineFunction line-modifying function
     * @return modified paragraph
     */
    @Nonnull
    public Page mapLines(@Nonnull final Function<Line, Line> lineFunction) {
        final Function<Paragraph, Paragraph> paragraphFunction = new Function<Paragraph, Paragraph>() {
            public Paragraph apply(@Nullable Paragraph paragraph) {
                if (paragraph == null) throw new IllegalStateException();
                return paragraph.map(lineFunction);
            }
        };
        final Function<Area, Area> areaFunction =
                new Function<Area, Area>() {
                    public Area apply(@Nullable Area area) {
                        if (area == null) throw new IllegalStateException();
                        return area.map(paragraphFunction);
                    }
                };
        return new Page(pageNo, CollectionUtils.listMap(areas, areaFunction));
    }

    /**
     * Creates a copy of this page with new page number.
     * @param newPageNo new page number
     * @return copy with new page number
     */
    public Page changePageNumber(int newPageNo){
        return new Page((Void)null, newPageNo, areas, bounds);
    }

    @Nonnull
    @Override
    public Page subList(int i, int j) {
        return new Page(pageNo, areas.subList(i, j));
    }

    @Nonnull
    public String toString() {
        return "PAGE: " + areas;
    }

}
