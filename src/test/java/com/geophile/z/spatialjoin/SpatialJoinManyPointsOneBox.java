/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialjoin;

import com.geophile.util.MicroBenchmark;
import com.geophile.z.Pair;
import com.geophile.z.Space;
import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialJoin;
import com.geophile.z.SpatialObject;
import com.geophile.z.index.RecordWithSpatialObject;
import com.geophile.z.index.sortedarray.SortedArray;
import com.geophile.z.spatialobject.d2.Box;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

// For time measurement

public class SpatialJoinManyPointsOneBox
{
    public static void main(String[] args) throws Exception
    {
        new SpatialJoinManyPointsOneBox().run();
    }

    private void run() throws Exception
    {
        BoxGenerator pointGenerator = new BoxGenerator(SPACE, random, 1, 1);
        final SpatialIndex<RecordWithSpatialObject> dataIndex = loadSpatialIndex(N_POINTS, pointGenerator);
        for (int size = MIN_QUERY_BOX_SIZE; size <= MAX_QUERY_BOX_SIZE; size *= 2) {
            BoxGenerator boxGenerator = new BoxGenerator(SPACE, random, size, size);
            final Box query = (Box) boxGenerator.newSpatialObject();
            double manyManyJoinMsec;
            double oneManyJoinMsec;
            // Many/many join
            {
                MicroBenchmark benchmark =
                    new MicroBenchmark(10, 0.10)
                    {
                        @Override
                        public Object action() throws IOException, InterruptedException
                        {
                            SpatialIndex<RecordWithSpatialObject> queryIndex =
                                SpatialIndex.newSpatialIndex(SPACE, new SortedArray.OfBaseRecord());
                            queryIndex.add(query, RECORD_FACTORY.setup(query));
                            for (int trial = 0; trial < TRIALS; trial++) {
                                Iterator<Pair<RecordWithSpatialObject, RecordWithSpatialObject>> iterator =
                                    SpatialJoin.newSpatialJoin(SpatialJoin.Duplicates.INCLUDE, manyManyFilter)
                                               .iterator(queryIndex, dataIndex);
                                while (iterator.hasNext()) {
                                    iterator.next();
                                }
                            }
                            return null;
                        }
                    };
                manyManyJoinMsec = benchmark.run() / TRIALS / 1000;
            }
            // One/many join
            {
                MicroBenchmark benchmark =
                    new MicroBenchmark(10, 0.10)
                    {
                        @Override
                        public Object action() throws IOException, InterruptedException
                        {
                            for (int trial = 0; trial < TRIALS; trial++) {
                                Iterator<RecordWithSpatialObject> iterator =
                                    SpatialJoin.newSpatialJoin(SpatialJoin.Duplicates.INCLUDE, oneManyFilter)
                                               .iterator(query, dataIndex);
                                while (iterator.hasNext()) {
                                    iterator.next();
                                }
                            }
                            return null;
                        }
                    };
                oneManyJoinMsec = benchmark.run() / TRIALS / 1000;
            }
            System.out.format("%d x %d\t\tmany/many: %f msec\tone/many: %f msec\n",
                              size, size, manyManyJoinMsec, oneManyJoinMsec);
        }
    }
    private SpatialIndex<RecordWithSpatialObject> loadSpatialIndex(int n, SpatialObjectGenerator generator)
        throws IOException, InterruptedException
    {
        SpatialIndex<RecordWithSpatialObject> spatialIndex = SpatialIndex.newSpatialIndex(SPACE, new SortedArray.OfBaseRecord());
        for (int i = 0; i < n; i++) {
            SpatialObject spatialObject = generator.newSpatialObject();
            spatialIndex.add(spatialObject, RECORD_FACTORY.setup(spatialObject));
        }
        return spatialIndex;
    }

    private static final int NX = 1_000_000;
    private static final int NY = 1_000_000;
    private static final int X_BITS = 20;
    private static final int Y_BITS = 20;
    private static final Space SPACE = Space.newSpace(new double[]{0, 0},
                                                      new double[]{NX, NY},
                                                      new int[]{X_BITS, Y_BITS});
    private static final RecordWithSpatialObject.Factory RECORD_FACTORY = new RecordWithSpatialObject.Factory();

    private static final int TRIALS = 200;
    private static final int N_POINTS = 1_000_000;
    private static final int MIN_QUERY_BOX_SIZE = 1000;
    private static final int MAX_QUERY_BOX_SIZE = 64000;

    private final BoxOverlapTester overlapTester = new BoxOverlapTester();
    private final SpatialJoin.Filter<RecordWithSpatialObject, RecordWithSpatialObject> manyManyFilter =
        new SpatialJoin.Filter<RecordWithSpatialObject, RecordWithSpatialObject>()
    {
        @Override
        public boolean overlap(RecordWithSpatialObject left, RecordWithSpatialObject right)
        {
            testStats.filterCount++;
            boolean overlap = overlapTester.overlap(left.spatialObject(), right.spatialObject());
            if (overlap) {
                testStats.overlapCount++;
            }
            return overlap;
        }
    };
    private final SpatialJoin.Filter<SpatialObject, RecordWithSpatialObject> oneManyFilter =
        new SpatialJoin.Filter<SpatialObject, RecordWithSpatialObject>()
    {
        @Override
        public boolean overlap(SpatialObject left, RecordWithSpatialObject right)
        {
            testStats.filterCount++;
            boolean overlap = overlapTester.overlap(left, right.spatialObject());
            if (overlap) {
                testStats.overlapCount++;
            }
            return overlap;
        }
    };
    private final TestStats testStats = new TestStats();
    private Random random = new Random(12345);
}
