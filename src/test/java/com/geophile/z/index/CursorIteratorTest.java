package com.geophile.z.index;

import com.geophile.z.index.treeindex.TreeIndex;
import com.geophile.z.spatialobject.d2.Point;
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CursorIteratorTest
{
    @Test
    public void test()
    {
        for (int n = 0; n <= N_MAX; n++) {
            test(n);
        }
    }

    private void test(int n)
    {
        Index<Point> index = loadIndex(n);
        Iterator<Point> iterator = new CursorIterator<>(index.cursor(Long.MIN_VALUE));
        int expected = 0;
        while (iterator.hasNext()) {
            Point point = iterator.next();
            assertEquals(expected, point.x());
            assertEquals(expected, point.y());
            expected++;
        }
        assertEquals(n, expected);
        try {
            iterator.next();
            fail();
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    private Index<Point> loadIndex(int n)
    {
        Index<Point> index = new TreeIndex<>();
        for (int i = 0; i < n; i++) {
            index.add(i, new Point(i, i));
        }
        return index;
    }

    private static final int N_MAX = 100;
}
