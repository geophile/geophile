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

import com.geophile.z.ApplicationSpace;
import com.geophile.z.Space;
import com.geophile.z.SpatialJoin;
import com.geophile.z.SpatialObject;
import com.geophile.z.spatialobject.d2.Box;

import java.io.IOException;
import java.util.Random;

public class SpatialJoinManyPointsOneBox extends SpatialJoinIteratorTestBase
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        new SpatialJoinManyPointsOneBox().run();
    }

    private void run() throws IOException, InterruptedException
    {
/*
        // without single-cell optimization - warmup
        for (int w = 0; w < 10; w++) {
            run(false, true);
        }
        // without single-cell optimization - measure
        run(false, false);
*/
        // with single-cell optimization - measure
        for (int w = 0; w < 10; w++) {
            run(true, true);
        }
        // with single-cell optimization - measure
        run(true, false);
    }

    private void run(boolean singleCell, boolean warmup) throws IOException, InterruptedException
    {
        random = new Random(12345);
        System.setProperty(SpatialJoinImpl.SINGLE_CELL_OPTIMIZATION_PROPERTY, Boolean.toString(singleCell));
        Counters counters = Counters.forThread();
        counters.reset();
        testStats.resetAll();
        final int TRIALS = 100;
        final int N_POINTS = 1_000_000;
        testStats.loadTimeMsec = 0;
        TestInput rightInput = load(Side.RIGHT, N_POINTS, 1, 1);
        int maxSize = warmup ? 1000 : 64000;
        for (int size = 1000; size <= maxSize; size *= 2) {
            TestInput leftInput = load(Side.LEFT, 1, size, size);
            for (int trial = 0; trial < TRIALS; trial++) {
                test(leftInput, rightInput, filter, SpatialJoin.Duplicates.INCLUDE);
            }
            double loadMsecPerPoint = (double) testStats.loadTimeMsec / N_POINTS;
            double averageJoinMsec = (double) testStats.joinTimeNsec / (TRIALS * 1_000_000);
            double averageOutputRowCount = (double) testStats.outputRowCount / TRIALS;
            if (!warmup) {
                print("singlecell: %s\tload msec/point: %s\tjoin msec: %s\tancestor: %s\tenter: %s\toutput size: %s",
                      singleCell,
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
        return space;
    }

    @Override
    protected Box newLeftObject(int xSize, int ySize)
    {
        long xLo = random.nextInt(NX - xSize + 1);
        long xHi = xLo + xSize - 1;
        long yLo = random.nextInt(NY - ySize + 1);
        long yHi = yLo + ySize - 1;
        return new Box(xLo, xHi, yLo, yHi);
    }

    @Override
    protected void checkEquals(Object expected, Object actual)
    {
        assert expected.equals(actual);
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

    private static final int NX = 1_000_000;
    private static final int NY = 1_000_000;

    private final Space space = Space.newSpace(appSpace(0, NX, 0, NY), NX, NY);
    private final SpatialJoinFilter filter = new SpatialJoinFilter()
    {
        @Override
        public boolean overlap(SpatialObject x, SpatialObject y)
        {
            testStats.filterCount++;
            boolean overlap = ((Box) x).overlap(((Box) y));
            if (overlap) {
                testStats.overlapCount++;
            }
            return overlap;
        }
    };
    private Random random;
}
