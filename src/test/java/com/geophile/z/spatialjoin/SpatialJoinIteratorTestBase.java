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

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

public abstract class SpatialJoinIteratorTestBase
{
    protected final void test(TestInput leftInput,
                              TestInput rightInput,
                              SpatialJoinFilter filter,
                              SpatialJoin.Duplicates duplicates)
        throws IOException, InterruptedException
    {
        this.leftInput = leftInput;
        this.rightInput = rightInput;
        this.duplicates = duplicates;
        Map<Pair, Integer> actual;
        Set<Pair> expected = null;
        try {
            long start = System.nanoTime();
            Iterator<Pair> joinScan =
                SpatialJoin.newSpatialJoin(filter, duplicates)
                           .iterator(leftInput.spatialIndex(), rightInput.spatialIndex());
            // Actual
            actual = new HashMap<>();
            while (joinScan.hasNext()) {
                Pair pair = joinScan.next();
                if (verify()) {
                    Integer count = actual.get(pair);
                    if (count == null) {
                        count = 0;
                    }
                    actual.put(pair, count + 1);
                }
                testStats.outputRowCount++;
            }
            long stop = System.nanoTime();
            testStats.joinTimeNsec += stop - start;
            if (verify()) {
                // Expected
                expected = new HashSet<>();
                for (SpatialObject a : leftInput.spatialObjects()) {
                    for (SpatialObject b : rightInput.spatialObjects()) {
                        if (overlap(a, b)) {
                            expected.add(new Pair(a, b));
                        }
                    }
                }
                if (trace()) {
                    print("expected");
                    for (Pair pair : expected) {
                        print("    %s", pair);
                    }
                    print("actual");
                    for (Map.Entry<Pair, Integer> entry : actual.entrySet()) {
                        print("    %s: %s", entry.getKey(), entry.getValue());
                    }
                }
                checkEquals(expected, actual.keySet());
                if (duplicates == SpatialJoin.Duplicates.EXCLUDE) {
                    for (Integer count : actual.values()) {
                        assertEquals(1, count.intValue());
                    }
                }
            } else {
                while (joinScan.hasNext()) {
                    joinScan.next();
                }
            }
        } catch (AssertionError e) {
            print("Assertion error on %s", describeTest());
            throw e;
        }
        if (printSummary() && expected != null) {
            if (expected.size() == 0) {
                print("%s\taccuracy = EMPTY RESULT", describeTest());
            } else {
                double accuracy = (double) testStats.overlapCount / testStats.filterCount;
                print("%s\taccuracy = %s", describeTest(), accuracy);
            }
        }
    }

    protected abstract void checkEquals(Object expected, Object actual);

    protected abstract boolean overlap(SpatialObject x, SpatialObject y);

    protected abstract boolean verify();

    private String describeTest()
    {
        return String.format("duplicates = %s\t" +
                             "LEFT: n = %s, max sizes = (%s, %s)\t" +
                             "RIGHT: n = %s, max sizes = (%s, %s)\t",
                             duplicates,
                             leftInput.spatialObjects().size(), leftInput.maxXSize(), leftInput.maxYSize(),
                             rightInput.spatialObjects().size(), rightInput.maxXSize(), rightInput.maxYSize());
    }

    protected void print(String template, Object... args)
    {
        System.out.println(String.format(template, args));
    }

    protected final TestInput load(Side side, int n, int maxXSize, int maxYSize) throws IOException, InterruptedException
    {
        long start = System.currentTimeMillis();
        boolean singleCell = maxXSize == 1 && maxYSize == 1;
        TestInput input = new TestInput(space(), maxXSize, maxYSize, singleCell);
        for (int i = 0; i < n; i++) {
            input.add(
                side == Side.LEFT
                ? newLeftObject(maxXSize, maxYSize)
                : newRightObject(maxXSize, maxYSize));
        }
        long stop = System.currentTimeMillis();
        testStats.loadTimeMsec += stop - start;
        return input;
    }

    protected static ApplicationSpace appSpace(final double xLo, final double xHi, final double yLo, final double yHi)
    {
        return new ApplicationSpace()
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
    }

    protected abstract Space space();

    protected abstract SpatialObject newLeftObject(int maxXSize, int maxYSize);

    protected SpatialObject newRightObject(int maxXSize, int maxYSize)
    {
        return newLeftObject(maxXSize, maxYSize);
    }

    protected Level logLevel()
    {
        return Level.WARNING;
    }

    protected boolean trace()
    {
        return false;
    }

    protected void enableLogging()
    {
        Logger.getLogger("").setLevel(logLevel());
    }

    protected boolean printSummary()
    {
        return false;
    }

    protected void resetRandom()
    {
        random = new Random(SEED);
    }

    private static final long SEED = 123456789L;

    protected Random random = new Random(SEED);
    private TestInput leftInput;
    private TestInput rightInput;
    private SpatialJoin.Duplicates duplicates;
    protected TestStats testStats = new TestStats();

    protected enum Side { LEFT, RIGHT }
}
