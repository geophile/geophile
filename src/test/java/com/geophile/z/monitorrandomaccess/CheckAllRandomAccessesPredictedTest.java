/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.monitorrandomaccess;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class CheckAllRandomAccessesPredictedTest
{
    @Test
    public void test() throws IOException, InterruptedException
    {
        SpatialIndex<TestRecord> spatialIndex = loadDB();
        for (int q = 0; q < QUERIES; q++) {
            spatialJoin(spatialIndex);
        }
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
        return spatialIndex;
    }

    private void spatialJoin(SpatialIndex<TestRecord> dataIndex) throws IOException, InterruptedException
    {
        SpatialIndex<TestRecord> queryIndex = SpatialIndex.newSpatialIndex(SPACE, new TestIndex());
        Box query = (Box) queryGenerator.newSpatialObject();
        TestRecord record = new TestRecord(query, 0);
        queryIndex.add(query, record);
        CheckPredictedRandomAccesses dataObserver = new CheckPredictedRandomAccesses(query);
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
        for (Long z : dataObserver.predictRandomAccesses()) {
            System.out.format("    %s\n", SpaceImpl.formatZ(z));
        }
        System.out.println("Unexpected");
        for (Long z : dataObserver.unexpectedRandomAccesses()) {
            System.out.format("    %s\n", SpaceImpl.formatZ(z));
        }
*/
        assertTrue(dataObserver.unexpectedRandomAccesses().isEmpty());
    }

    private static final int QUERIES = 1000;
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

    private final BoxGenerator dataGenerator = new BoxGenerator(SPACE, new Random(1234567), DATA_BOX_SIDE, DATA_BOX_SIDE);
    private final BoxGenerator queryGenerator = new BoxGenerator(SPACE, new Random(7654321), QUERY_BOX_SIDE, QUERY_BOX_SIDE);

    // Inner classes

    private static class CheckPredictedRandomAccesses implements SpatialJoin.InputObserver
    {
        @Override
        public void randomAccess(long z)
        {
            if (!predictedRandomAccesses.contains(z)) {
                unexpectedRandomAccesses.add(z);
            }
        }

        public List<Long> predictedRandomAccesses()
        {
            List<Long> sorted = new ArrayList<>(predictedRandomAccesses);
            Collections.sort(sorted);
            return sorted;
        }

        public List<Long> unexpectedRandomAccesses()
        {
            Collections.sort(unexpectedRandomAccesses);
            return unexpectedRandomAccesses;
        }

        public CheckPredictedRandomAccesses(SpatialObject spatialObject)
        {
            predictedRandomAccesses.add(0L); // Simplifies loop
            long[] zs = new long[spatialObject.maxZ()];
            SPACE.decompose(spatialObject, zs);
            for (int i = 0; i < zs.length && zs[i] != Space.Z_NULL; i++) {
                long z = zs[i];
                do {
                    predictedRandomAccesses.add(z);
                    z = SpaceImpl.parent(z);
                } while (SpaceImpl.length(z) > 0);
            }
        }

        private final Set<Long> predictedRandomAccesses = new HashSet<>();
        private final List<Long> unexpectedRandomAccesses = new ArrayList<>();
    }
}