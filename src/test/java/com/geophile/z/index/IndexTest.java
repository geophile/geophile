/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index;

import com.geophile.z.Index;
import com.geophile.z.SpatialObject;
import com.geophile.z.index.treeindex.TreeIndex;
import com.geophile.z.space.Region;
import com.geophile.z.space.RegionComparison;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class IndexTest
{
    @Test
    public void test() throws IllegalAccessException, InstantiationException
    {
        test(TreeIndex.class);
    }

    private void test(Class indexClass)
        throws InstantiationException, IllegalAccessException
    {
        Index<TestSpatialObject> index = newIndex(indexClass);
        for (int uniqueKeys = 10; uniqueKeys <= 1000; uniqueKeys += 10) {
            for (int copies = 1; copies <= 8; copies++) {
                load(index, uniqueKeys, copies);
                checkContents(index, uniqueKeys, copies);
                testRemoval(index, uniqueKeys, copies);
            }
        }
    }

    private Index<TestSpatialObject> newIndex(Class indexClass)
        throws IllegalAccessException, InstantiationException
    {
        return (Index<TestSpatialObject>) indexClass.newInstance();
    }

    private void load(Index<TestSpatialObject> index, int uniqueKeys, int zCount)
    {
        /*
            With zCount = 3:

            spatial object   0     1     2     ...     (uniqueKeys - 1)

                         z   0
                            10    10
                            20    20    20
                                  30    30
                                        40

         */
        for (long k = 0; k < uniqueKeys; k++) {
            TestSpatialObject spatialObject = new TestSpatialObject(k);
            for (long c = 0; c < zCount; c++) {
                long z = (k + c) * GAP;
                index.add(z, spatialObject);
            }
        }
    }

    private void checkContents(Index<TestSpatialObject> index, int uniqueKeys, int zCount)
    {
        // debug("uniqueKeys: %s, zCount: %s", uniqueKeys, zCount);
        Cursor<TestSpatialObject> cursor = index.cursor(Long.MIN_VALUE);
        Record<TestSpatialObject> record;
        List<Long> zById[] = (List<Long>[]) new List[uniqueKeys];
        while (!(record = cursor.next()).eof()) {
            int id = (int) record.spatialObject().id();
            List<Long> zList = zById[id];
            if (zList == null) {
                zList = new ArrayList<>();
                zById[id] = zList;
            }
            zList.add(record.key().z());
        }
        for (int id = 0; id < uniqueKeys; id++) {
            List<Long> zList = zById[id];
            assertEquals(zCount, zList.size());
            long expected = id * GAP;
            for (Long z : zList) {
                assertEquals(expected, z.longValue());
                expected += GAP;
            }
        }
    }

    private void testRemoval(Index<TestSpatialObject> index, int uniqueKeys, int zCount)
    {
        for (long k = 0; k < uniqueKeys; k++) {
            long z = k * GAP;
            long id = index.remove(z, new TestSpatialObject(k));
            assertEquals(k, id);
            for (int c = 1; c < zCount; c++) {
                z += GAP;
                boolean removed = index.remove(z, id);
                assertTrue(removed);
            }
            boolean removed = index.remove(z + GAP, id);
            assertTrue(!removed);
        }
        Cursor<TestSpatialObject> cursor = index.cursor(Long.MIN_VALUE);
        assertTrue(cursor.next().eof());
    }

    private void debug(String template, Object ... args)
    {
        System.out.println(String.format(template, args));
    }

    private static class TestSpatialObject implements SpatialObject
    {
        @Override
        public long id()
        {
            return id;
        }

        @Override
        public long[] arbitraryPoint()
        {
            fail();
            return null;
        }

        @Override
        public boolean equalTo(SpatialObject that)
        {
            return this.id == ((TestSpatialObject)that).id;
        }

        @Override
        public boolean containedBy(Region region)
        {
            fail();
            return false;
        }

        @Override
        public RegionComparison compare(Region region)
        {
            fail();
            return null;
        }

        public TestSpatialObject(long id)
        {
            this.id = id;
        }

        private final long id;
    }

    private static final int GAP = 10;
}
