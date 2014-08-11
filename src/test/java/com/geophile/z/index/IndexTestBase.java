/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index;

import com.geophile.z.*;
import com.geophile.z.space.SpaceImpl;
import com.geophile.util.Stopwatch;
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
    public void testIndex() throws Exception
    {
        try {
            Index index = newIndex();
            for (int nObjects = 0; nObjects <= 1000; nObjects += 100) {
                for (int copies = 1; copies <= 8; copies++) {
                    long start = System.currentTimeMillis();
                    load(index, nObjects, copies);
                    long load = System.currentTimeMillis();
                    checkContents(index, nObjects, copies, Collections.<Integer>emptySet());
                    long checkContents = System.currentTimeMillis();
                    checkRetrieval(index, nObjects, copies);
                    long checkRetrieval = System.currentTimeMillis();
                    removeAll(index, nObjects, copies);
                    long removeAll = System.currentTimeMillis();

    /*
                    print("    load: %s, checkContents: %s, checkRetrieval: %s, removeAll: %s",
                          (load - start) / 1000.0,
                          (checkContents - load) / 1000.0,
                          (checkRetrieval - checkContents) / 1000.0,
                          (removeAll - checkRetrieval) / 1000.0 );
    */
                }
            }
        } finally {
            shutdown();
        }
    }

    @Test
    public void testCursor() throws Exception
    {
        for (int nObjects = 0; nObjects <= 1000; nObjects += 100) {
            testCursor(nObjects);
        }
    }

    protected abstract Index newIndex() throws IOException, InterruptedException;

    protected void commit()
    {}

    protected void shutdown() throws IOException, InterruptedException
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
                                  30    30    30
                                        40    40    40
                                              50    50

         */
        for (int id = 0; id < nObjects; id++) {
            TestSpatialObject spatialObject = new TestSpatialObject(id);
            for (long c = 0; c < zCount; c++) {
                long z = z((id + c) * GAP);
                TestRecord record = (TestRecord) index.newRecord();
                record.z(z);
                record.spatialObject(spatialObject);
                record.soid(id);
                index.add(record);
            }
        }
        commit();
    }

    private void removeAll(Index index, int nObjects, int zCount)
        throws IOException, InterruptedException
    {
        Stopwatch removeTimer = new Stopwatch();
        Stopwatch checkTimer = new Stopwatch();
        Set<Integer> removedIds = new HashSet<>();
        for (int id = 0; id < nObjects; id++) {
            // Remove id and check remaining contents
            removeTimer.start();
            for (long c = 0; c < zCount; c++) {
                long expected = (id + c) * GAP;
                boolean removed = index.remove(z(expected), recordFilter(z(expected), id));
                assertTrue(index.blindUpdates() && !removed || !index.blindUpdates() && removed);
                removed = index.remove(z(expected + GAP / 2), recordFilter(z(expected + GAP / 2), id));
                assertTrue(!removed);
                removed = index.remove(z(expected), recordFilter(z(expected), Integer.MAX_VALUE));
                assertTrue(!removed);
            }
            removeTimer.stop();
            removedIds.add(id);
            if (id % 1000 == 0) {
                checkTimer.start();
                checkContents(index, nObjects, zCount, removedIds);
                checkTimer.stop();
            }
        }
        commit();
/*
        print("       remove time: %s", removeTimer.nSec() / 1_000_000_000.0);
        print("       check time: %s", checkTimer.nSec() / 1_000_000_000.0);
*/
    }

    private void checkContents(Index index, int nObjects, int zCount, Set<Integer> removedIds)
        throws IOException, InterruptedException
    {
        Set<Integer> presentIds = new HashSet<>();
        Cursor cursor = newCursor(index, SpaceImpl.Z_MIN);
        TestRecord record;
        List<List<Long>> zById = new ArrayList<>();
        for (int id = 0; id < nObjects; id++) {
            zById.add(new ArrayList<Long>());
        }
        while ((record = (TestRecord) cursor.next()) != null) {
            int id = record.soid();
            assertTrue(!removedIds.contains(id));
            presentIds.add(id);
            List<Long> zList = zById.get(id);
            zList.add(record.z());
        }
        for (int id = 0; id < nObjects; id++) {
            if (!removedIds.contains(id)) {
                assertTrue(presentIds.contains(id));
                List<Long> zList = zById.get(id);
                assertEquals(zCount, zList.size());
                long expected = id * GAP;
                for (Long z : zList) {
                    assertEquals(z(expected), z.longValue());
                    expected += GAP;
                }
            }
        }
    }

    private void checkRetrieval(Index index, int nObjects, int zCount)
        throws IOException, InterruptedException
    {
        // Expected
        NavigableMap<TestRecord, Object> allKeys = new TreeMap<>(TEST_RECORD_COMPARATOR);
        for (int id = 0; id < nObjects; id++) {
            for (long c = 0; c < zCount; c++) {
                TestRecord key = new TestRecord();
                key.z(z((id + c) * GAP));
                key.soid(id);
                key.spatialObject(new TestSpatialObject(id));
                allKeys.put(key, null);
            }
        }
        Cursor cursor;
        TestRecord record;
        TestRecord start;
        Iterator<TestRecord> expected;
        int count;
        // Try traversal forward from the beginning
        cursor = newCursor(index, SpaceImpl.Z_MIN);
        expected = allKeys.keySet().iterator();
        count = 0;
        while ((record = (TestRecord) cursor.next()) != null) {
            TestRecord next = expected.next();
            assertEquals(next, record);
            count++;
        }
        assertTrue(!expected.hasNext());
        assertEquals(nObjects * zCount, count);
        // Try traversal forward from halfway
        start = key(index, z(GAP * nObjects / 2 + GAP / 2));
        expected = allKeys.tailMap(start, true).keySet().iterator();
        cursor = newCursor(index, start.z());
        while ((record = (TestRecord) cursor.next()) != null) {
            assertEquals(expected.next(), record);
        }
        assertTrue(!expected.hasNext());
        // Try traversal forward from the end
        start = key(index, SpaceImpl.Z_MAX, Integer.MAX_VALUE);
        expected = allKeys.tailMap(start, true).keySet().iterator();
        cursor = newCursor(index, start.z());
        while ((record = (TestRecord) cursor.next()) != null) {
            fail();
        }
        assertTrue(!expected.hasNext());
        // Try traversal backward from the beginning (lower bound is before first key)
        start = key(index, SpaceImpl.Z_MIN);
        expected = allKeys.headMap(start, true).descendingKeySet().iterator();
        cursor = newCursor(index, start.z());
        while ((record = (TestRecord) cursor.previous()) != null) {
            fail();
        }
        assertTrue(!expected.hasNext());
        // Try traversal backward from halfway
        start = key(index, z(GAP * nObjects / 2 + GAP / 2), Integer.MAX_VALUE);
        expected = allKeys.headMap(start, true).descendingKeySet().iterator();
        cursor = newCursor(index, start.z());
        while ((record = (TestRecord) cursor.previous()) != null) {
            assertEquals(expected.next(), record);
        }
        assertTrue(!expected.hasNext());
        // Try traversal backward from the end (upper bound gets everything)
        start = key(index, SpaceImpl.Z_MAX, Integer.MAX_VALUE);
        expected = allKeys.descendingKeySet().iterator();
        cursor = newCursor(index, start.z());
        count = 0;
        while ((record = (TestRecord) cursor.previous()) != null) {
            assertEquals(expected.next(), record);
            count++;
        }
        assertTrue(!expected.hasNext());
        assertEquals(nObjects * zCount, count);
    }

    private void testCursor(int nObjects) throws Exception
    {
        Index index = newIndex();
        try {
            // Delete everything in scan
            {
                load(index, nObjects, 1);
                Cursor cursor = newCursor(index, SpaceImpl.Z_MIN);
                Record record;
                int id = 0;
                while ((record = cursor.next()) != null) {
                    assertEquals(z(id * GAP), record.z());
                    cursor.deleteCurrent();
                    id++;
                }
                cursor.goTo(key(index, SpaceImpl.Z_MIN));
                assertNull(cursor.next());
                assertEquals(id, nObjects);
            }
            // Same thing, backwards
            {
                load(index, nObjects, 1);
                Cursor cursor = newCursor(index, SpaceImpl.Z_MAX);
                Record record;
                int id = nObjects;
                while ((record = cursor.previous()) != null) {
                    id--;
                    assertEquals(z(id * GAP), record.z());
                    cursor.deleteCurrent();
                }
                cursor.goTo(key(index, SpaceImpl.Z_MAX));
                assertNull(cursor.previous());
                assertEquals(id, 0);
            }
            // Skip every other
            {
                load(index, nObjects, 1);
                Cursor cursor = newCursor(index, SpaceImpl.Z_MIN);
                Record record;
                int id = 0;
                while ((record = cursor.next()) != null) {
                    // Delete even ids
                    assertEquals(z(id * GAP), record.z());
                    if (id % 2 == 0) {
                        cursor.deleteCurrent();
                    }
                    id++;
                }
                // Check odd ids remain
                id = 1;
                cursor.goTo(key(index, SpaceImpl.Z_MIN));
                while ((record = cursor.next()) != null) {
                    assertEquals(z(id * GAP), record.z());
                    cursor.deleteCurrent();
                    id += 2;
                }
                cursor.goTo(key(index, SpaceImpl.Z_MIN));
                assertNull(cursor.next());
                assertEquals(id, nObjects + 1);
            }
            // Skip every other going backward
            {
                load(index, nObjects, 1);
                Cursor cursor = newCursor(index, SpaceImpl.Z_MAX);
                Record record;
                int id = nObjects;
                while ((record = cursor.previous()) != null) {
                    id--;
                    // Delete even ids
                    assertEquals(z(id * GAP), record.z());
                    if (id % 2 == 0) {
                        cursor.deleteCurrent();
                    }
                }
                // Check odd ids remain
                id = 1;
                cursor.goTo(key(index, SpaceImpl.Z_MIN));
                while ((record = cursor.next()) != null) {
                    assertEquals(z(id * GAP), record.z());
                    cursor.deleteCurrent();
                    id += 2;
                }
                cursor.deleteCurrent();
                cursor.goTo(key(index, SpaceImpl.Z_MAX));
                assertNull(cursor.previous());
                assertEquals(id, nObjects + 1);
            }
            // Test random walk, selecting next or previous randomly
            {
                Random random = new Random(23456);
                load(index, nObjects, 1);
                // Get copy of keys for control
                long[] control = new long[nObjects];
                Cursor cursor = newCursor(index, SpaceImpl.Z_MIN);
                Record record;
                int c = 0;
                while ((record = cursor.next()) != null) {
                    control[c++] = record.z();
                }
                // Do random walk, starting in the middle, deleting each visited key.
                // Do the same walk in the control array, indicating deleting with -1.
                c = nObjects / 2;
                long zExpected;
                cursor = newCursor(index, z(nObjects * GAP / 2));
                int delta;
                for (int i = 0; i < nObjects; i++) {
                    if (random.nextBoolean()) {
                        // forward
                        record = cursor.next();
                        if (record == null) {
                            cursor.goTo(key(index, SpaceImpl.Z_MIN));
                            record = cursor.next();
                        }
                        assertNotNull(record);
                        cursor.deleteCurrent();
                        delta = 1;
                    } else {
                        // backward
                        record = cursor.previous();
                        if (record == null) {
                            cursor.goTo(key(index, SpaceImpl.Z_MAX));
                            record = cursor.previous();
                        }
                        assertNotNull(record);
                        cursor.deleteCurrent();
                        delta = -1;
                    }
                    if (i > 0) {
                        do {
                            c += delta;
                            if (c == nObjects) {
                                c = 0;
                            } else if (c == -1) {
                                c = nObjects - 1;
                            }
                        } while (control[c] == -1);
                    }
                    zExpected = control[c];
                    control[c] = -1;
                    assertEquals(zExpected, record.z());
                }
                cursor.goTo(key(index, SpaceImpl.Z_MIN));
                assertNull(cursor.previous());
                for (c = 0; c < nObjects; c++) {
                    assertEquals(-1, control[c]);
                }
            }
        } finally {
            shutdown();
        }
    }

    private void dumpContents(Index index, String label)
        throws IOException, InterruptedException
    {
        print(label);
        print("{");
        Cursor cursor = newCursor(index, SpaceImpl.Z_MIN);
        Record record;
        while ((record = cursor.next()) != null) {
            print("    %s", record);
        }
        print("}");
    }

    private long z(long x)
    {
        return SpaceImpl.z(x << SpaceImpl.LENGTH_BITS, SpaceImpl.MAX_Z_BITS);
    }

    private RecordFilter recordFilter(final long z, final int soid)
    {
        return
            new RecordFilter()
            {
                @Override
                public boolean select(Record record)
                {
                    assert record.z() == z;
                    TestRecord testRecord = (TestRecord) record;
                    return testRecord.soid() == soid;
                }
            };
    }

    private Cursor newCursor(Index index, long z) throws IOException, InterruptedException
    {
        Cursor cursor = index.cursor();
        Record key= index.newKeyRecord();
        key.z(z);
        cursor.goTo(key);
        return cursor;
    }

    private TestRecord key(Index index, long z)
    {
        return key(index, z, 0);
    }

    private TestRecord key(Index index, long z, int soid)
    {
        TestRecord key = (TestRecord) index.newKeyRecord();
        key.z(z);
        key.soid(soid);
        return key;
    }

    private void print(String template, Object ... args)
    {
        System.out.println(String.format(template, args));
    }

    protected static final SpatialObjectSerializer SERIALIZER = SpatialObjectSerializer.newSerializer();
    private static final int GAP = 10;
    private static final Comparator<TestRecord> TEST_RECORD_COMPARATOR =
        new Comparator<TestRecord>()
        {
            @Override
            public int compare(TestRecord r, TestRecord s)
            {
                return r.keyCompare(s);
            }
        };
}
