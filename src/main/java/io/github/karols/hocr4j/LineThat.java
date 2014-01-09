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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import java.util.Comparator;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

public class LineThat {

    private final static Comparator<Line> HAS_LEAST_WORDS = new Comparator<Line>() {
        public int compare(Line line, Line line2) {
            int c1 = line.words.size();
            int c2 = line2.words.size();
            if (c1 > c2) return -1;
            if (c1 < c2) return 1;
            return 0;
        }
    };
    private final static Comparator<Line> HAS_MOST_WORDS = new Comparator<Line>() {
        public int compare(Line line, Line line2) {
            int c1 = line.words.size();
            int c2 = line2.words.size();
            if (c1 > c2) return 1;
            if (c1 < c2) return -1;
            return 0;
        }
    };
    private static final Predicate<Line> IS_ARBITRARY = Predicates.alwaysTrue();
    private static final Comparator<Line> IS_AT_THE_BEGINNING = new Comparator<Line>() {
        @Override
        public int compare(Line line1, Line line2) {
            return LineThat.IS_AT_THE_END.compare(line2, line1);
        }
    };
    private final static Comparator<Line> IS_AT_THE_BOTTOM = new Comparator<Line>() {
        public int compare(Line line, Line line2) {
            int c1 = line.getBounds().getMiddle();
            int c2 = line2.getBounds().getMiddle();
            if (c1 > c2) return 1;
            if (c1 < c2) return -1;
            return 0;
        }
    };
    private static final Comparator<Line> IS_AT_THE_END = new Comparator<Line>() {
        public int compare(Line line, Line line2) {
            Bounds b1 = line.bounds;
            Bounds b2 = line2.bounds;
            if (b1 != null && b2 != null) {
                if (b1.isBelow(b2)) return 1;
                if (b2.isBelow(b1)) return -1;
                if (b1.isToTheRight(b2)) return 1;
                if (b2.isToTheRight(b1)) return -1;
                if (b1.getMiddle() > b2.getMiddle()) return 1;
                if (b1.getMiddle() < b2.getMiddle()) return -1;
                if (b1.getCenter() > b2.getCenter()) return 1;
                if (b1.getCenter() < b2.getCenter()) return -1;
                return 0;
            } else {
                return Integer.compare(line.hashCode(), line2.hashCode());
            }
        }
    };
    private final static Comparator<Line> IS_AT_THE_LEFT = new Comparator<Line>() {
        public int compare(Line line, Line line2) {
            int c1 = line.getBounds().getCenter();
            int c2 = line2.getBounds().getCenter();
            if (c1 > c2) return -1;
            if (c1 < c2) return 1;
            return 0;
        }
    };
    private final static Comparator<Line> IS_AT_THE_RIGHT = new Comparator<Line>() {
        public int compare(Line line, Line line2) {
            int c1 = line.getBounds().getCenter();
            int c2 = line2.getBounds().getCenter();
            if (c1 > c2) return 1;
            if (c1 < c2) return -1;
            return 0;
        }
    };
    private final static Comparator<Line> IS_AT_THE_TOP = new Comparator<Line>() {
        public int compare(Line line, Line line2) {
            int c1 = line.getBounds().getMiddle();
            int c2 = line2.getBounds().getMiddle();
            if (c1 > c2) return -1;
            if (c1 < c2) return 1;
            return 0;
        }
    };
    private static final Predicate<Line> IS_NOT_BLANK = new Predicate<Line>() {
        public boolean apply(Line input) {
            return input != null && !input.isBlank();
        }
    };

    public static Predicate<Line> contains(final String string) {
        return new Predicate<Line>() {
            public boolean apply(Line input) {
                return input != null && input.mkString().contains(string);
            }
        };
    }

    /**
     * Returns a comparator comparing lines by the number of words descending
     *
     * @return comparator
     */
    public static Comparator<Line> hasLeastWords() {
        return HAS_LEAST_WORDS;
    }

    /**
     * Returns a comparator comparing lines by the number of words ascending
     *
     * @return comparator
     */
    public static Comparator<Line> hasMostWords() {
        return HAS_MOST_WORDS;
    }

    /**
     * Returns a predicate that tests
     * if the line has any word that intersects given rectangle.
     *
     * @param rect rectangle
     * @return predicate
     */
    public static Predicate<Line> hasWordsIntersecting(final Bounds rect) {
        return new Predicate<Line>() {
            public boolean apply(@Nullable Line input) {
                if (input == null) {
                    return false;
                }
                for (Word w : input.words) {
                    if (w.getBounds().intersects(rect)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Returns a predicate that always yields true.
     *
     * @return predicate
     */
    public static Predicate<Line> isArbitrary() {
        return IS_ARBITRARY;
    }

    /**
     * Returns a comparator comparing lines
     * reverse to the natural flow of the left-to-right text
     *
     * @return comparator
     */
    public static Comparator<Line> isAtTheBeginning() {
        return IS_AT_THE_BEGINNING;
    }

    /**
     * Returns a comparator comparing lines by their average y coordinate ascending
     *
     * @return comparator
     */
    public static Comparator<Line> isAtTheBottom() {
        return IS_AT_THE_BOTTOM;
    }

    /**
     * Returns a comparator comparing lines
     * according to the natural flow of the left-to-right text
     *
     * @return comparator
     */
    public static Comparator<Line> isAtTheEnd() {
        return IS_AT_THE_END;
    }

    /**
     * Returns a comparator comparing lines by their average x coordinate descending
     *
     * @return comparator
     */
    public static Comparator<Line> isAtTheLeft() {
        return IS_AT_THE_LEFT;
    }

    /**
     * Returns a comparator comparing lines by their average x coordinate ascending
     *
     * @return comparator
     */
    public static Comparator<Line> isAtTheRight() {
        return IS_AT_THE_RIGHT;
    }

    /**
     * Returns a comparator comparing lines by their average y coordinate descending
     *
     * @return comparator
     */
    public static Comparator<Line> isAtTheTop() {
        return IS_AT_THE_TOP;
    }

    /**
     * Returns a predicate that returns <code>true</code> if the line is not blank.
     *
     * @return predicate
     */
    public static Predicate<Line> isNotBlank() {
        return IS_NOT_BLANK;
    }

    /**
     * Returns a predicate that returns <code>true</code>
     * if the line matches given pattern case-sensitive
     *
     * @param regex pattern
     * @return predicate
     */
    public static Predicate<Line> matchesRegex(String regex) {
        return matchesRegex(Pattern.compile(regex));
    }

    /**
     * Returns a predicate that returns <code>true</code>
     * if the line matches given pattern
     *
     * @param regex pattern
     * @return predicate
     */
    public static Predicate<Line> matchesRegex(final Pattern regex) {
        return new Predicate<Line>() {
            public boolean apply(Line input) {
                return input != null && regex.matcher(input.mkString()).matches();
            }
        };
    }

    /**
     * Returns a predicate that returns <code>true</code>
     * if the line matches given pattern case-insensitive
     *
     * @param regex pattern
     * @return predicate
     */
    public static Predicate<Line> matchesRegexIgnoringCase(String regex) {
        return matchesRegex(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
    }
}