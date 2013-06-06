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

public class SpatialJoinIteratorProfile extends SpatialJoinIteratorTestBase
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        new SpatialJoinIteratorProfile().run();
    }

    private void run() throws IOException, InterruptedException
    {
        // enableLogging(Level.FINE);
        final int TRIALS = 1_000_000;
        final int N_POINTS = 1_000_000;
        final int MAX_Z = 8;
        final int SIZE = 5_000;
        System.setProperty("maxz", Integer.toString(MAX_Z));
        System.setProperty(SpatialJoinImpl.SINGLE_CELL_OPTIMIZATION_PROPERTY, "true");
        TestInput leftInput = loadBoxes(1, SIZE, SIZE);
        TestInput rightInput = loadBoxes(N_POINTS, 1, 1);
        testStats.resetAll();
        for (int trial = 0; trial < TRIALS; trial++) {
            test(leftInput, rightInput, SpatialJoin.Duplicates.INCLUDE);
        }
        double joinTimeMsec = (double) testStats.joinTimeNsec / (TRIALS * 1_000_000L);
        print("points: %s, maxZ: %s, box size: %s, join time: %s msec",
              N_POINTS, MAX_Z, SIZE, joinTimeMsec);
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
}
