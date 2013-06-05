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

import com.geophile.z.SpatialJoin;
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
        // warmup
        run(false, true);
        run(true, true);
        run(false, true);
        run(true, true);
        // measure
        run(false, false);
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
        final int SIZE = 50_000;
        TestInput leftInput = loadBoxes(1, SIZE, SIZE);
        testStats.loadTimeMsec = 0;
        TestInput rightInput = loadBoxes(N_POINTS, 1, 1);
        for (int trial = 0; trial < TRIALS; trial++) {
            test(leftInput, rightInput, SpatialJoin.Duplicates.INCLUDE);
        }
        double loadMsecPerPoint = (double) testStats.loadTimeMsec / N_POINTS;
        double averageJoinMsec = (double) testStats.joinTimeMsec / TRIALS;
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

    @Override
    protected Box randomBox(int xSize, int ySize)
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
    protected boolean verify()
    {
        return false;
    }

    @Override
    protected boolean printSummary()
    {
        return false;
    }

    private Random random;
}
