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
import io.github.karols.hocr4j.utils.TextUtils;

import com.google.common.base.Function;
import org.apache.commons.lang3.ObjectUtils;

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Represents a line in the OCR'd document.
 *
 * Corresponding hOCR class: <code>ocr_line</code>.
 */
@Immutable
public class Line extends DelegatingUnmodifiableList<Word> implements Bounded {

    /**
     * A function object for the <code>getWords</code> method.
     *
     * @see Line#getWords()
     */
    public static final Function<Line, List<Word>> GET_WORDS = new Function<Line, List<Word>>() {
        public List<Word> apply(Line input) {
            if (input == null) return Collections.emptyList();
            return input.getWords();
        }
    };

    /**
     * A function object for the <code>mkString</code> method.
     *
     * @see Line#mkString()
     */
    public static final Function<Line, String> MK_STRING = new Function<Line, String>() {
        public String apply(Line input) {
            if (input == null) return "";
            return input.mkString();
        }
    };
    final Bounds bounds;
    final List<Word> words;


    /**
     * Creates a line from the corresponding HOCR &lt;span&gt; tag
     * with class="ocr_line"
     *
     * @param e HOCR tag
     * @throws IllegalArgumentException if not a valid &lt;span&gt; tag
     */
    public Line(@Nonnull HocrElement e) {
        words = new ArrayList<Word>();
        if (e instanceof HocrTag) {
            HocrTag tag = (HocrTag) e;
            if (tag.name.equals("span") && "ocr_line".equals(tag.clazz)) {
                for (HocrElement k : tag.elements) {
                    words.add(new Word(k));
                }
                Bounds b = Bounds.fromHocrTitleValue(tag.title);
                if (b == null) {
                    b = Bounds.ofAll(words);
                }
                bounds = b;
                return;
            }
        }
        throw new IllegalArgumentException(e.mkString());
    }

    /**
     * Creates a line containing given words.
     * The line bounds are calculated.
     *
     * @param words list of words (not empty)
     */
    public Line(@Nonnull List<Word> words) {
        if (words.isEmpty()) throw new IllegalArgumentException();
        this.words = new ArrayList<Word>(words);
        this.bounds = Bounds.ofAll(words);
    }

    /**
     * Creates a line containing given words.
     *
     * @param words list of words (not empty)
     * @param b     bounds of the line
     */
    public Line(@Nonnull List<Word> words, Bounds b) {
        this.words = new ArrayList<Word>(words);
        this.bounds = b;
    }

    private Line(Void v, @Nonnull List<Word> words, Bounds b) {
        this.words = words;
        this.bounds = b;
    }

    private Line(Void v, Bounds b) {
        this.words = new ArrayList<Word>();
        this.bounds = b;
    }

    private Line(Void v, @Nonnull List<Word> words) {
        if (words.isEmpty()) throw new IllegalArgumentException();
        this.words = words;
        this.bounds = Bounds.ofAll(words);
    }

    /**
     * Creates copy of this line containing
     * only the words that are contained in given rectangle.
     * If rectangle is <code>null</code>, returns this.
     * <b>This differs from the usual interpretation of null bounds.</b>
     *
     * @param rectangle bounding rectangle
     * @return line cropped to the bounding rectangle
     */
    @Nonnull
    public Line createBounded(Bounds rectangle) {
        if (rectangle == null) {
            return this;
        }
        List<Word> resultingWords = new ArrayList<Word>();
        for (Word e : words) {
            if (e.getBounds().in(rectangle)) {
                resultingWords.add(e);
            }
        }
        return new Line(null, resultingWords, bounds.intersection(rectangle));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Line other = (Line) obj;
        return ObjectUtils.equals(this.words, other.words) && ObjectUtils.equals(this.bounds, other.bounds);
    }

