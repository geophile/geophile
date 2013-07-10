/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialjoin;

import com.geophile.z.*;
import com.geophile.z.index.treeindex.TreeIndex;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

public class SpatialJoinJTSTest extends SpatialJoinTestBase
{
    @Test
    public void testManyPointOnePolygon() throws IOException, InterruptedException
    {
        SpatialJoin spatialJoin = SpatialJoin.newSpatialJoin(filter, SpatialJoin.Duplicates.EXCLUDE);
        JTSPointGenerator dataGenerator = new JTSPointGenerator(SPACE, FACTORY, random);
        TestInput dataInput = newTestInput(COUNT, dataGenerator);
        for (int boxSize : BOX_SIZES) {
            for (int trial = 0; trial < TRIALS; trial++) {
                JTSSquareGenerator queryGenerator = new JTSSquareGenerator(SPACE, FACTORY, random, boxSize);
                TestInput queryInput = newTestInput(1, queryGenerator);
                testJoin(spatialJoin, queryInput, dataInput);
            }
        }
    }

    @Test
    public void testManyLineStringsManyLineStrings() throws IOException, InterruptedException
    {
        SpatialJoin spatialJoin = SpatialJoin.newSpatialJoin(filter, SpatialJoin.Duplicates.EXCLUDE);
        JTSLineStringGenerator generator =
            new JTSLineStringGenerator(SPACE, FACTORY, random, X_MAX_SEGMENT_DISTANCE, Y_MAX_SEGMENT_DISTANCE);
        TestInput leftInput = newTestInput(COUNT, generator);
        for (int trial = 0; trial < TRIALS; trial++) {
            TestInput rightInput = newTestInput(COUNT, generator);
            testJoin(spatialJoin, rightInput, leftInput);
        }
    }

    @Test
    public void testManyPolygonsManyPolygons() throws IOException, InterruptedException
    {
        SpatialJoin spatialJoin = SpatialJoin.newSpatialJoin(filter, SpatialJoin.Duplicates.EXCLUDE);
        JTSPolygonGenerator generator =
            new JTSPolygonGenerator(SPACE, FACTORY, random, X_MAX_SEGMENT_DISTANCE, Y_MAX_SEGMENT_DISTANCE);
        TestInput leftInput = newTestInput(COUNT, generator);
        for (int trial = 0; trial < TRIALS; trial++) {
            TestInput rightInput = newTestInput(COUNT, generator);
            testJoin(spatialJoin, rightInput, leftInput);
        }
    }

    @Test
    public void testManyMultiPointOnePolygon() throws IOException, InterruptedException
    {
        SpatialJoin spatialJoin = SpatialJoin.newSpatialJoin(filter, SpatialJoin.Duplicates.EXCLUDE);
        JTSMultiPointGenerator dataGenerator = new JTSMultiPointGenerator(SPACE, FACTORY, random, 1000, 1000);
        TestInput dataInput = newTestInput(COUNT, dataGenerator);
        for (int boxSize : BOX_SIZES) {
            for (int trial = 0; trial < TRIALS; trial++) {
                JTSSquareGenerator queryGenerator = new JTSSquareGenerator(SPACE, FACTORY, random, boxSize);
                TestInput queryInput = newTestInput(1, queryGenerator);
                testJoin(spatialJoin, queryInput, dataInput);
            }
        }
    }

    @Override
    protected boolean overlap(SpatialObject s, SpatialObject t)
    {
        return OVERLAP_TESTER.overlap(s, t);
    }

    @Override
    protected final boolean verify()
    {
        return true;
    }

    @Override
    protected final boolean printSummary()
    {
        return false;
    }

    @Override
    protected boolean trace()
    {
        return false;
    }

    private TestInput newTestInput(int n, SpatialObjectGenerator spatialObjectGenerator)
        throws IOException, InterruptedException
    {
        Index index = new TreeIndex();
        SpatialIndex spatialIndex = SpatialIndex.newSpatialIndex(SPACE, index);
        TestInput testInput = new TestInput(spatialIndex, spatialObjectGenerator.description());
        load(n, spatialObjectGenerator, testInput);
        return testInput;
    }


    private static final int COUNT = 10_000;
    private static final int NX = 1_000_000;
    private static final int NY = 1_000_000;
    private static final int X_BITS = 20;
    private static final int Y_BITS = 20;
    private static final int TRIALS = 20;
    private static final int[] BOX_SIZES = new int[]{20_000, 50_000, 100_000};
    // max x-distance between two successive points of the same shape.
    private static final int X_MAX_SEGMENT_DISTANCE = 1_000;
    // max y-distance between two successive points of the same shape.
    private static final int Y_MAX_SEGMENT_DISTANCE = 1_000;
    private static final JTSOverlapTester OVERLAP_TESTER = new JTSOverlapTester();
    private static final GeometryFactory FACTORY = new GeometryFactory();
    private static final ApplicationSpace APP_SPACE = appSpace(0, NX, 0, NY);
    private static final Space SPACE = Space.newSpace(APP_SPACE, X_BITS, Y_BITS);

    private final Random random = new Random(123456);
    private final SpatialJoinFilter filter = new SpatialJoinFilter()
    {
        @Override
        public boolean overlap(SpatialObject x, SpatialObject y)
        {
            testStats.filterCount++;
            boolean overlap = OVERLAP_TESTER.overlap(x, y);
            if (overlap) {
                testStats.overlapCount++;
            }
            return overlap;
        }
    };
}
