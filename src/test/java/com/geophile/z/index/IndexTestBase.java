/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index;

import com.geophile.z.Index;
import com.geophile.z.Serializer;
import com.geophile.z.space.SpaceImpl;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public abstract class IndexTestBase
{
    // TODO: Load and remove in other orders.

    @BeforeClass
    public static void beforeClass()
    {
        SERIALIZER.register(1, TestSpatialObject.class);
    }

    @Test
    public void test() throws Exception
    {
        Index index = newIndex();
        for (int nObjects = 0; nObjects <= 1000; nObjects += 100) {
            for (int copies = 1; copies <= 8; copies++) {
                print("nObjects: %s, copies: %s", nObjects, copies);
                load(index, nObjects, copies);
                checkContents(index, nObjects, copies, Collections.<Long>emptySet());
                checkRetrieval(index, nObjects, copies);
                removeAll(index, nObjects, copies);
            }
        }
    }

    protected abstract Index newIndex() throws IOException, InterruptedException;

    protected void commit()
    {}

    private void load(Index index, int nObjects, int zCount)
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
                long z = z((id + c) * GAP);
                // print("id: %s, c: %s -- z: %s", id, c, SpaceImpl.formatZ(z));
                index.add(z, spatialObject);
            }
        }
        commit();
    }

    private void checkContents(Index index, int nObjects, int zCount, Set<Long> removedIds)
        throws IOException, InterruptedException
    {
        Set<Long> presentIds = new HashSet<>();
        Cursor cursor = index.cursor(SpaceImpl.Z_MIN);
        Record record;
        List<List<Long>> zById = new ArrayList<>();
        for (long id = 0; id < nObjects; id++) {
            zById.add(new ArrayList<Long>());
        }
        while (!(record = cursor.next()).eof()) {
            System.out.println(record);
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
                    assertEquals(z(expected), z.longValue());
                    expected += GAP;
                }
            }
        }
    }

    private void removeAll(Index index, int nObjects, int zCount)
        throws IOException, InterruptedException
    {
        Set<Long> removedIds = new HashSet<>();
        for (long id = 0; id < nObjects; id++) {
            // Remove id and check remaining contents
            for (long c = 0; c < zCount; c++) {
                long expected = (id + c) * GAP;
                boolean removed = index.remove(z(expected), id);
                assertTrue(index.blindUpdates() && !removed || !index.blindUpdates() && removed);
                removed = index.remove(z(expected + GAP / 2), id);
                assertTrue(!removed);
                removed = index.remove(z(expected), SpaceImpl.Z_MAX);
                assertTrue(!removed);
            }
            removedIds.add(id);
            checkContents(index, nObjects, zCount, removedIds);
        }
    }

    private void checkRetrieval(Index index, int nObjects, int zCount)
        throws IOException, InterruptedException
    {
        // Expected
        NavigableMap<SpatialObjectKey, Object> allKeys = new TreeMap<>();
        for (long id = 0; id < nObjects; id++) {
            for (long c = 0; c < zCount; c++) {
                SpatialObjectKey key = SpatialObjectKey.key(z((id + c) * GAP), id);
                allKeys.put(key, null);
            }
        }
        Cursor cursor;
        Record record;
        Iterator<SpatialObjectKey> expected;
        SpatialObjectKey start;
        int count;
        // Try traversal forward from the beginning
        start = SpatialObjectKey.keyLowerBound(z(SpaceImpl.Z_MIN));
        expected = allKeys.keySet().iterator();
        cursor = index.cursor(start.z());
        count = 0;
        while (!(record = cursor.next()).eof()) {
            assertEquals(expected.next(), record.key());
            count++;
        }
        assertTrue(!expected.hasNext());
        assertEquals(nObjects * zCount, count);
        // Try traversal forward from halfway
        start = SpatialObjectKey.keyLowerBound(z(GAP * nObjects / 2 + GAP / 2));
        expected = allKeys.tailMap(start, true).keySet().iterator();
        cursor = index.cursor(start.z());
        while (!(record = cursor.next()).eof()) {
            assertEquals(expected.next(), record.key());
        }
        assertTrue(!expected.hasNext());
        // Try traversal forward from the end
        start = SpatialObjectKey.keyUpperBound(z(SpaceImpl.Z_MAX));
        expected = allKeys.tailMap(start, true).keySet().iterator();
        cursor = index.cursor(start.z());
        while (!(record = cursor.next()).eof()) {
            fail();
        }
        assertTrue(!expected.hasNext());
        // Try traversal backward from the beginning (lower bound is before first key)
        start = SpatialObjectKey.keyLowerBound(z(SpaceImpl.Z_MIN));
        expected = allKeys.headMap(start, true).descendingKeySet().iterator();
        cursor = index.cursor(start.z());
        while (!(record = cursor.previous()).eof()) {
            fail();
        }
        assertTrue(!expected.hasNext());
        // Try traversal backward from halfway
        start = SpatialObjectKey.keyUpperBound(z(GAP * nObjects / 2 + GAP / 2));
        expected = allKeys.headMap(start, true).descendingKeySet().iterator();
        cursor = index.cursor(start.z());
        while (!(record = cursor.previous()).eof()) {
            assertEquals(expected.next(), record.key());
        }
        assertTrue(!expected.hasNext());
        // Try traversal backward from the end (upper bound gets everything)
        start = SpatialObjectKey.keyUpperBound(z(SpaceImpl.Z_MAX));
        expected = allKeys.descendingKeySet().iterator();
        cursor = index.cursor(start.z());
        count = 0;
        while (!(record = cursor.previous()).eof()) {
            assertEquals(expected.next(), record.key());
            count++;
        }
        assertTrue(!expected.hasNext());
        assertEquals(nObjects * zCount, count);
    }

    private long z(long x)
    {
        return SpaceImpl.z(x << SpaceImpl.LENGTH_BITS, SpaceImpl.MAX_Z_BITS);
    }

    private void print(String template, Object ... args)
    {
        System.out.println(String.format(template, args));
    }

    private static final int GAP = 10;

    protected static final Serializer SERIALIZER = Serializer.newSerializer();
}