    /**
     * If <code>string</code> is made from concatenating
     * several consecutive words from this line,
     * returns union of bounds of those words.
     * If not, returns <code>null</code>.
     * Words are looked up matching case.
     * Spaces are ignored. Uses <code>TextUtils.fuzzyContains</code> to match the string.
     *
     * @param string string to search for
     * @return bounds of the matching words
     * @see Word#findBoundsOfWord(java.util.List, String)
     * @see TextUtils#fuzzyContains(String, String)
     * @see TextUtils#fuzzyEqual(String, String)
     * @see MatchWords#ofList(java.util.List, String[])
     */
    public Bounds findBoundsOfWord(@Nonnull String string) {
        string = string.replace(" ", "");
        for (int i = words.size() - 1; i >= 0; i--) {
            for (int length = 1; length < words.size(); length++) {
                String fromFoundLocation = getSpacelessStringFromElements(i, length);
                if (TextUtils.fuzzyContains(fromFoundLocation, string)) {
                    return Bounds.ofAll(words.subList(i, i + length));
                }
            }
        }
        return bounds;
    }


    /**
     * If <code>string</code> is made from concatenating
     * several consecutive words from this line,
     * returns union of bounds of those words.
     * If not, returns <code>null</code>.
     * Words are looked up ignoring case.
     * Spaces are ignored. Uses <code>TextUtils.fuzzyContains</code> to match the string.
     *
     * @param string string to search for
     * @return bounds of the matching words
     * @see Word#findBoundsOfWord(java.util.List, String)
     * @see TextUtils#fuzzyContains(String, String)
     * @see TextUtils#fuzzyEqual(String, String)
     * @see MatchWords#ofList(java.util.List, String[])
     */
    public Bounds findBoundsOfWordCaseInsensitive(@Nonnull String string) {
        string = string.replace(" ", "");
        for (int i = words.size() - 1; i >= 0; i--) {
            for (int length = 1; length < words.size(); length++) {
                String fromFoundLocation = getSpacelessStringFromElements(i, length);
                if (TextUtils.fuzzyContains(fromFoundLocation, string, true)) {
                    return Bounds.ofAll(words.subList(i, i + length));
                }
            }
        }
        return bounds;
    }

    /**
     * Creates a line that is a fragment of this line containing only given words,
     * or returns this line if it does not contain the words.
     * Spaces in the words are ignored.
     * The comparison is made case-sensitive.
     *
     * @param wordsToFocusOn string to search
     * @return either the line containing only <code>words</code> cut out from this line,
     * or this line
     * @see Line#findBoundsOfWord(String)
     * @see Line#createBounded(Bounds)
     */
    @Nonnull
    public Line focusOn(@Nonnull String wordsToFocusOn) {
        return this.createBounded(findBoundsOfWord(wordsToFocusOn));
    }

