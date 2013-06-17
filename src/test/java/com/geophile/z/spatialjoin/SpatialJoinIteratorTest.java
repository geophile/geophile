/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialjoin;

import com.geophile.z.ApplicationSpace;
import com.geophile.z.Space;
import com.geophile.z.SpatialJoin;
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
        this.space = Space.newSpace(appSpace(0, NX, 0, NY), X_BITS, Y_BITS);
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
                                    leftInput = loadBoxes(nLeft, maxLeftXSize, maxLeftYSize);
                                }
                                if (trial == 0 || nRight <= nLeft) {
                                    rightInput = loadBoxes(nRight, maxRightXSize, maxRightYSize);
                                }
                                test(leftInput, rightInput, SpatialJoin.Duplicates.INCLUDE);
                                test(leftInput, rightInput, SpatialJoin.Duplicates.EXCLUDE);
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
    protected Box testBox(int maxXSize, int maxYSize)
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

    private ApplicationSpace appSpace(final double xLo, final double xHi, final double yLo, final double yHi)
    {
        appSpace = new ApplicationSpace()
        {
            @Override
            public int dimensions()
            {
                return 2;
            }

            @Override
            public double lo(int d)
            {
                switch (d) {
                    case 0: return xLo;
                    case 1: return yLo;
                }
                assert false;
                return Double.NaN;
            }

            @Override
            public double hi(int d)
            {
                switch (d) {
                    case 0: return xHi;
                    case 1: return yHi;
                }
                assert false;
                return Double.NaN;
            }
        };
        return appSpace;
    }

    private static final int MAX_COUNT = 100_000; // 1_000_000;
    private static final int[] COUNTS = new int[]{1, 10, 100, 1_000, 10_000, 100_000 /*, 1_000_000 */};
    private static final int[] MAX_SIZES = new int[]{1, 10_000, /* 1% */ 100_000 /* 10% */};
    private static final int NX = 1_000_000;
    private static final int NY = 1_000_000;
    private static final int X_BITS = 20;
    private static final int Y_BITS = 20;
    private Space space;
    private ApplicationSpace appSpace;
}
