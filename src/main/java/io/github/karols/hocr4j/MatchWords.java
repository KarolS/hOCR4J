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

import io.github.karols.hocr4j.utils.CollectionUtils;
import io.github.karols.hocr4j.utils.TextUtils;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Karol Stasiak
 */
public class MatchWords {

    static private class ListW2SM implements Comparable<ListW2SM>, Iterable<WordToStringMatch> {
        private final long hash;
        private final WordToStringMatch head;
        private final int length;
        private final ListW2SM tail;

        private ListW2SM(WordToStringMatch head, ListW2SM tail) {
            this.hash = tail.hash * 0x44444444447L + head.hashCode();
            this.tail = tail;
            this.head = head;
            this.length = tail.length + 1;
        }

        private ListW2SM() {
            this.hash = 0;
            this.tail = null;
            this.head = null;
            this.length = 0;
        }

        static ListW2SM cons(WordToStringMatch head, ListW2SM tail) {
            return new ListW2SM(head, tail);
        }

        public static Function<ListW2SM, ListW2SM> cons_(final WordToStringMatch head) {
            return new Function<ListW2SM, ListW2SM>() {
                public ListW2SM apply(ListW2SM tail) {
                    return ListW2SM.cons(head, tail);
                }
            };
        }

        static ListW2SM nil() {
            return new ListW2SM();
        }

        public int compareTo(ListW2SM that) {
            if (hash < that.hash) {
                return -1;
            }
            if (hash > that.hash) {
                return 1;
            }
            if (isEmpty() && !that.isEmpty()) {
                return -1;
            }
            if (!isEmpty() && that.isEmpty()) {
                return -1;
            }
            int hc = head.compareTo(that.head);
            if (hc != 0) {
                return hc;
            } else {
                return tail.compareTo(that.tail);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ListW2SM that = (ListW2SM) o;
            if (hash != that.hash) return false;
            if (isEmpty() != that.isEmpty()) {
                return false;
            } else if (isEmpty()) {
                return true;
            }
            return head.equals(that.head) && tail.equals(that.tail);
        }

        @Override
        public int hashCode() {
            return (int) (hash ^ (hash >>> 32));
        }

        boolean isEmpty() {
            return length == 0;
        }

        public Iterator<WordToStringMatch> iterator() {
            return new Iterator<WordToStringMatch>() {

                ListW2SM x = ListW2SM.this;

                public boolean hasNext() {
                    return !x.isEmpty();
                }

                public WordToStringMatch next() {
                    WordToStringMatch result = x.head;
                    x = x.tail;
                    return result;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    static private class Parser {
        List<String> strings;
        List<Word> words;

        Parser(List<Word> ws, List<String> ss) {
            words = ws;
            strings = ss;
        }

        ArrayList<ListW2SM> parse() {
            return new ArrayList<ListW2SM>(parse(0, 0, ImmutableList.<Word>of()));
        }

        @SuppressWarnings("unchecked")
        Set<ListW2SM> parse(int wordOffset, int stringOffset, ImmutableList<Word> prefix) {
            if (words.size() - wordOffset < strings.size() - stringOffset) {
                return Collections.emptySet();
            }
            if (wordOffset >= words.size()) {
                if (prefix.isEmpty() && stringOffset >= strings.size()) {
                    return Collections.singleton(ListW2SM.nil());
                } else {
                    return Collections.emptySet();
                }
            }
            Word headWord = words.get(wordOffset);
            ArrayList<ListW2SM> buffer = new ArrayList<ListW2SM>();
            if (headWord.getText().length() <= 1) {
                Iterables.addAll(buffer,
                        parse(wordOffset + 1, stringOffset, prefix));
            }
            if (stringOffset < strings.size()) {
                StringBuilder sb = new StringBuilder();
                for (Word w : prefix) {
                    sb.append(w.getText());
                }
                sb.append(headWord.getText());

                String prefixPlusHeadWord = sb.toString();
                String headString = strings.get(stringOffset);

                if (TextUtils.fuzzyEqual(headString, prefixPlusHeadWord)) {
                    Set<ListW2SM> tails =
                            parse(wordOffset + 1, stringOffset + 1, ImmutableList.<Word>of());
                    final WordToStringMatch head = new WordToStringMatch(CollectionUtils.pushBack(prefix, headWord), headString);
                    Iterables.addAll(buffer,
                            CollectionUtils.<ListW2SM, ListW2SM>setMap(tails, ListW2SM.cons_(head)));
                }
                if (TextUtils.fuzzyPrefix(headString, prefixPlusHeadWord)) {
                    Iterables.addAll(buffer,
                            parse(wordOffset + 1, stringOffset, CollectionUtils.pushBack(prefix, headWord)));
                }
            }
            return new HashSet<ListW2SM>(buffer);
        }
    }

    static private class WordToStringMatch implements Comparable<WordToStringMatch> {
        String string;
        ImmutableList<Word> words;

        private WordToStringMatch(ImmutableList<Word> words, String string) {
            this.words = words;
            this.string = string;
        }

        public int compareTo(WordToStringMatch that) {
            return new CompareToBuilder()
                    .append(words, that.words)
                    .append(string, that.string)
                    .toComparison();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WordToStringMatch that = (WordToStringMatch) o;
            return this.compareTo(that) == 0;
        }

        @Override
        public int hashCode() {
            int result = words != null ? words.hashCode() : 0;
            result = 31 * result + (string != null ? string.hashCode() : 0);
            return result;
        }
    }

    /**
     * For given array of strings,
     * returns bounds those strings have in the given list list word.
     * Returns <code>null</code> if failed to match strings to words.
     * @param words list of words (can be a <code>Line</code>)
     * @param strings array of strings to match
     * @return array of matched bounds if successful, <code>null</code> if not
     * @see TextUtils#fuzzyEqual(String, String)
     * @see Line#findBoundsOfWord(String)
     */
    @Nullable
    public static Bounds[] ofList(@Nonnull List<Word> words, @Nonnull String[] strings) {
        Parser parser = new Parser(words, Arrays.asList(strings));
        List<ListW2SM> parses = parser.parse();
        if (parses.size() == 1) {
            Bounds[] result = new Bounds[strings.length];
            ListW2SM onlyParse = parses.get(0);
            assert (onlyParse.length == strings.length);
            int i = 0;
            for (WordToStringMatch match : onlyParse) {
                result[i] = Bounds.ofAll(match.words);
                i++;
            }
            return result;
        }
        return null;
    }//tested for easy examples
}