    /**
     * Creates a line that is a fragment of this line containing only given words,
     * or returns this line if it does not contain the words.
     * Spaces in the words are ignored.
     * The comparison is made case-insensitive.
     *
     * @param wordsToFocusOn string to search
     * @return either the line containing only <code>words</code> cut out from this line,
     * or this line
     * @see Line#findBoundsOfWord(String)
     * @see Line#createBounded(Bounds)
     */
    @Nonnull
    public Line focusOnCaseInsensitive(@Nonnull String wordsToFocusOn) {
        return this.createBounded(findBoundsOfWordCaseInsensitive(wordsToFocusOn));
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    /**
     * Returns the closest (according to taxicab distance between centers) line
     * from the list, treating the lines that are above this line as being twice as far.
     *
     * @param lines list of lines
     * @return the closest line, or <code>null</code> if <code>lines</code> is empty
     */
    @Nullable
    public Line getClosestButPreferOnesBelowFrom(@Nonnull List<Line> lines) {
        Line closest = null;
        int minDist = Integer.MAX_VALUE;
        for (Line l : lines) {
            int dist = getDistance(l);
            if (l.bounds.isAbove(this.bounds)) {
                dist *= 2;
            }
            if (dist < minDist) {
                closest = l;
                minDist = dist;
            }
        }
        assert (lines.isEmpty() || closest != null);
        return closest;
    }

    /**
     * Returns the closest (according to taxicab distance between centers) line
     * from the list.
     *
     * @param line  first line
     * @param lines more lines
     * @return the closest line
     */
    @Nonnull
    public Line getClosestFrom(@Nonnull Line line, @Nonnull Line... lines) {
        Line closest = line;
        int minDist = getDistance(line);
        for (Line l : lines) {
            int dist = getDistance(l);
            if (dist < minDist) {
                closest = l;
                minDist = dist;
            }
        }
        return closest;
    }

    /**
     * Returns the closest (according to taxicab distance between centers) line
     * from the list.
     *
     * @param lines list of lines
     * @return the closest line, or <code>null</code> if <code>lines</code> is empty
     */
    @Nullable
    public Line getClosestFrom(@Nonnull Collection<? extends Line> lines) {
        Line closest = null;
        int minDist = Integer.MAX_VALUE;
        for (Line l : lines) {
            int dist = getDistance(l);
            if (dist < minDist) {
                closest = l;
                minDist = dist;
            }
        }
        assert (lines.isEmpty() || closest != null);
        return closest;
    }

    /**
     * Returns taxicab distance between middles of this line and the other line.
     *
     * @param l another line
     * @return taxicab distance
     */
    public int getDistance(@Nonnull Line l) {
        Bounds thatBounds = l.getBounds();
        return bounds.distance(thatBounds);
    }

    /**
     * Returns the median of the space width in this line.
     * Returns null if there are not enough words.
     *
     * @return median space width, or <code>null</code> if not available
     */
    @Nullable
    public Integer getMedianSpaceWidth() {
        int prevRight = Integer.MIN_VALUE;
        List<Integer> widths = new ArrayList<Integer>();
        for (Word e : words) {
            if (prevRight != Integer.MIN_VALUE) {
                int left = e.getBounds().getLeft();
                widths.add(left - prevRight);
            }
            prevRight = e.getBounds().getRight();
        }
        if (widths.isEmpty()) {
            return null;
        }
        Collections.sort(widths);
        int size = widths.size();
        if (size % 2 == 0) {
            return (widths.get(size / 2) + widths.get(size / 2 - 1)) / 2;
        } else {
            return widths.get(size / 2);
        }
    }

    /**
     * Returns a string containing texts of words from given range,
     * <b>not</b> separated by spaces.
     * The range of words starts from index <code>offset</code> (0-based)
     * and contains <code>length</code> words.
     * <br/>
     * If <code>length</code> &lt;= 0, the result is an empty string.
     * <br/>
     * If <code>offset</code> &gt;= <code>this.words.size()</code>, the result is an empty string.
     * <br/>
     * If <code>offset+length</code> &gt; <code>this.words.size()</code>,
     * only the available words are used.
     * <br/>
     * If <code>offset</code> &lt; 0,
     * it treats it as if it was 0 and <code>length</code> was correspondingly lower.
     *
     * @param offset index of the first word in the range
     * @param length number of words to include
     * @return string containing words from the range separated by spaces
     * @see Word#getSpaceLessStringFromWords(java.util.List, int, int)
     */
    @Nonnull
    public String getSpacelessStringFromElements(int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = Math.max(0, offset); i < Math.min(words.size(), offset + length); i++) {
            Word e = words.get(i);
            if (!e.isBlank()) {
                sb.append(e.mkString());
            }
        }
        return sb.toString();
    }

    @Override
    protected List<Word> getUnderlying() {
        return words;
    }

    /**
     * Returns a copy of the list of all words in this line.
     *
     * @return list of words
     */
    @Nonnull
    public List<Word> getWords() {
        ArrayList<Word> words = new ArrayList<Word>();
        for (Word e : this.words) {
            words.add(e);
        }
        return words;
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hashCodeMulti(words, bounds);
    }

