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

import io.github.karols.hocr4j.Page;
import io.github.karols.hocr4j.utils.ListWrappingQueue;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class HocrParser {

    private HocrParser(){}

    private static int nonnegative(int i) {
        if (i < 0) throw new IllegalStateException();
        return i;
    }

    static int elementLength(String hocr, int offset) {
        if (hocr.length() <= offset) return 0;
        if (hocr.charAt(offset) == '<') {
            int closedAt = hocr.indexOf('>', offset);
            int nextOpening = hocr.indexOf('<', offset + 1);
            if (nextOpening > 0 && nextOpening < closedAt) {
                // DAMN MALFORMED XML!!!!
                if (hocr.length() <= offset + 1) {
                    return nonnegative(hocr.length() - offset);
                } else {
                    if (hocr.charAt(offset + 1) != '<') {
                        return nonnegative(1 + elementLength(hocr, offset + 1));
                    } else {
                        return 1;
                    }
                }
            } else {
                if (closedAt < 0) {
                    return nonnegative(hocr.length() - offset);
                } else {
                    return nonnegative(closedAt + 1 - offset);
                }
            }
        } else {
            int openedAt = hocr.indexOf('<', offset);
            if (openedAt < 0) return nonnegative(hocr.length() - offset);
            else return nonnegative(openedAt - offset);
        }
    }

    static ArrayList<String> lex(String hocr) {
        ArrayList<String> result = new ArrayList<String>();
        int offset = 0;
        while (hocr.length() > offset) {
            int elemLength = elementLength(hocr, offset);
            String elem = hocr.substring(offset, offset + elemLength);
            result.add(elem);
            offset += elemLength;
        }
        return result;
    }//tested

    static List<HocrElement> createAst(Queue<String> tokens) {
        ArrayList<HocrElement> result = new ArrayList<HocrElement>();
        while (!tokens.isEmpty()) {
            String t = tokens.poll();
            if (t.startsWith("<") && t.endsWith(">")) {
                if (t.startsWith("<!")) continue;
                if (t.startsWith("</")) break;
                if (t.endsWith("/>")) {
                    result.add(new HocrTag(t, ImmutableList.<HocrElement>of()));
                } else {
                    result.add(new HocrTag(t, createAst(tokens)));
                }
            } else {
                result.add(new HocrText(t));
            }
        }
        return result;
    }

    public static List<HocrElement> createAst(String hocr) {
        return createAst(new ListWrappingQueue<String>(lex(hocr)));
    }

    public static List<Page> parse(List<HocrElement> elements) {
        return parse(elements, 1);
    }

    public static List<Page> parse(List<HocrElement> elements, int startingPageNumber) {
        ArrayList<Page> result = new ArrayList<Page>();
        List<HocrElement> bodyContents =
                new HocrTag("<>", elements).findTag("body").elements;
        int pageNo = startingPageNumber;
        for (HocrElement h : bodyContents) {
            if (h.isNotBlank()) {
                result.add(new Page(pageNo, h));
                pageNo++;
            }
        }
        return result;
    }

    public static List<Page> parse(String hocr) {
        return parse(createAst(hocr));
    }
}
