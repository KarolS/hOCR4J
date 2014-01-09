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

package io.github.karols.hocr4j.dom;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A DOM text element.
 */
public class HocrText extends HocrElement {

    /**
     * Text of this node.
     * All HTML entities are already decoded.
     */
    public final String text;

    /**
     * Creates a text node with given text
     * @param t HTML-encoded text
     */
    public HocrText(String t) {
        text = StringEscapeUtils.unescapeHtml4(t);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final HocrText other = (HocrText) obj;
        return ObjectUtils.equals(this.text, other.text);
    }

    @Nullable
    @Override
    public HocrTag findTag(@Nonnull String tagName) {
        return null;
    }

    @Nonnull
    @Override
    public String getRawText() {
        return text;
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hashCode(text);
    }

    @Override
    public boolean isBlank() {
        return StringUtils.isBlank(text);
    }//tested

    /**
     * Text of this node, or an empty string if this node is blank.
     * All HTML entities are already decoded.
     */
    @Nonnull
    @Override
    public String mkString() {
        if (isBlank()) return "";
        else return text;
    }

    public String toString() {
        return '\"' + text + '\"';
    }
}
