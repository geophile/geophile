package com.geophile.z.spatialjoin;

import com.geophile.z.*;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

public abstract class SpatialJoinTestBase
{
    protected final void testJoin(SpatialJoin spatialJoin, TestInput leftInput, TestInput rightInput)
        throws IOException, InterruptedException
    {
        this.spatialJoin = spatialJoin;
        this.leftInput = leftInput;
        this.rightInput = rightInput;
        this.testStats = new TestStats();
        try {
            Map<Pair, Integer> actual = computeSpatialJoin();
            Set<Pair> expected = null;
            if (verify()) {
                if (spatialJoin.duplicates() == SpatialJoin.Duplicates.EXCLUDE) {
                    checkNoDuplicates(actual);
                }
                expected = computeExpectedSpatialJoin();
                checkEquals(expected, actual.keySet());
            }
            if (trace()) {
                if (expected != null) {
                    print("expected");
                    for (Pair pair : expected) {
                        print("    %s", pair);
                    }
                }
                print("actual");
                for (Map.Entry<Pair, Integer> entry : actual.entrySet()) {
                    print("    %s: %s", entry.getKey(), entry.getValue());
                }
            }
            if (printSummary() && expected != null) {
                double speedup = (double) testStats.slowJoinTimeNsec / testStats.joinTimeNsec;
                if (expected.size() == 0) {
                    print("%s\taccuracy = EMPTY RESULT\tspeedup = %s",
                          describeTest(), speedup);
                } else {
                    double accuracy = (double) testStats.overlapCount / testStats.filterCount;
                    print("%s\tcount = %s\taccuracy = %s\tspeedup = %s",
                          describeTest(), actual.size(), accuracy, speedup);
                }
            }
        } catch (AssertionError e) {
            print("Assertion error on %s", describeTest());
            throw e;
        }
    }

    protected final TestInput newTestInput(int n, SpatialObjectGenerator spatialObjectGenerator)
        throws IOException, InterruptedException
    {
        SpatialIndex spatialIndex = SpatialIndex.newSpatialIndex(space(), newIndex());
        TestInput testInput = new TestInput(spatialIndex, spatialObjectGenerator.description());
        load(n, spatialObjectGenerator, testInput);
        commit();
        return testInput;
    }

    protected abstract Space space();

    protected abstract Index newIndex();

    protected void commit()
    {}

    protected abstract boolean overlap(SpatialObject s, SpatialObject t);

    protected abstract boolean verify();

    protected abstract boolean printSummary();

    protected abstract boolean trace();

    protected void checkEquals(Object expected, Object actual)
    {
        assertEquals(expected, actual);
    }

    protected void load(int n, SpatialObjectGenerator spatialObjectGenerator, TestInput testInput)
    throws IOException, InterruptedException
    {
        for (int i = 0; i < n; i++) {
            testInput.add(spatialObjectGenerator.newSpatialObject());
        }
    }

    protected static ApplicationSpace appSpace(final double xLo, final double xHi, final double yLo, final double yHi)
    {
        return ApplicationSpace.newApplicationSpace(new double[]{xLo, yLo}, new double[]{xHi, yHi});
    }

    protected final void print(String template, Object... args)
    {
        System.out.println(String.format(template, args));
    }

    private void checkNoDuplicates(Map<Pair, Integer> actual)
    {
        for (Integer count : actual.values()) {
            assertEquals(1, count.intValue());
        }
    }

    private Map<Pair, Integer> computeSpatialJoin() throws IOException, InterruptedException
    {
        Map<Pair, Integer> actual = new HashMap<>(); // Pair -> occurrence count
        long start = System.nanoTime();
        Iterator<Pair> joinScan = spatialJoin.iterator(leftInput.spatialIndex(), rightInput.spatialIndex());
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
        return actual;
    }

    private Set<Pair> computeExpectedSpatialJoin()
    {
        Set<Pair> expected = new HashSet<>();
        long start = System.nanoTime();
        for (SpatialObject s : leftInput.spatialObjects()) {
            for (SpatialObject t : rightInput.spatialObjects()) {
                if (overlap(s, t)) {
                    expected.add(new Pair(s, t));
                }
            }
        }
        long stop = System.nanoTime();
        testStats.slowJoinTimeNsec += stop - start;
        return expected;
    }

    private String describeTest()
    {
        return String.format("duplicates = %s\tLEFT: %s\tRIGHT: %s\t", spatialJoin.duplicates(), leftInput, rightInput);
    }

    private SpatialJoin spatialJoin;
    private TestInput leftInput;
    private TestInput rightInput;
    protected TestStats testStats;
}
