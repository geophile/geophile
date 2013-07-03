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
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SpatialJoinIteratorTest extends SpatialJoinIteratorTestBase
{
    @Test
    public void test() throws IOException, InterruptedException
    {
        // print(space.toString());
        appSpace = appSpace(0, NX, 0, NY);
        this.space = Space.newSpace(appSpace, X_BITS, Y_BITS);
        TestInput leftInput = null;
        TestInput rightInput = null;
        for (int nLeft : COUNTS) {
            int nRight = MAX_COUNT / nLeft;
            assertEquals(MAX_COUNT, nLeft * nRight);
            for (int maxLeftXSize : MAX_SIZES) {
                for (int maxLeftYSize : MAX_SIZES) {
                    for (int maxRightXSize : MAX_SIZES) {
                        for (int maxRightYSize : MAX_SIZES) {
/*
                            if (!(nLeft == 1 &&
                                  maxLeftXSize == 100000 &&
                                  maxLeftYSize == 1 &&
                                  nRight == 100000 &&
                                  maxRightXSize == 10000 &&
                                  maxRightYSize == 10000)) {
                                continue;
                            }
*/
                            for (int trial = 0; trial < TRIALS; trial++) {
                                if (trial == 0 || nLeft < nRight) {
                                    leftInput = load(Side.LEFT, nLeft, maxLeftXSize, maxLeftYSize);
                                }
                                if (trial == 0 || nRight <= nLeft) {
                                    rightInput = load(Side.RIGHT, nRight, maxRightXSize, maxRightYSize);
                                }
                                test(leftInput, rightInput, filter, SpatialJoin.Duplicates.INCLUDE);
                                test(leftInput, rightInput, filter, SpatialJoin.Duplicates.EXCLUDE);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected Space space()
    {
        return space;
    }

    @Override
    protected Box newLeftObject(int maxXSize, int maxYSize)
    {
        int nx = (int) (appSpace.hi(0) - appSpace.lo(0));
        long xLo = random.nextInt(nx - maxXSize);
        long xHi = xLo + (maxXSize == 1 ? 0 : random.nextInt(maxXSize));
        int ny = (int) (appSpace.hi(1) - appSpace.lo(1));
        long yLo = random.nextInt(ny - maxYSize);
        long yHi = yLo + (maxYSize == 1 ? 0 : random.nextInt(maxYSize));
        return new Box(xLo, xHi, yLo, yHi);
    }

    @Override
    protected void checkEquals(Object expected, Object actual)
    {
        assertEquals(expected, actual);
    }

    @Override
    protected boolean verify()
    {
        return true;
    }

    @Override
    protected boolean printSummary()
    {
        return false;
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

    private static final int MAX_COUNT = 100_000; // 1_000_000;
    private static final int[] COUNTS = new int[]{1, 10, 100, 1_000, 10_000, 100_000 /*, 1_000_000 */};
    private static final int[] MAX_SIZES = new int[]{1, 10_000, /* 1% */ 100_000 /* 10% */};
    private static final int NX = 1_000_000;
    private static final int NY = 1_000_000;
    private static final int X_BITS = 20;
    private static final int Y_BITS = 20;
    private static final int TRIALS = 1; // 50;
    private Space space;
    private ApplicationSpace appSpace;
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
}
