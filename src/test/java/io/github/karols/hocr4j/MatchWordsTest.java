package io.github.karols.hocr4j;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static io.github.karols.hocr4j.MatchWords.ofList;
import static org.junit.Assert.assertArrayEquals;

/**
 * MatchWords Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Nov 20, 2013</pre>
 */
public class MatchWordsTest {

    private Bounds b12 = new Bounds(1, 0, 2, 0);
    private Bounds b13 = new Bounds(1, 0, 3, 0);
    private Bounds b14 = new Bounds(1, 0, 4, 0);
    private Bounds b15 = new Bounds(1, 0, 5, 0);
    private Bounds b23 = new Bounds(2, 0, 3, 0);
    private Bounds b24 = new Bounds(2, 0, 4, 0);
    private Bounds b25 = new Bounds(2, 0, 5, 0);
    private Bounds b34 = new Bounds(3, 0, 4, 0);
    private Bounds b35 = new Bounds(3, 0, 5, 0);
    private Bounds b45 = new Bounds(4, 0, 5, 0);

    private String[] s(String... ss) {
        return ss;
    }

    private Bounds[] b(Bounds... bs) {
        return bs;
    }

    private List<Word> w(Word... ws) {
        return Arrays.asList(ws);
    }

    private Word w(String s, Bounds b) {
        return new Word(s, b);
    }

    /**
     * Method: ofList(List<Word> words, String[] strings)
     */
    @Test
    public void testOfList() throws Exception {
        assertArrayEquals(
                b(b12, b23, b45),
                ofList(
                        w(w("Ala", b12), w("ma", b23), w("kota", b45)),
                        s("Ala", "ma", "kota")
                ));
        assertArrayEquals(
                b(b12, b25),
                ofList(
                        w(w("Ala", b12), w("ma", b23), w("kota", b45)),
                        s("Ala", "makota")
                ));
    }


} 
