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

import com.geophile.z.Index;
import com.geophile.z.JTSLineStringGenerator;
import com.geophile.z.JTSMultiPointGenerator;
import com.geophile.z.JTSPointGenerator;
import com.geophile.z.JTSPolygonGenerator;
import com.geophile.z.JTSSquareGenerator;
import com.geophile.z.Space;
import com.geophile.z.SpatialJoin;
import com.geophile.z.SpatialObject;
import com.geophile.z.TestIndex;
import com.geophile.z.TestRecord;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

public class SpatialJoinJTSTest extends SpatialJoinTestBase
{
    @Test
    public void testManyPointOnePolygon() throws IOException, InterruptedException
    {
        JTSPointGenerator dataGenerator = new JTSPointGenerator(SPACE, FACTORY, random);
        TestInput dataInput = newTestInput(COUNT, dataGenerator);
        for (int boxSize : BOX_SIZES) {
            for (int trial = 0; trial < TRIALS; trial++) {
                JTSSquareGenerator queryGenerator = new JTSSquareGenerator(SPACE, FACTORY, random, boxSize);
                TestInput queryInput = newTestInput(1, queryGenerator);
                testJoin(queryInput, dataInput, manyManyFilter, oneManyFilter, SpatialJoin.Duplicates.EXCLUDE);
            }
        }
    }

    @Test
    public void testManyLineStringsManyLineStrings() throws IOException, InterruptedException
    {
        JTSLineStringGenerator generator =
            new JTSLineStringGenerator(SPACE, FACTORY, random, X_MAX_SEGMENT_DISTANCE, Y_MAX_SEGMENT_DISTANCE);
        TestInput leftInput = newTestInput(COUNT, generator);
        for (int trial = 0; trial < TRIALS; trial++) {
            TestInput rightInput = newTestInput(COUNT, generator);
            testJoin(rightInput, leftInput, manyManyFilter, oneManyFilter, SpatialJoin.Duplicates.EXCLUDE);
        }
    }

    @Test
    public void testManyPolygonsManyPolygons() throws IOException, InterruptedException
    {
        JTSPolygonGenerator generator =
            new JTSPolygonGenerator(SPACE, FACTORY, random, X_MAX_SEGMENT_DISTANCE, Y_MAX_SEGMENT_DISTANCE);
        TestInput leftInput = newTestInput(COUNT, generator);
        for (int trial = 0; trial < TRIALS; trial++) {
            TestInput rightInput = newTestInput(COUNT, generator);
            testJoin(rightInput, leftInput, manyManyFilter, oneManyFilter, SpatialJoin.Duplicates.EXCLUDE);
        }
    }

    @Test
    public void testManyMultiPointOnePolygon() throws IOException, InterruptedException
    {
        JTSMultiPointGenerator dataGenerator = new JTSMultiPointGenerator(SPACE, FACTORY, random, 1000, 1000);
        TestInput dataInput = newTestInput(COUNT, dataGenerator);
        for (int boxSize : BOX_SIZES) {
            for (int trial = 0; trial < TRIALS; trial++) {
                JTSSquareGenerator queryGenerator = new JTSSquareGenerator(SPACE, FACTORY, random, boxSize);
                TestInput queryInput = newTestInput(1, queryGenerator);
                testJoin(queryInput, dataInput, manyManyFilter, oneManyFilter, SpatialJoin.Duplicates.EXCLUDE);
            }
        }
    }

    @Override
    protected Space space()
    {
        return SPACE;
    }

    @Override
    protected Index<TestRecord> newIndex(boolean stableRecords)
    {
        return new TestIndex();
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
    private static final Space SPACE = Space.newSpace(new double[]{0, 0},
                                                      new double[]{NX, NY},
                                                      new int[]{X_BITS, Y_BITS});

    private final Random random = new Random(123456);
    private final SpatialJoin.Filter<TestRecord, TestRecord> manyManyFilter =
        new SpatialJoin.Filter<TestRecord, TestRecord>()
    {
        @Override
        public boolean overlap(TestRecord left, TestRecord right)
        {
            testStats.filterCount++;
            boolean overlap = OVERLAP_TESTER.overlap(left.spatialObject(), right.spatialObject());
            if (overlap) {
                testStats.overlapCount++;
            }
            return overlap;
        }
    };
    private final SpatialJoin.Filter<SpatialObject, TestRecord> oneManyFilter =
        new SpatialJoin.Filter<SpatialObject, TestRecord>()
    {
        @Override
        public boolean overlap(SpatialObject left, TestRecord right)
        {
            testStats.filterCount++;
            boolean overlap = OVERLAP_TESTER.overlap(left, right.spatialObject());
            if (overlap) {
                testStats.overlapCount++;
            }
            return overlap;
        }
    };
}
