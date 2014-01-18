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

import java.util.Comparator;

/**
 * Utility class containing comparators for <code>Bounded</code> objects.
 *
 * @see io.github.karols.hocr4j.Bounded
 * @see io.github.karols.hocr4j.Bounds
 */
public final class OrderedBy {
    private final static Comparator<Bounded> CENTER_LEFTWARDS = new Comparator<Bounded>() {
        public int compare(Bounded bounded1, Bounded bounded2) {
            int c1 = bounded1.getBounds().getCenter();
            int c2 = bounded2.getBounds().getCenter();
            if (c1 > c2) return -1;
            if (c1 < c2) return 1;
            return FLOW_ORDER.compare(bounded2, bounded1);
        }
    };
    private final static Comparator<Bounded> CENTER_RIGHTWARDS = new Comparator<Bounded>() {
        public int compare(Bounded bounded1, Bounded bounded2) {
            int c1 = bounded1.getBounds().getCenter();
            int c2 = bounded2.getBounds().getCenter();
            if (c1 > c2) return 1;
            if (c1 < c2) return -1;
            return FLOW_ORDER.compare(bounded1, bounded2);
        }
    };
    private static final Comparator<Bounded> FLOW_ORDER = new Comparator<Bounded>() {
        public int compare(Bounded bounded1, Bounded bounded2) {
            Bounds b1 = bounded1.getBounds();
            Bounds b2 = bounded2.getBounds();
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
                return Integer.compare(bounded1.hashCode(), bounded2.hashCode());
            }
        }
    };
    private static final Comparator<Bounded> LEFTISH_RIGHWARDS = new Comparator<Bounded>() {
        public int compare(Bounded bounded1, Bounded bounded2) {
            int c1 = bounded1.getBounds().getLeftish();
            int c2 = bounded2.getBounds().getLeftish();
            if (c1 > c2) return 1;
            if (c1 < c2) return -1;
            return FLOW_ORDER.compare(bounded1, bounded2);
        }
    };
    private final static Comparator<Bounded> MIDDLE_DOWNWARDS = new Comparator<Bounded>() {
        public int compare(Bounded bounded1, Bounded bounded2) {
            int c1 = bounded1.getBounds().getMiddle();
            int c2 = bounded2.getBounds().getMiddle();
            if (c1 > c2) return 1;
            if (c1 < c2) return -1;
            return FLOW_ORDER.compare(bounded1, bounded2);
        }
    };
    private final static Comparator<Bounded> MIDDLE_UPWARDS = new Comparator<Bounded>() {
        public int compare(Bounded bounded1, Bounded bounded2) {
            int c1 = bounded1.getBounds().getMiddle();
            int c2 = bounded2.getBounds().getMiddle();
            if (c1 > c2) return -1;
            if (c1 < c2) return 1;
            return FLOW_ORDER.compare(bounded2, bounded1);
        }
    };
    private static final Comparator<Bounded> REVERSE_FLOW_ORDER = new Comparator<Bounded>() {
        @Override
        public int compare(Bounded bounded1, Bounded bounded2) {
            return OrderedBy.FLOW_ORDER.compare(bounded2, bounded1);
        }
    };

    /**
     * Returns a comparator comparing bounds by their average x coordinate descending
     *
     * @return comparator
     */
    public static Comparator<Bounded> centerLeftwards() {
        return CENTER_LEFTWARDS;
    }

    /**
     * Returns a comparator comparing bounds by their average x coordinate ascending
     *
     * @return comparator
     */
    public static Comparator<Bounded> centerRightwards() {
        return CENTER_RIGHTWARDS;
    }

    /**
     * Returns a comparator comparing bounds
     * according to the natural flow of the left-to-right text
     *
     * @return comparator
     */
    public static Comparator<Bounded> flowOrder() {
        return FLOW_ORDER;
    }

    /**
     * Returns a comparator comparing bounds by the x coordinate
     * at 20% on the way from the left edge to the right edge
     * ascending
     *
     * @return comparator
     */
    public static Comparator<Bounded> leftishRightwards() {
        return LEFTISH_RIGHWARDS;
    }

    /**
     * Returns a comparator comparing bounds by their average y coordinate ascending
     *
     * @return comparator
     */
    public static Comparator<Bounded> middleDownwards() {
        return MIDDLE_DOWNWARDS;
    }

    /**
     * Returns a comparator comparing bounds by their average y coordinate descending
     *
     * @return comparator
     */
    public static Comparator<Bounded> middleUpwards() {
        return MIDDLE_UPWARDS;
    }

    /**
     * Returns a comparator comparing bounds
     * reverse to the natural flow of the left-to-right text
     *
     * @return comparator
     */
    public static Comparator<Bounded> reverseFlowOrder() {
        return REVERSE_FLOW_ORDER;
    }


    private OrderedBy() {
    }
}
