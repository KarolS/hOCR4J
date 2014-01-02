package io.github.karols.hocr4j.dom;

import org.junit.Test;

import static io.github.karols.hocr4j.dom.HocrParser.lex;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

/**
 * HocrParser Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Oct 4, 2013</pre>
 */
public class HocrParserTest {

    /**
     * Method: lex(String hocr)
     */
    @Test
    public void testLex() throws Exception {
        assertEquals(asList("a"), lex("a"));
        assertEquals(asList("<a>", "</a>"), lex("<a></a>"));
        assertEquals(asList("<a>", "a", "</a>"), lex("<a>a</a>"));
        assertEquals(asList("<a>", "<aa", "</a>"), lex("<a><aa</a>"));
        assertEquals(asList("<a>", "aa", "<", "</a>"), lex("<a>aa<</a>"));
        assertEquals(asList("<a>", "a", "<a", "</a>"), lex("<a>a<a</a>"));
        assertEquals(asList("<a>", "<", "</a>"), lex("<a><</a>"));
        assertEquals(asList("<a>", ">", "</a>"), lex("<a>></a>"));
        assertEquals(asList("<a>", "<", "<", "</a>"), lex("<a><<</a>"));
        assertEquals(asList("<a>", ">", "<", "</a>"), lex("<a>><</a>"));
    }

    /**
     * Method: createAst(Queue<String> tokens)
     */
    @Test
    public void testParseTokens() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: createAst(String hocr)
     */
    @Test
    public void testParseHocr() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: parse(List<HocrElement> words)
     */
    @Test
    public void testInterpretElements() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: parse(String hocr)
     */
    @Test
    public void testInterpretHocr() throws Exception {
//TODO: Test goes here... 
    }


} 
