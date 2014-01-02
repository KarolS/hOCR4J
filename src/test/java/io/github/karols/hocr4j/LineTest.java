package io.github.karols.hocr4j;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class LineTest {

    private Line createLine(String... words) {
        List<Word> es = new ArrayList<Word>(words.length);
        for (int i = 0; i < words.length; i++) {
            Word w = new Word(words[i], new Bounds(5 * i, 0, 5 * i + 4, 5));
            es.add(w);
        }
        return new Line(es);
    }

    @Test
    public void testMkString() {
        assertEquals("a", createLine("a").mkString());
        assertEquals("a b", createLine("a", "b").mkString());
        assertEquals("a b c", createLine("a", "b", "c").mkString());
    }

    @Test
    public void testMkRoughString() {
        assertEquals("", createLine("a").mkRoughString());
        assertEquals("aaa", createLine("aaa").mkRoughString());
        assertEquals("aaa bbb", createLine("aaa", "bbb").mkRoughString());
        assertEquals("bbb", createLine("a", "bbb").mkRoughString());
        assertEquals("aaa", createLine("aaa", "b").mkRoughString());
        assertEquals("aaa bbb ccc", createLine("aaa", "bbb", "ccc").mkRoughString());
        assertEquals("bbb ccc", createLine("a", "bbb", "ccc").mkRoughString());
        assertEquals("aaa ccc", createLine("aaa", "b", "ccc").mkRoughString());
        assertEquals("aaa bbb", createLine("aaa", "bbb", "c").mkRoughString());
        assertEquals("aaa", createLine("aaa", "b", "c").mkRoughString());
        assertEquals("bbb", createLine("a", "bbb", "c").mkRoughString());
        assertEquals("ccc", createLine("a", "b", "ccc").mkRoughString());
    }

    @Test
    public void testEmpty() {
        assertFalse(createLine("a").isEmpty());
    }

    @Test
    public void testBlank() {
        assertFalse(createLine("a").isBlank());
    }

    private Line createBoundedLine(int left, int right, String... words) {
        return createLine(words).createBounded(new Bounds(left, 0, right, 5));
    }

    @Test
    public void testCreateBounded() {
        assertEquals("b c", createBoundedLine(5, 15, "a", "b", "c", "d").mkString());
        assertEquals("a b c", createBoundedLine(0, 15, "a", "b", "c", "d").mkString());
        assertEquals("d", createBoundedLine(15, 300, "a", "b", "c", "d").mkString());
        assertEquals("", createBoundedLine(11, 12, "a", "b", "c", "d").mkString());
    }

    private void tbow(String word, int left, int right, String... words) {
        Bounds b = createLine(words).findBoundsOfWord(word);
        assertEquals(left, b.getLeft());
        assertEquals(right, b.getRight());
    }

    @Test
    public void testBoundsOfWords() {
        tbow("b", 5, 9, "a", "b", "c", "d");
        tbow("b c", 5, 14, "a", "b", "c", "d");
        tbow("bbb ccc", 5, 19, "a", "bbb", ",", "ccc", "d");
    }


    private void tfo(String resultingWord, String wordToFocusOn, String... words) {
        Line original = createLine(words);
        Line focused = original.focusOn(wordToFocusOn);
        assertTrue(focused.bounds.in(original.bounds));
        assertEquals(resultingWord, focused.mkString());
    }

    @Test
    public void testFocusOn() {
        tfo("a", "a",
                "a", "b");
        tfo("b c", "b c",
                "a", "b", "c", "d");
        tfo("b cch", "b cc",
                "a", "b", "cch", "d");
        tfo("hbb cch", "bb cc",
                "a", "hbb", "cch", "d");
    }

    private void tgssfe(String spaceless, int from, int length, String... words) {
        Line l = createLine(words);
        assertEquals(spaceless, l.getSpacelessStringFromElements(from, length));
    }

    @Test
    public void testGetSpacelessStringFromElements() {
        tgssfe("ab", 0, 2, "a", "b", "c");
        tgssfe("a", 0, 1, "a", "b", "c");
        tgssfe("abc", 0, 5, "a", "b", "c");
        tgssfe("bc", 1, 5, "a", "b", "c");
        tgssfe("c", 2, 5, "a", "b", "c");
        tgssfe("b", 1, 1, "a", "b", "c");
    }


}

