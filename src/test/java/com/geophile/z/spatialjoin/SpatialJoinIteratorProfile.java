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

import com.geophile.z.Space;
import com.geophile.z.SpatialJoin;
import com.geophile.z.SpatialObject;
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
        final int TRIALS = 100_000;
        final int N_POINTS = 1_000_000;
        final int MAX_Z = 8;
        final int SIZE = 75_000;
        System.setProperty("maxz", Integer.toString(MAX_Z));
        System.setProperty(SpatialJoinImpl.SINGLE_CELL_OPTIMIZATION_PROPERTY, "true");
        TestInput leftInput = load(Side.LEFT, 1, SIZE, SIZE);
        TestInput rightInput = load(Side.RIGHT, N_POINTS, 1, 1);
        testStats.resetAll();
        for (int trial = 0; trial < TRIALS; trial++) {
            test(leftInput, rightInput, filter, SpatialJoin.Duplicates.INCLUDE);
        }
        double joinTimeMsec = (double) testStats.joinTimeNsec / (TRIALS * 1_000_000L);
        print("points: %s, maxZ: %s, box size: %s, join time: %s msec",
              N_POINTS, MAX_Z, SIZE, joinTimeMsec);
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

    private static final int NX = 1_000_000;
    private static final int NY = 1_000_000;

    private final Space space = Space.newSpace(appSpace(0, NX, 0, NY), NX, NY);
    private final SpatialJoinFilter filter = new SpatialJoinFilter()
    {
        @Override
        public boolean overlap(SpatialObject x, SpatialObject y)
        {
            testStats.filterCount++;
            assert false;
            boolean overlap = false; // ((Box) x).overlap(((Box) y));
            if (overlap) {
                testStats.overlapCount++;
            }
            return overlap;
        }
    };
}
