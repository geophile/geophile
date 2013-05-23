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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

public abstract class SpatialJoinIteratorTestBase
{
    protected abstract void checkEquals(Object expected, Object actual);

    protected abstract boolean verify();

    protected void test(int nLeft, int maxLeftXSize, int nRight, int maxRightXSize, int trials)
    {
        TestInput leftInput = null;
        TestInput rightInput = null;
        for (int trial = 0; trial < trials; trial++) {
            boolean trace = false; // nLeft == 1 && nRight == 1_000_000 && maxLeftXSize == 1 && maxRightXSize == 10000;
            if (trace) {
                enableLogging(Level.FINE);
            }
            // For each trial with a given set of parameters, only generate one new input sequence, for the smaller
            // data set, (or the right data set if the sizes match).
            if (trial == 0 || nLeft < nRight) {
                leftInput = loadBoxes(nLeft, maxLeftXSize);
            }
            if (trial == 0 || nRight <= nLeft) {
                rightInput = loadBoxes(nRight, maxRightXSize);
            }
            // if (!(trace && trial == 2)) continue;
            Map<Pair<Box, Box>, Integer> actual = null;
            Set<Pair<Box, Box>> expected = null;
            for (boolean duplicates : DUPLICATES) {
                try {
                    Iterator<Pair<Box, Box>> joinScan =
                        SpatialJoin.newSpatialJoin(FILTER,
                                                   duplicates
                                                   ? SpatialJoinImpl.Duplicates.INCLUDE
                                                   : SpatialJoinImpl.Duplicates.EXCLUDE)
                            .iterator(leftInput.spatialIndex(), rightInput.spatialIndex());
                    if (verify()) {
                        // Actual
                        actual = new HashMap<>();
                        while (joinScan.hasNext()) {
                            Pair<Box, Box> pair = joinScan.next();
                            Integer count = actual.get(pair);
                            if (count == null) {
                                count = 0;
                            }
                            actual.put(pair, count + 1);
                        }
                        // Expected
                        expected = new HashSet<>();
                        for (Box a : leftInput.boxes()) {
                            for (Box b : rightInput.boxes()) {
                                if (overlaps(a, b)) {
                                    expected.add(new Pair<>(a, b));
                                }
                            }
                        }
                        if (trace) {
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
                        if (!duplicates) {
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
                    print("Assertion error on: nLeft: %s, nRight: %s, maxLeftXSize: %s, maxRightXSize: %s, trial: %s",
                          nLeft, nRight, maxLeftXSize, maxRightXSize, trial);
                    throw e;
                }
                if (PRINT_SUMMARY) {
                    if (expected.size() == 0) {
                        print("nLeft: %s, nRight: %s, maxLeftXSize: %s, maxRightXSize: %s, trial: %s, accuracy: EMPTY RESULT",
                              nLeft, nRight, maxLeftXSize, maxRightXSize, trial);
                    } else {
                        double accuracy = (double) expected.size() / actual.size();
                        print("nLeft: %s, nRight: %s, maxLeftXSize: %s, maxRightXSize: %s, trial: %s, accuracy: %s",
                              nLeft, nRight, maxLeftXSize, maxRightXSize, trial, accuracy);
                    }
                }
            }
        }
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

    protected TestInput loadBoxes(int n, int maxXSize)
    {
        TestInput input = new TestInput(SPACE);
        for (int i = 0; i < n; i++) {
            input.addBox(randomBox(maxXSize));
        }
        return input;
    }

    private Box randomBox(int maxXSize)
    {
        double aspectRatio = ASPECT_RATIOS[random.nextInt(ASPECT_RATIOS.length)];
        int maxYSize = (int) (maxXSize * aspectRatio);
        if (maxYSize == 0) {
            maxYSize = 1;
        }
        if (maxYSize > NY) {
            maxYSize = NY - 1;
        }
        long xLo = random.nextInt(NX - maxXSize);
        long xHi = xLo + (maxXSize == 1 ? 0 : random.nextInt(maxXSize));
        long yLo = random.nextInt(NY - maxYSize);
        long yHi = yLo + (maxYSize == 1 ? 0 : random.nextInt(maxYSize));
        return new Box(xLo, xHi, yLo, yHi);
    }

    private void enableLogging(Level level)
    {
        Logger.getLogger("").setLevel(level);
    }

    protected static final int MAX_COUNT = 100_000; // 1_000_000;
    protected static final int[] COUNTS = new int[]{1, 10, 100, 1_000 , 10_000, 100_000 /*, 1_000_000 */};
    protected static final int[] MAX_X_SIZES = new int[]{1, 10_000, /* 1% */ 100_000 /* 10% */};
    protected static final int TRIALS = 1; // 50;
    private static final boolean PRINT_SUMMARY = false;
    private static final int NX = 1_000_000;
    private static final int NY = 1_000_000;
    private static final Space SPACE = Space.newSpace(NX, NY);
    private static final long SEED = 123456789L;
    private static final double[] ASPECT_RATIOS = new double[]{1 / 8.0, 1 / 4.0, 1 / 2.0, 1.0, 2.0, 4.0, 8.0};
    private static int testIdGenerator = 0;
    private static final boolean[] DUPLICATES = { true, false };
    private static final SpatialJoinFilter<Box, Box> FILTER = new SpatialJoinFilter<Box, Box>()
    {
        @Override
        public boolean overlap(Box x, Box y)
        {
            return x.overlap(y);
        }
    };

    private final Random random = new Random(SEED);
    private int testId = testIdGenerator++;
}
