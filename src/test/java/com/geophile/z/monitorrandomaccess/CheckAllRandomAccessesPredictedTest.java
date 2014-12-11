/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.monitorrandomaccess;

import com.geophile.z.Cursor;
import com.geophile.z.Record;
import com.geophile.z.Space;
import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialJoin;
import com.geophile.z.SpatialObject;
import com.geophile.z.TestIndex;
import com.geophile.z.TestRecord;
import com.geophile.z.space.SpaceImpl;
import com.geophile.z.spatialjoin.BoxGenerator;
import com.geophile.z.spatialobject.d2.Box;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import static com.geophile.z.space.SpaceImpl.Z_NULL;
import static com.geophile.z.space.SpaceImpl.formatZ;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CheckAllRandomAccessesPredictedTest
{
    @Test
    public void test() throws IOException, InterruptedException
    {
        for (int dataBoxSize = DELTA_DATA_BOX_SIDE;
             dataBoxSize <= MAX_DATA_BOX_SIDE;
             dataBoxSize += DELTA_DATA_BOX_SIDE) {
            SpatialIndex<TestRecord> spatialIndex = loadDB(dataBoxSize);
            for (int queryBoxSize = DELTA_QUERY_BOX_SIDE;
                 queryBoxSize <= MAX_QUERY_BOX_SIDE;
                 queryBoxSize += DELTA_QUERY_BOX_SIDE) {
                for (int q = 0; q < QUERIES; q++) {
                    spatialJoin(spatialIndex, queryBoxSize);
                }
            }
        }
    }

    private SpatialIndex<TestRecord> loadDB(int dataBoxSide) throws IOException, InterruptedException
    {
        BoxGenerator dataGenerator = new BoxGenerator(SPACE, new Random(dataBoxSide), dataBoxSide, dataBoxSide);
        TestIndex index = new TestIndex();
        SpatialIndex<TestRecord> spatialIndex = SpatialIndex.newSpatialIndex(SPACE, index);
        for (int id = 0; id < BOXES; id++) {
            Box box = (Box) dataGenerator.newSpatialObject();
            spatialIndex.add(box, RECORD_FACTORY.setup(box, id));
        }
        // dumpIndex(index);
        return spatialIndex;
    }

    private void spatialJoin(SpatialIndex<TestRecord> dataIndex, int queryBoxSize) throws IOException, InterruptedException
    {
        BoxGenerator queryGenerator = new BoxGenerator(SPACE, new Random(queryBoxSize), queryBoxSize, queryBoxSize);
        SpatialIndex<TestRecord> queryIndex = SpatialIndex.newSpatialIndex(SPACE, new TestIndex());
        Box query = (Box) queryGenerator.newSpatialObject();
        queryIndex.add(query, RECORD_FACTORY.setup(query, 0));
        RandomAccessMonitor dataObserver = new RandomAccessMonitor(query);
        Iterator<TestRecord> iterator =
            SpatialJoin
                .newSpatialJoin(SpatialJoin.Duplicates.INCLUDE, null, null, dataObserver)
                .iterator(query, dataIndex);
        // Run the spatial join
        while (iterator.hasNext()) {
            iterator.next();
        }
/*
        System.out.println("Predicted");
        for (Long z : dataObserver.predictedRandomAccesses()) {
            System.out.format("    %s\n", SpaceImpl.formatZ(z));
        }
        System.out.println("Unexpected");
        for (Long z : dataObserver.unexpectedRandomAccesses()) {
            System.out.format("    %s\n", SpaceImpl.formatZ(z));
        }
        System.out.format("ancestorFound: %d\nancestorNotFound: %d\n",
                          dataObserver.ancestorFound(),
                          dataObserver.ancestorNotFound());
*/
        assertTrue(dataObserver.unexpectedRandomAccesses().isEmpty());
    }

    private void dumpIndex(TestIndex index) throws IOException, InterruptedException
    {
        System.out.println("DATA");
        Cursor<TestRecord> cursor = index.cursor();
        TestRecord minZ = index.newKeyRecord();
        minZ.z(SpaceImpl.Z_MIN);
        cursor.goTo(minZ);
        TestRecord record;
        while ((record = cursor.next()) != null) {
            indent(record.z());
            System.out.format("%s: %s\n", formatZ(record.z()), record.spatialObject());
        }
        System.out.println();
    }

    private static void indent(long z)
    {
        int n = SpaceImpl.length(z);
        for (int i = 0; i < n; i++) {
            System.out.print(' ');
        }
    }

    private static final int QUERIES = 1000;
    private static final int BOXES = 10000;
    private static final int MAX_DATA_BOX_SIDE = 50000;
    private static final int DELTA_DATA_BOX_SIDE = 10000;
    private static final int MAX_QUERY_BOX_SIDE = 50000;
    private static final int DELTA_QUERY_BOX_SIDE = 10000;
    private static final int NX = 1_000_000;
    private static final int NY = 1_000_000;
    private static final int X_BITS = 20;
    private static final int Y_BITS = 20;
    private static final Space SPACE = Space.newSpace(new double[]{0, 0},
                                                      new double[]{NX, NY},
                                                      new int[]{X_BITS, Y_BITS});
    private static final TestRecord.Factory RECORD_FACTORY = new TestRecord.Factory();

    // Inner classes

    private static class RandomAccessMonitor extends SpatialJoin.InputObserver
    {
        @Override
        public void randomAccess(Cursor cursor, long z)
        {
            // System.out.format("    %s -> %s\n", cursor, formatZ(z));
            ZRun zRun = predictedRandomAccesses.get(z);
            if (zRun == null) {
                unexpectedRandomAccesses.add(z);
            } else {
                zRun.randomAccess(z);
            }
        }

        @Override
        public void sequentialAccess(Cursor cursor, long zRandomAccess, Record record)
        {
            // System.out.format("        %s.next: %s -> %s\n", cursor, formatZ(zRandomAccess), formatZ(zSequentialAccess));
            if (record != null) {
                ZRun zRun = predictedRandomAccesses.get(zRandomAccess);
                if (zRun == null) {
                    fail();
                } else {
                    zRun.sequentialAccess(record);
                }
            }
        }

        @Override
        public void ancestorSearch(Cursor cursor, long zStart, long zAncestor)
        {
            if (zAncestor == SpaceImpl.Z_NULL) {
                ancestorNotFound++;
            } else {
                ancestorFound++;
            }
        }

        public Set<Long> predictedRandomAccesses()
        {
            return predictedRandomAccesses.keySet();
        }

        public int ancestorFound()
        {
            return ancestorFound;
        }

        public int ancestorNotFound()
        {
            return ancestorNotFound;
        }

        public List<Long> unexpectedRandomAccesses()
        {
            Collections.sort(unexpectedRandomAccesses);
            return unexpectedRandomAccesses;
        }

        public RandomAccessMonitor(SpatialObject spatialObject)
        {
            predictedRandomAccesses.put(0L, new ZRun(0L)); // Simplifies loop
            long[] zs = new long[spatialObject.maxZ()];
            SPACE.decompose(spatialObject, zs);
            for (int i = 0; i < zs.length && zs[i] != Space.Z_NULL; i++) {
                long z = zs[i];
                do {
                    predictedRandomAccesses.put(z, new ZRun(z));
                    z = SpaceImpl.parent(z);
                } while (SpaceImpl.length(z) > 0);
            }
        }

        private final Map<Long, ZRun> predictedRandomAccesses = new TreeMap<>();
        private final List<Long> unexpectedRandomAccesses = new ArrayList<>();
        private int ancestorFound;
        private int ancestorNotFound;

        // Tracks a run of z-values, starting with random access at a predicted z-value.
        private static class ZRun
        {
            public ZRun(long zPredicted)
            {
                this.zPredicted = zPredicted;
            }

            public void randomAccess(long z)
            {
                // OK as long as we haven't moved past the first.
                assertTrue(zMax == -1L || zMax == zFirst);
            }

            public void sequentialAccess(Record record)
            {
                long z = record.z();
                assertTrue(z >= zPredicted);
                if (zFirst == Z_NULL) {
                    assertEquals(Z_NULL, zMax);
                    zFirst = z;
                    zMax = z;
                } else if (z == zFirst) {
                    // OK, a revisit
                } else if (z > zFirst) {
                    if (z < zMax) {
                        fail();
                    } else {
                        zMax = z;
                    }
                }
            }

            private final long zPredicted;
            private long zFirst = Z_NULL; // first after zPredicted
            private long zMax = Z_NULL; // max z-value seen in sequential accesses starting at zPredicted
        }
    }
}