    /**
     * Checks if all words in this line are blank.
     * Empty lines are treated as blank.
     *
     * @return <code>true</code> is all the words are blank, <code>false otherwise</code>
     */
    public boolean isBlank() {
        for (Word e : words) {
            if (!e.isBlank()) return false;
        }
        return true;
    }

    /**
     * Creates a new line with all words modified by the given function.
     * Bounds are recalculated unless this line contains no words.
     *
     * @param f word-modifying function
     * @return modified line
     */
    @Nonnull
    public Line map(@Nonnull Function<Word, Word> f) {
        List<Word> wordList = CollectionUtils.listMap(words, f);
        if (wordList.isEmpty()) {
            return new Line(null, wordList, bounds);
        } else {
            return new Line(null, wordList);
        }
    }

    /**
     * Creates a new line with all bounds modified by the given function.
     * Bounds are recalculated unless this line contains no words;
     * If there are no words, the bounds of this line
     * are modified using the given function.
     *
     * @param f bounds-modifying function
     * @return modified line
     */
    @Nonnull
    public Line mapBounds(@Nonnull final Function<Bounds, Bounds> f) {
        List<Word> wordList = CollectionUtils.listMap(words, new Function<Word, Word>() {
            @Nullable
            public Word apply(@Nullable Word word) {
                assert word != null;
                return word.mapBounds(f);
            }
        });
        if (wordList.isEmpty()) {
            return new Line(null, wordList, f.apply(bounds));
        } else {
            return new Line(null, wordList);
        }
    }

    /**
     * Returns a lowercase string containing texts of all words in this line, not separated by spaces.
     *
     * @param locale locale used to convert strings to lowercase
     * @return lowercase string
     */
    public String mkLowercaseSpacelessString(Locale locale) {
        StringBuilder sb = new StringBuilder();
        for (Word w : words) {
            sb.append(w.getText().toLowerCase(locale));
        }
        return sb.toString();
    }

    /**
     * Creates a string with all words from this line, space-separated,
     * without the words that may be OCR artifacts.
     *
     * @return string representation of most words in this line.
     * @see Word#mayBeOcrArtifact()
     */
    @Nonnull
    public String mkRoughString() {
        StringBuilder sb = new StringBuilder();
        boolean lastWasWord = false;
        for (Word w : words) {
            if (!w.mayBeOcrArtifact()) {
                if (lastWasWord) {
                    sb.append(' ');
                }
                sb.append(w.getText());
                lastWasWord = true;
            }
        }
        return sb.toString().trim();
    }

    /**
     * Returns a string containing texts of all words separated by spaces.
     *
     * @return string containing all words separated by spaces
     */
    @Nonnull
    public String mkString() {
        StringBuilder sb = new StringBuilder();
        boolean lastWasWord = false;
        for (Word w : words) {
            if (lastWasWord) {
                sb.append(' ');
            }
            sb.append(w.getText());
            lastWasWord = true;
        }
        return sb.toString();
    }

    @Nonnull
    @Override
    public Line subList(int i, int j) {
        return new Line(words.subList(i, j));
    }

    @Nonnull
    public String toString() {
        return mkString();
    }

    /**
     * Returns a string representation of the list of words,
     * containing also bounds of each of the words.
     *
     * @return string representation
     */
    @Nonnull
    public String toVerboseString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean first = true;
        for (Word e : words) {
            if (!first) {
                sb.append(", ");
            }
            if (e == null) {
                sb.append("null");
            } else {
                sb.append(e.toString());
                sb.append('@');
                sb.append(e.getBounds());
            }
            first = false;
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Translates the list by given vector.
     *
     * @param dx x displacement
     * @param dy y displacement
     * @return translated list
     */
    public Line translate(int dx, int dy) {
        List<Word> ws = new ArrayList<Word>(words.size());
        for (Word w : words) {
            ws.add(w.translate(dx, dy));
        }
        return new Line(null, ws, bounds.translate(dx, dy));
    }
}
