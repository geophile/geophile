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
import com.geophile.z.index.tree.TreeIndex;
import com.geophile.z.spatialobject.d2.Box;

import java.io.IOException;
import java.util.Random;

// For time measurement

public class SpatialJoinManyPointsOneBox extends SpatialJoinTestBase
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        new SpatialJoinManyPointsOneBox().run();
    }

    private void run() throws IOException, InterruptedException
    {
        for (int w = 0; w < 10; w++) {
            run(true);
        }
        run(false);
    }

    private void run(boolean warmup) throws IOException, InterruptedException
    {
        final int TRIALS = 100;
        final int N_POINTS = 1_000_000;
        SpatialJoin spatialJoin = SpatialJoin.newSpatialJoin(filter, SpatialJoin.Duplicates.INCLUDE);
        Counters counters = Counters.forThread();
        counters.reset();
        testStats.resetAll();
        testStats.loadTimeMsec = 0;
        BoxGenerator pointGenerator = new BoxGenerator(SPACE, random, 1, 1);
        TestInput rightInput = newTestInput(N_POINTS, pointGenerator);
        int maxSize = warmup ? 1000 : 64000;
        for (int size = 1000; size <= maxSize; size *= 2) {
            BoxGenerator boxGenerator = new BoxGenerator(SPACE, random, size, size);
            TestInput leftInput = newTestInput(1, boxGenerator);
            for (int trial = 0; trial < TRIALS; trial++) {
                testJoin(spatialJoin, leftInput, rightInput);
            }
            double loadMsecPerPoint = (double) testStats.loadTimeMsec / N_POINTS;
            double averageJoinMsec = (double) testStats.joinTimeNsec / (TRIALS * 1_000_000);
            double averageOutputRowCount = (double) testStats.outputRowCount / TRIALS;
            if (!warmup) {
                print("load msec/point: %s\tjoin msec: %s\tancestor: %s\tenter: %s\toutput size: %s",
                      loadMsecPerPoint,
                      averageJoinMsec,
                      counters.ancestorFind(),
                      counters.enterZ(),
                      averageOutputRowCount);
            }
        }
    }

    @Override
    protected Space space()
    {
        return SPACE;
    }

    @Override
    protected Index newIndex()
    {
        return new TreeIndex();
    }

    @Override
    protected boolean overlap(SpatialObject x, SpatialObject y)
    {
        Box a = (Box) x;
        Box b = (Box) y;
        return
            a.xLo() <= b.xHi() && b.xLo() <= a.xHi() &&
            a.yLo() <= b.yHi() && b.yLo() <= a.yHi();
    }

    @Override
    protected boolean verify()
    {
        return false;
    }

    @Override
    protected boolean printSummary()
    {
        return false;
    }

    @Override
    protected boolean trace()
    {
        return false;
    }

    @Override
    protected void checkEquals(Object expected, Object actual)
    {
        assert expected.equals(actual);
    }

    private TestInput newTestInput(int n, BoxGenerator boxGenerator) throws IOException, InterruptedException
    {
        Index index = new TreeIndex();
        SpatialIndex spatialIndex = SpatialIndex.newSpatialIndex(SPACE, index);
        TestInput testInput = new TestInput(spatialIndex, boxGenerator.description());
        load(n, boxGenerator, testInput);
        return testInput;
    }

    private static final int NX = 1_000_000;
    private static final int NY = 1_000_000;
    private static final int X_BITS = 20;
    private static final int Y_BITS = 20;
    private static final Space SPACE = Space.newSpace(new double[]{0, 0},
                                                      new double[]{NX, NY},
                                                      new int[]{X_BITS, Y_BITS});

    private final BoxOverlapTester overlapTester = new BoxOverlapTester();
    private final SpatialJoinFilter filter = new SpatialJoinFilter()
    {
        @Override
        public boolean overlap(SpatialObject x, SpatialObject y)
        {
            testStats.filterCount++;
            boolean overlap = overlapTester.overlap(x, y);
            if (overlap) {
                testStats.overlapCount++;
            }
            return overlap;
        }
    };
    private final TestStats testStats = new TestStats();
    private Random random = new Random(12345);
}
