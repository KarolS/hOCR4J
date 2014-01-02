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

package io.github.karols.hocr4j.dom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A DOM element of a HOCR document.
 */
public abstract class HocrElement {

    /**
     * Returns the first tag with given name found in this element.
     * If this element is a tag with this name, returns this element.
     * If no tag is found, <code>null</code> is returned.
     * @param tagName name of the tag to search for.
     * @return the first tag with this name, or <code>null</code> if not found
     */
    public abstract @Nullable HocrTag findTag(@Nonnull String tagName);

    /**
     * Returns all text in the element, stripping all tags.
     * HTML entities will be decoded.
     * @return all the text in this element
     */
    public abstract @Nonnull String getRawText();

    /**
     * Returns true if the element is a blank text element, false otherwise.
     */
    public abstract boolean isBlank();

    /**
     * Returns false if the element is a blank text element, true otherwise.
     */
    public final boolean isNotBlank() {
        return !isBlank();
    }

    /**
     * Converts the element to a compact, incomplete text representation.
     * @return string representation
     */
    public abstract @Nonnull String mkString();
}
