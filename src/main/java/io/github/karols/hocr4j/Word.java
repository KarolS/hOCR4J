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

import com.google.common.base.Function;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Represents a word in the OCR'd document.
 *
 * Corresponding hOCR class: <code>ocrx_word</code>.
 */
@Immutable
public class Word implements Bounded, Comparable<Word> {

    /**
     * If <code>string</code> is made from concatenating
     * several consecutive words from the <code>wordList</code>,
     * returns union of bounds of those words.
     * If not, returns <code>null</code>.
     * Spaces are ignored. Uses normal, strict string equality.
     *
     * @param wordList list of words
     * @param string   string to search for
     * @return bounds of the matching words
     * @see Line#findBoundsOfWord(String)
     */
    @Nullable
    public static Bounds findBoundsOfWord(@Nonnull List<Word> wordList, @Nonnull String string) {
        string = string.replace(" ", "");
        for (int i = wordList.size() - 1; i >= 0; i--) {
            String tillTheEndOfLine = getSpaceLessStringFromWords(wordList, i, wordList.size());
            if (tillTheEndOfLine.startsWith(string)) {
                for (int length = 1; length < wordList.size(); length++) {
                    String fromFoundLocation = getSpaceLessStringFromWords(wordList, i, length);
                    if (string.equals(fromFoundLocation)) {
                        return Bounds.ofAll(wordList.subList(i, i + length));
                    }
                }
            }
        }
        return null; // TODO?
    }

    /**
     * @see Line#getSpacelessStringFromElements(int, int)
     */
    @Nonnull
    public static String getSpaceLessStringFromWords(@Nonnull List<Word> words, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < Math.min(words.size(), offset + length); i++) {
            Word e = words.get(i);
            sb.append(e.mkString());
        }
        return sb.toString();
    }

    /**
     * Converts a list of words to a space-separated string.
     *
     * @param words list of words
     * @return string
     * @see Line#mkString()
     */
    @Nonnull
    public static String toString(@Nonnull List<Word> words) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Word w : words) {
            if (!first) {
                sb.append(' ');
            }
            sb.append(w.text);
            first = false;
        }
        return sb.toString();
    }

    private final Bounds bounds;
    private final boolean isBold;
    private final boolean isItalic;
    private final String text;

    /**
     * Creates a word using standard font.
     *
     * @param text   text of the word
     * @param bounds bounds of the word
     */
    public Word(@Nonnull String text, Bounds bounds) {
        this.text = text;
        this.bounds = bounds;
        this.isBold = false;
        this.isItalic = false;
    }

    /**
     * Creates a word using given font font.
     *
     * @param text     text of the word
     * @param bounds   bounds of the word
     * @param isBold   if the font is bold
     * @param isItalic if the font is italic
     */
    public Word(@Nonnull String text, Bounds bounds, boolean isBold, boolean isItalic) {
        this.text = text;
        this.bounds = bounds;
        this.isBold = isBold;
        this.isItalic = isItalic;
    }

    /**
     * Creates a word from the corresponding HOCR element. Recognized elements include:
     * <ul>
     * <li>&lt;span&gt; tag with class="ocrx_word" or class="ocr_word</li>
     * <li>&lt;b&gt;, &lt;i&gt;, &lt;em&gt;, &lt;strong&gt; tags</li>
     * <li>text elements</li>
     * </ul>
     *
     * @param e HOCR element
     */
    public Word(@Nonnull HocrElement e) {
        Bounds _bounds = null;
        boolean _isBold = false;
        boolean _isItalic = false;
        text = e.getRawText();
        while (true) {
            if (e instanceof HocrTag) {
                HocrTag tag = (HocrTag) e;
                if (tag.name.equals("span") && ("ocrx_word".equals(tag.clazz) || "ocr_word".equals(tag.clazz))) {
                    _bounds = Bounds.fromHocrTitleValue(tag.title);
                } else if (tag.name.equals("strong") || tag.name.equals("b")) {
                    _isBold = true;
                } else if (tag.name.equals("em") || tag.name.equals("i")) {
                    _isItalic = true;
                } else {
                    throw new IllegalArgumentException(e.mkString());
                }
                if (tag.elements.isEmpty()) {
                    break;
                } else {
                    e = tag.elements.get(0);
                }
            } else {
                break;
            }
        }
        isItalic = _isItalic;
        isBold = _isBold;
        bounds = _bounds;
    }

    @Override
    public int compareTo(Word that) {
        return new CompareToBuilder()
                .append(this.text, that.text)
                .append(this.bounds, that.bounds)
                .append(this.isBold, that.isBold)
                .append(this.isItalic, that.isItalic)
                .toComparison();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Word word = (Word) o;

        if (isBold != word.isBold) return false;
        if (isItalic != word.isItalic) return false;
        if (text != null ? !text.equals(word.text) : word.text != null) return false;

        return true;
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    /**
     * Returns text of this word
     *
     * @return text
     */
    public String getText() {
        return text;
    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + (isBold ? 1 : 0);
        result = 31 * result + (isItalic ? 1 : 0);
        return result;
    }

    /**
     * Checks if this word is blank.
     *
     * @return <code>true</code> if this word is blank, <code>false</code> otherwise
     */
    public boolean isBlank() {
        return StringUtils.isBlank(text);
    }

    /**
     * Checks if this word is written using bold font.
     *
     * @return <code>true</code> if this word is bold, <code>false</code> otherwise
     */
    public boolean isBold() {
        return isBold;
    }

    /**
     * Checks if this word is written using italic font.
     *
     * @return <code>true</code> if this word is italic, <code>false</code> otherwise
     */
    public boolean isItalic() {
        return isItalic;
    }

    /**
     * Creates a new word with bounds modified by the given function.
     *
     * @param f bounds-modifying function
     * @return modified word
     */
    @Nonnull
    public Word mapBounds(Function<Bounds, Bounds> f) {
        return new Word(text, f.apply(bounds), isBold, isItalic);
    }

    /**
     * Checks if this word may be an OCR artifact rather than actual word.
     * Current implementation just checks if the word has one character or more.
     *
     * @return <code>true</code> if this word may be an OCR artifact,
     * <code>false</code> otherwise
     */
    public boolean mayBeOcrArtifact() {
        return text.length() <= 1; // TODO: think up something smarter
    }

    /**
     * Returns text of this word.
     *
     * @return text
     */
    @Nonnull
    public String mkString() {
        return text;
    }

    public String toString() {
        return text;
    }

    /**
     * Translates the word by given vector.
     *
     * @param dx x displacement
     * @param dy y displacement
     * @return translated word
     */
    public Word translate(int dx, int dy) {
        return new Word(text, bounds.translate(dx, dy));
    }
}
