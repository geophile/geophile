/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialjoin;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DuplicateEliminatingIteratorTest
{
    @Test
    public void noDuplicates()
    {
        for (int n = 0; n < 10; n++) {
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                list.add(i);
            }
            Iterator<Integer> iterator = new DuplicateEliminatingIterator<>(list.iterator());
            for (int i = 0; i < n; i++) {
                assertTrue(iterator.hasNext());
                assertEquals(i, iterator.next().intValue());
            }
            assertTrue(!iterator.hasNext());
        }
    }

    @Test
    public void allDuplicates()
    {
        Integer x = 419;
        for (int n = 1; n < 10; n++) {
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                list.add(x);
            }
            Iterator<Integer> iterator = new DuplicateEliminatingIterator<>(list.iterator());
            assertTrue(iterator.hasNext());
            assertEquals(x, iterator.next());
            assertTrue(!iterator.hasNext());
        }
    }

    @Test
    public void nonAdjacentDuplicates()
    {
        int x = 0;
        List<Integer> list = new ArrayList<>();
        for (int repeats = 0; repeats < 10; repeats++) {
            list.add(x++ % 3);
        }
        Iterator<Integer> iterator = new DuplicateEliminatingIterator<>(list.iterator());
        assertTrue(iterator.hasNext());
        assertEquals(0, iterator.next().intValue());
        assertTrue(iterator.hasNext());
        assertEquals(1, iterator.next().intValue());
        assertTrue(iterator.hasNext());
        assertEquals(2, iterator.next().intValue());
        assertTrue(!iterator.hasNext());
    }
}
