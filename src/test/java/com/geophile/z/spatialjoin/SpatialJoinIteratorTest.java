/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialjoin;

import com.geophile.z.Pair;
import com.geophile.z.Space;
import com.geophile.z.SpatialIndex;
import com.geophile.z.spatialobject.d2.Box;
import org.junit.Test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SpatialJoinIteratorTest
{
    @Test
    public void test()
    {
        for (int nLeft : COUNTS) {
            int nRight = MAX_COUNT / nLeft;
            assertEquals(MAX_COUNT, nLeft * nRight);
            for (int maxLeftXSize : MAX_X_SIZES) {
                for (int maxRightXSize : MAX_X_SIZES) {
                    test(nLeft, maxLeftXSize, nRight, maxRightXSize);
                }
            }
        }
    }

    private void test(int nLeft, int maxLeftXSize, int nRight, int maxRightXSize)
    {
        TestInput leftInput = null;
        TestInput rightInput = null;
        for (int trial = 0; trial < TRIALS; trial++) {
            boolean trace = false; // nLeft == 1 && nRight == 1000 && maxLeftXSize == 1 && maxRightXSize == 100000;
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
            // if (!(trace && trial == 4)) continue;
            Set<Pair<Box, Box>> actual;
            Set<Pair<Box, Box>> expected;
            try {
                // Actual
                Iterator<Pair<Box, Box>> joinScan =
                    leftInput.spatialIndex().join(rightInput.spatialIndex(), SpatialIndex.Duplicates.INCLUDE);
                actual = new HashSet<>();
                while (joinScan.hasNext()) {
                    actual.add(joinScan.next());
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
                    print("expected");
                    for (Pair<Box, Box> pair : expected) {
                        print("    %s", pair);
                    }
                    print("actual");
                    for (Pair<Box, Box> pair : actual) {
                        print("    %s", pair);
                    }
                }
                assertTrue(actual.containsAll(expected));
            } catch (AssertionError e) {
                print("Assertion error on: nLeft: %s, nRight: %s, maxLeftXSize: %s, maxRightXSize: %s, trial: %s",
                      nLeft, nRight, maxLeftXSize, maxRightXSize, trial);
                throw e;
            }
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

    private boolean overlaps(Box a, Box b)
    {
        return
            a.xLo() <= b.xHi() && b.xLo() <= a.xHi() &&
            a.yLo() <= b.yHi() && b.yLo() <= a.yHi();
    }

    private TestInput loadBoxes(int n, int maxXSize)
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

    private void print(String template, Object... args)
    {
        System.out.println(String.format(template, args));
    }

    private void enableLogging(Level level)
    {
        Logger.getLogger("").setLevel(level);
    }

    private static final int NX = 1_000_000;
    private static final int NY = 1_000_000;
    private static final Space SPACE = Space.newSpace(new int[]{20, 20});
    private static final long SEED = 123456789L;
    private static final int MAX_COUNT = 1000; // 1_000_000;
    private static final int[] COUNTS = new int[]{1, 10, 100, 1_000 /* , 10_000, 100_000, 1_000_000 */};
    private static final int[] MAX_X_SIZES = new int[]{1, 10_000 /* 1% */, 100_000 /* 10% */};
    private static final double[] ASPECT_RATIOS = new double[]{1 / 8.0, 1 / 4.0, 1 / 2.0, 1.0, 2.0, 4.0, 8.0};
    private static final int TRIALS = 10;
    private static int testIdGenerator = 0;

    private final Random random = new Random(SEED);
    private int testId = testIdGenerator++;
}
