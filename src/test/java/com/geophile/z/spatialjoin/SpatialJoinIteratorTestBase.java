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

import com.geophile.z.Pair;
import com.geophile.z.Space;
import com.geophile.z.SpatialJoin;
import com.geophile.z.spatialobject.d2.Box;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

public abstract class SpatialJoinIteratorTestBase
{
    protected final void test(TestInput leftInput, TestInput rightInput, SpatialJoin.Duplicates duplicates)
        throws IOException, InterruptedException
    {
        this.leftInput = leftInput;
        this.rightInput = rightInput;
        this.duplicates = duplicates;
        Map<Pair<Box, Box>, Integer> actual = null;
        Set<Pair<Box, Box>> expected = null;
        try {
            long start = System.nanoTime();
            Iterator<Pair<Box, Box>> joinScan =
                SpatialJoin.newSpatialJoin(FILTER, duplicates)
                           .iterator(leftInput.spatialIndex(), rightInput.spatialIndex());
            // Actual
            actual = new HashMap<>();
            while (joinScan.hasNext()) {
                Pair<Box, Box> pair = joinScan.next();
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
                for (Box a : leftInput.boxes()) {
                    for (Box b : rightInput.boxes()) {
                        if (overlaps(a, b)) {
                            expected.add(new Pair<>(a, b));
                        }
                    }
                }
                if (trace()) {
                    assert expected != null;
                    print("expected");
                    for (Pair<Box, Box> pair : expected) {
                        print("    %s", pair);
                    }
                    print("actual");
                    for (Map.Entry<Pair<Box, Box>, Integer> entry : actual.entrySet()) {
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

    protected abstract boolean verify();

    private String describeTest()
    {
        return String.format("duplicates = %s\t" +
                             "LEFT: n = %s, max sizes = (%s, %s)\t" +
                             "RIGHT: n = %s, max sizes = (%s, %s)\t",
                             duplicates,
                             leftInput.boxes().size(), leftInput.maxXSize(), leftInput.maxYSize(),
                             rightInput.boxes().size(), rightInput.maxXSize(), rightInput.maxYSize());
    }

    protected void print(String template, Object... args)
    {
        System.out.println(String.format(template, args));
    }

    private boolean overlaps(Box a, Box b)
    {
        return
            a.xLo() <= b.xHi() && b.xLo() <= a.xHi() &&
            a.yLo() <= b.yHi() && b.yLo() <= a.yHi();
    }

    protected TestInput loadBoxes(int n, int maxXSize, int maxYSize) throws IOException, InterruptedException
    {
        long start = System.currentTimeMillis();
        boolean singleCell = maxXSize == 1 && maxYSize == 1;
        TestInput input = new TestInput(space(), maxXSize, maxYSize, singleCell);
        for (int i = 0; i < n; i++) {
            input.addBox(testBox(maxXSize, maxYSize));
        }
        long stop = System.currentTimeMillis();
        testStats.loadTimeMsec += stop - start;
        return input;
    }

    protected abstract Space space();

    protected abstract Box testBox(int maxXSize, int maxYSize);

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

    protected static final int TRIALS = 1; // 50;
    private static final long SEED = 123456789L;

    private final TestFilter FILTER = new TestFilter();
    protected Random random = new Random(SEED);
    private TestInput leftInput;
    private TestInput rightInput;
    private SpatialJoin.Duplicates duplicates;
    protected TestStats testStats = new TestStats();

    private final class TestFilter implements SpatialJoinFilter<Box, Box>
    {
        @Override
        public boolean overlap(Box x, Box y)
        {
            testStats.filterCount++;
            boolean overlap = x.overlap(y);
            if (overlap) {
                testStats.overlapCount++;
            }
            return overlap;
        }
    }
}
