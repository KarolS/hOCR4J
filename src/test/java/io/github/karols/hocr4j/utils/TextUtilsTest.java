package io.github.karols.hocr4j.utils;

import org.junit.Test;

import static io.github.karols.hocr4j.utils.TextUtils.*;
import static org.junit.Assert.*;

/**
 * Utils Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>paź 9, 2013</pre>
 */
public class TextUtilsTest {

    @Test
    public void testToAscii() {
        assertEquals('a', toAscii('a'));
        assertEquals('a', toAscii('ą'));
        assertEquals('A', toAscii('A'));
        assertEquals('n', toAscii('ŋ'));
        assertEquals('x', toAscii('×'));
        assertEquals('Y', toAscii('Ÿ'));
    }

    @Test
    public void testFuzzyEqual() {
        assertTrue(fuzzyEqual("a", "a"));
        assertTrue(fuzzyEqual("ą", "a"));
        assertTrue(fuzzyEqual("ą ", "a"));
        assertTrue(fuzzyEqual("ą ", "\'a"));
        assertFalse(fuzzyEqual("b", "c"));
        assertFalse(fuzzyEqual("ą \'", "\'a"));
        assertFalse(fuzzyEqual("ąb'", "\'a"));
    }

    @Test
    public void testFuzzyPrefix() {
        assertTrue(fuzzyPrefix("a", ""));
        assertTrue(fuzzyPrefix("a", "a"));
        assertTrue(fuzzyPrefix("aa", "a"));
        assertTrue(fuzzyPrefix("abbbb", "a"));
        assertTrue(fuzzyPrefix("a", "a"));
        assertTrue(fuzzyPrefix("ą", "a"));
        assertTrue(fuzzyPrefix("ą ", "a"));
        assertTrue(fuzzyPrefix("ą ", "\'a"));
        assertTrue(fuzzyPrefix("ą \'", "\'a"));
        assertFalse(fuzzyPrefix("b", "c"));
        assertFalse(fuzzyPrefix("b", "bc"));
        assertTrue(fuzzyPrefix("ąb'", "\'a"));
        assertTrue(fuzzyPrefix("ą bcdf'", "\'ab"));
    }

    @Test
    public void testFuzzyContains() {
        assertTrue(fuzzyContains("a", ""));
        assertTrue(fuzzyContains("a", "a"));
        assertTrue(fuzzyContains("aa", "a"));
        assertTrue(fuzzyContains("abbbb", "a"));
        assertTrue(fuzzyContains("a", "a"));
        assertTrue(fuzzyContains("ą", "a"));
        assertTrue(fuzzyContains("ą ", "a"));
        assertTrue(fuzzyContains("ą ", "\'a"));
        assertTrue(fuzzyContains("ą \'", "\'a"));
        assertFalse(fuzzyContains("b", "c"));
        assertFalse(fuzzyContains("b", "bc"));
        assertTrue(fuzzyContains("ąb'", "\'a"));
        assertTrue(fuzzyContains("ą bcdf'", "\'ab"));
        assertTrue(fuzzyContains("xxxxxxąązzzzzzzz", "aa"));
    }
} 
