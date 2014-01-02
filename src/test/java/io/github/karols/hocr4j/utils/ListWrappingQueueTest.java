package io.github.karols.hocr4j.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Queue;

import static org.junit.Assert.*;

/**
 * ListWrappingQueue Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Oct 4, 2013</pre>
 */
public class ListWrappingQueueTest {

    /**
     * Method: iterator()
     */
    @Test
    public void testIterator() throws Exception {
        Queue<Integer> q = new ListWrappingQueue<Integer>(Arrays.asList(1, 2, 3));
        Iterator<Integer> i = q.iterator();
        assertTrue(i.hasNext());
        assertEquals(Integer.valueOf(1), i.next());
        assertTrue(i.hasNext());
        assertEquals(Integer.valueOf(2), i.next());
        assertTrue(i.hasNext());
        assertEquals(Integer.valueOf(3), i.next());
        assertFalse(i.hasNext());
    }

    /**
     * Method: size()
     */
    @Test
    public void testSize() throws Exception {
        Queue<Integer> q = new ListWrappingQueue<Integer>(Arrays.asList(1, 2, 3));
        assertEquals(3, q.size());
        q.poll();
        assertEquals(2, q.size());
        q.poll();
        assertEquals(1, q.size());
    }

    /**
     * Method: offer(E e)
     */
    @Test
    public void testOffer() throws Exception {
        Queue<Integer> q = new ListWrappingQueue<Integer>(Arrays.asList(1, 2, 3));
        try {
            q.offer(4);
            fail();
        } catch (UnsupportedOperationException uoe) {
            // OK
        }
    }

    /**
     * Method: poll()
     */
    @Test
    public void testPoll() throws Exception {
        Queue<Integer> q = new ListWrappingQueue<Integer>(Arrays.asList(1, 2, 3));
        assertEquals(Integer.valueOf(1), q.poll());
        assertEquals(Integer.valueOf(2), q.poll());
        assertEquals(Integer.valueOf(3), q.poll());
        try {
            q.poll();
            fail();
        } catch (Exception e) {
            // OK
        }
    }

    /**
     * Method: peek()
     */
    @Test
    public void testPeek() throws Exception {
        Queue<Integer> q = new ListWrappingQueue<Integer>(Arrays.asList(1, 2, 3));
        assertEquals(Integer.valueOf(1), q.peek());
        assertEquals(Integer.valueOf(1), q.peek());
        assertEquals(Integer.valueOf(1), q.peek());
        q.poll();
        assertEquals(Integer.valueOf(2), q.peek());
        assertEquals(Integer.valueOf(2), q.peek());
        q.poll();
        assertEquals(Integer.valueOf(3), q.peek());
        assertEquals(Integer.valueOf(3), q.peek());
        q.poll();
        try {
            q.peek();
            fail();
        } catch (Exception e) {
            // OK
        }
    }


} 
