/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index;

import com.geophile.z.Index;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public abstract class IndexTestBase
{
    // TODO: Load and remove in other orders.

    @Test
    public void test() throws IllegalAccessException, InstantiationException, IOException, InterruptedException
    {
        Index<TestSpatialObject> index = newIndex();
        for (int nObjects = 0; nObjects <= 1000; nObjects += 100) {
            for (int copies = 1; copies <= 8; copies++) {
                load(index, nObjects, copies);
                checkContents(index, nObjects, copies, Collections.<Long>emptySet());
                checkRetrieval(index, nObjects, copies);
                removeAll(index, nObjects, copies);
            }
        }
    }

    protected abstract Index<TestSpatialObject> newIndex();

    private void load(Index<TestSpatialObject> index, int nObjects, int zCount)
        throws IOException, InterruptedException
    {
        /*
            With zCount = 3, GAP = 10:

            spatial object   0     1     2     ...     (nObjects - 1)

                         z   0
                            10    10
                            20    20    20
                                  30    30
                                        40

         */
        for (long id = 0; id < nObjects; id++) {
            TestSpatialObject spatialObject = new TestSpatialObject(id);
            for (long c = 0; c < zCount; c++) {
                long z = (id + c) * GAP;
                index.add(z, spatialObject);
            }
        }
    }

    private void checkContents(Index<TestSpatialObject> index, int nObjects, int zCount, Set<Long> removedIds)
        throws IOException, InterruptedException
    {
        Set<Long> presentIds = new HashSet<>();
        Cursor<TestSpatialObject> cursor = index.cursor(Long.MIN_VALUE);
        Record<TestSpatialObject> record;
        List<List<Long>> zById = new ArrayList<>();
        for (long id = 0; id < nObjects; id++) {
            zById.add(new ArrayList<Long>());
        }
        while (!(record = cursor.next()).eof()) {
            long id = (int) record.spatialObject().id();
            assertTrue(!removedIds.contains(id));
            presentIds.add(id);
            List<Long> zList = zById.get((int) id);
            zList.add(record.key().z());
        }
        for (long id = 0; id < nObjects; id++) {
            if (!removedIds.contains(id)) {
                assertTrue(presentIds.contains(id));
                List<Long> zList = zById.get((int) id);
                assertEquals(zCount, zList.size());
                long expected = id * GAP;
                for (Long z : zList) {
                    assertEquals(expected, z.longValue());
                    expected += GAP;
                }
            }
        }
    }

    private void removeAll(Index<TestSpatialObject> index, int nObjects, int zCount)
        throws IOException, InterruptedException
    {
        Set<Long> removedIds = new HashSet<>();
        for (long id = 0; id < nObjects; id++) {
            // Remove id and check remaining contents
            for (long c = 0; c < zCount; c++) {
                long z = (id + c) * GAP;
                boolean removed = index.remove(z, id);
                assertTrue(removed);
                removed = index.remove(z + GAP / 2, id);
                assertTrue(!removed);
                removed = index.remove(z, Long.MAX_VALUE);
                assertTrue(!removed);
            }
            removedIds.add(id);
            checkContents(index, nObjects, zCount, removedIds);
        }
    }

    private void checkRetrieval(Index<TestSpatialObject> index, int nObjects, int zCount)
        throws IOException, InterruptedException
    {
        print("nObjects: %s, zCount: %s", nObjects, zCount);
        // Expected
        NavigableMap<SpatialObjectKey, Object> allKeys = new TreeMap<>();
        for (long id = 0; id < nObjects; id++) {
            // Remove id and check remaining contents
            for (long c = 0; c < zCount; c++) {
                long z = (id + c) * GAP;
                SpatialObjectKey key = SpatialObjectKey.key(z, id);
                allKeys.put(key, null);
            }
        }
        Cursor<TestSpatialObject> cursor;
        Record<TestSpatialObject> record;
        Iterator<SpatialObjectKey> expected;
        SpatialObjectKey start;
        // Try traversal forward from the beginning
        start = SpatialObjectKey.keyLowerBound(Long.MIN_VALUE);
        expected = allKeys.keySet().iterator();
        cursor = index.cursor(start.z());
        while (!(record = cursor.next()).eof()) {
            assertEquals(expected.next(), record.key());
        }
        assertTrue(!expected.hasNext());
        // Try traversal forward from the halfway
        start = SpatialObjectKey.keyLowerBound(GAP * nObjects / 2);
        expected = allKeys.tailMap(start, true).keySet().iterator();
        cursor = index.cursor(start.z());
        while (!(record = cursor.next()).eof()) {
            assertEquals(expected.next(), record.key());
        }
        assertTrue(!expected.hasNext());
        // Try traversal forward from the end
        start = SpatialObjectKey.keyLowerBound(Long.MAX_VALUE);
        expected = allKeys.tailMap(start, true).keySet().iterator();
        cursor = index.cursor(start.z());
        while (!(record = cursor.next()).eof()) {
            fail();
        }
        assertTrue(!expected.hasNext());
        // Try traversal backward from the beginning
        start = SpatialObjectKey.keyLowerBound(Long.MIN_VALUE);
        expected = allKeys.headMap(start, true).descendingKeySet().iterator();
        cursor = index.cursor(start.z());
        while (!(record = cursor.previous()).eof()) {
            fail();
        }
        assertTrue(!expected.hasNext());
        // Try traversal backward from the halfway
        start = SpatialObjectKey.keyLowerBound(GAP * nObjects / 2);
        expected = allKeys.headMap(start, true).descendingKeySet().iterator();
        cursor = index.cursor(start.z());
        while (!(record = cursor.previous()).eof()) {
            assertEquals(expected.next(), record.key());
        }
        assertTrue(!expected.hasNext());
        // Try traversal backward from the end
        start = SpatialObjectKey.keyLowerBound(Long.MAX_VALUE);
        expected = allKeys.descendingKeySet().iterator();
        cursor = index.cursor(start.z());
        while (!(record = cursor.previous()).eof()) {
            assertEquals(expected.next(), record.key());
        }
        assertTrue(!expected.hasNext());
    }

    private void print(String template, Object ... args)
    {
        System.out.println(String.format(template, args));
    }

    private static final int GAP = 10;
}
