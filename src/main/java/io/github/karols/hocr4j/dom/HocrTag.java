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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An HOCR DOM tag element.
 */
public class HocrTag extends HocrElement {

    /**
     * Elements of the tag body.
     */
    public final ImmutableMap<String, String> attributes;
    /**
     * Value of the <code>class</code> attribute.
     */
    public final String clazz;
    /**
     * All tag attributes.
     */
    public final ImmutableList<HocrElement> elements;
    /**
     * Value of the <code>id</code> attribute.
     */
    public final String id;
    /**
     * Tag name.
     */
    public final String name;
    /**
     * Value of the <code>title</code> attribute.
     */
    public final String title;

    /**
     * Creates a new tag with the given elements
     * and with name and attributes based on the given contents of the opening tag.
     * @param openingTagString contents of the opening tag
     * @param contents elements in the body of the tag
     */
    public HocrTag(String openingTagString, List<HocrElement> contents) {
        final String x = openingTagString;
        int i = 1;
        while (x.charAt(i) == ' ') i++;
        int nameStart = i;
        while (x.charAt(i) != ' ' && x.charAt(i) != '>' && x.charAt(i) != '/') {
            i++;
        }
        name = x.substring(nameStart, i).toLowerCase(Locale.US);
        while (x.charAt(i) == ' ') i++;
        HashMap<String, String> attributes = new HashMap<String, String>();
        while (x.charAt(i) != '/' && x.charAt(i) != '>') {
            int attrNameStart = i;
            ing_bad:
            while (true) {
                switch (x.charAt(i)) {
                    case '=':
                    case '/':
                    case ' ':
                    case '>':
                        break ing_bad;
                    default:
                        i++;
                }
            }
            String attrName = x.substring(attrNameStart, i);
            while (x.charAt(i) == ' ') i++;
            String attrValue = attrName;
            if (x.charAt(i) == '=') {
                i++;
                while (x.charAt(i) == ' ') i++;
                int attrValueStart = i;
                switch (x.charAt(i)) {
                    case '\'':
                        attrValueStart++;
                        i++;
                        while (x.charAt(i) != '\'') i++;
                        attrValue = x.substring(attrValueStart, i);
                        i++;
                        break;
                    case '\"':
                        attrValueStart++;
                        i++;
                        while (x.charAt(i) != '\"') i++;
                        attrValue = x.substring(attrValueStart, i);
                        i++;
                        break;
                    default:
                        while (x.charAt(i) != ' ' && x.charAt(i) != '/' && x.charAt(i) != '>') i++;
                        attrValue = x.substring(attrValueStart, i);
                        break;
                }
            }
            while (x.charAt(i) == ' ') i++;
            attributes.put(
                    StringEscapeUtils.unescapeHtml4(attrName),
                    StringEscapeUtils.unescapeHtml4(attrValue));
        }
        this.id = attributes.get("id");
        this.clazz = attributes.get("class");
        this.title = attributes.get("title");
        this.attributes = ImmutableMap.copyOf(attributes);
        this.elements = ImmutableList.copyOf(contents);
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
        final HocrTag other = (HocrTag) obj;
        return ObjectUtils.equals(this.elements, other.elements) && ObjectUtils.equals(this.name, other.name) && ObjectUtils.equals(this.clazz, other.clazz) && ObjectUtils.equals(this.title, other.title) && ObjectUtils.equals(this.id, other.id) && ObjectUtils.equals(this.attributes, other.attributes);
    }

    @Nullable
    @Override
    public HocrTag findTag(@Nonnull String tagName) {
        if (name.equals(tagName)) {
            return this;
        } else {
            for (HocrElement e : elements) {
                HocrTag found = e.findTag(tagName);
                if (found != null) return found;
            }
        }
        return null;
    }//tested

    @Nonnull
    @Override
    public String getRawText() {
        StringBuilder sb = new StringBuilder();
        for (HocrElement e : elements) {
            sb.append(e.getRawText());
        }
        return sb.toString();
    }//tested

    @Override
    public int hashCode() {
        return ObjectUtils.hashCodeMulti(elements, name, clazz, title, id, attributes);
    }

    @Override
    public boolean isBlank() {
        return false;
    }//tested

    @Nonnull
    public String mkString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append(name);
        sb.append(" ");
        sb.append(id);
        sb.append(" ");
        sb.append(clazz);
        sb.append(">");
        for (HocrElement e : elements) {
            sb.append(e.mkString());
        }
        sb.append("</");
        sb.append(name);
        sb.append(">");
        return sb.toString();
    }

    public String toString() {
        String result = " <" + name + ">";
        if (!attributes.isEmpty()) {
            result += attributes;
        }
        if (!elements.isEmpty()) {
            result += elements;
        }
        return result + " ";

    }
}
