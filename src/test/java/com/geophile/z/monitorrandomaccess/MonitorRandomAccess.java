/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.monitorrandomaccess;

import com.geophile.z.Cursor;
import com.geophile.z.Space;
import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialJoin;
import com.geophile.z.TestIndex;
import com.geophile.z.TestRecord;
import com.geophile.z.space.SpaceImpl;
import com.geophile.z.spatialjoin.BoxGenerator;
import com.geophile.z.spatialobject.d2.Box;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.geophile.z.space.SpaceImpl.formatZ;

public class MonitorRandomAccess
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        new MonitorRandomAccess().run();
    }

    private void run() throws IOException, InterruptedException
    {
        SpatialIndex<TestRecord> spatialIndex = loadDB();
        spatialJoin(spatialIndex);
    }

    private SpatialIndex<TestRecord> loadDB() throws IOException, InterruptedException
    {
        TestIndex index = new TestIndex();
        SpatialIndex<TestRecord> spatialIndex = SpatialIndex.newSpatialIndex(SPACE, index);
        for (int id = 0; id < BOXES; id++) {
            Box box = (Box) dataGenerator.newSpatialObject();
            TestRecord record = new TestRecord(box, id);
            spatialIndex.add(box, record);
        }
        Cursor<TestRecord> cursor = index.cursor();
        TestRecord zMinKey = index.newKeyRecord();
        zMinKey.z(SpaceImpl.Z_MIN);
        cursor.goTo(zMinKey);
        TestRecord record;
        System.out.println("DATA");
        while ((record = cursor.next()) != null) {
            System.out.format("    %s: %s\n", formatZ(record.z()), record.spatialObject());
        }
        return spatialIndex;
    }

    private void spatialJoin(SpatialIndex<TestRecord> dataIndex) throws IOException, InterruptedException
    {
        SpatialIndex<TestRecord> queryIndex = SpatialIndex.newSpatialIndex(SPACE, new TestIndex());
        Box query = (Box) queryGenerator.newSpatialObject();
        TestRecord record = new TestRecord(query, 0);
        queryIndex.add(query, record);
        RandomAccessObserver queryObserver = new RandomAccessObserver();
        RandomAccessObserver dataObserver = new RandomAccessObserver();
        Iterator<TestRecord> iterator =
            SpatialJoin
                .newSpatialJoin(SpatialJoin.Duplicates.INCLUDE, null, queryObserver, dataObserver)
                .iterator(query, dataIndex);
        // System.out.format("Query: %s\n", query);
        while (iterator.hasNext()) {
            record = iterator.next();
            // System.out.format("    %s\n", record.spatialObject());
        }
        // Z values from decomposition, and prefixes
        {
            System.out.println("Query");
            long[] zs = new long[query.maxZ()];
            SPACE.decompose(query, zs);
            for (int i = 0; i < zs.length && zs[i] != Space.Z_NULL; i++) {
                System.out.format("    %s\n", formatZ(zs[i]));
            }
        }
        // Random accesses
        {
            System.out.println("DATA random accesses");
            Map<Long, Integer> randomAccesses = dataObserver.randomAccesses();
            List<Long> zs = new ArrayList<>(randomAccesses.keySet());
            Collections.sort(zs);
            for (Long z : zs) {
                System.out.format("    %s: %d\n", formatZ(z), randomAccesses.get(z));
            }
        }
        {
            System.out.println("QUERY random accesses");
            Map<Long, Integer> randomAccesses = queryObserver.randomAccesses();
            List<Long> zs = new ArrayList<>(randomAccesses.keySet());
            Collections.sort(zs);
            for (Long z : zs) {
                System.out.format("    %s: %d\n", formatZ(z), randomAccesses.get(z));
            }
        }
    }

    private static final int BOXES = 10000;
    private static final int DATA_BOX_SIDE = 100;
    private static final int QUERY_BOX_SIDE = 20000;
    private static final int NX = 1_000_000;
    private static final int NY = 1_000_000;
    private static final int X_BITS = 20;
    private static final int Y_BITS = 20;
    private static final Space SPACE = Space.newSpace(new double[]{0, 0},
                                                      new double[]{NX, NY},
                                                      new int[]{X_BITS, Y_BITS});

    private final BoxGenerator dataGenerator = new BoxGenerator(SPACE, new Random(123456), DATA_BOX_SIDE, DATA_BOX_SIDE);
    private final BoxGenerator queryGenerator = new BoxGenerator(SPACE, new Random(654321), QUERY_BOX_SIDE, QUERY_BOX_SIDE);

    // Inner classes

    private static class RandomAccessObserver implements SpatialJoin.InputObserver
    {
        @Override
        public void randomAccess(long z)
        {
            Integer count = randomAccesses.get(z);
            count = count == null ? 1 : count + 1;
            randomAccesses.put(z, count);
        }

        public Map<Long, Integer> randomAccesses()
        {
            return randomAccesses;
        }

        private final Map<Long, Integer> randomAccesses = new HashMap<>();
    }
}