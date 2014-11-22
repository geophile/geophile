/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialjoin;

import com.geophile.z.Index;
import com.geophile.z.Pair;
import com.geophile.z.Record;
import com.geophile.z.Space;
import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialJoin;
import com.geophile.z.SpatialObject;
import com.geophile.z.TestRecord;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public abstract class SpatialJoinTestBase
{
    protected final void testJoin(TestInput leftInput,
                                  TestInput rightInput,
                                  SpatialJoin.Filter<TestRecord, TestRecord> manyManyFilter,
                                  SpatialJoin.Filter<SpatialObject, TestRecord> oneManyFilter,
                                  SpatialJoin.Duplicates duplicates)
        throws IOException, InterruptedException
    {
        testManyManyJoin(leftInput, rightInput, manyManyFilter, duplicates);
        if (leftInput.records().size() == 1) {
            testOneManyJoin(leftInput.only().spatialObject(), rightInput, oneManyFilter, duplicates);
        } else if (rightInput.records().size() == 1) {
            testOneManyJoin(rightInput.only().spatialObject(), leftInput, oneManyFilter, duplicates);
        }
    }

    protected final void testOneManyJoin(SpatialObject query,
                                         TestInput data,
                                         SpatialJoin.Filter<SpatialObject, TestRecord> filter,
                                         SpatialJoin.Duplicates duplicates)
        throws IOException, InterruptedException
    {
        // The query object is the left join argument, data is the right join argument.
        this.query = query;
        this.rightInput = data;
        this.oneManyFilter = filter;
        this.duplicates = duplicates;
        this.testStats = new TestStats();
        try {
            Map<Record, Integer> actual = computeOneManySpatialJoin();
            Set<Record> expected = null;
            if (verify()) {
                if (duplicates == SpatialJoin.Duplicates.EXCLUDE) {
                    checkNoDuplicates(actual);
                }
                expected = computeExpectedOneManySpatialJoin();
                checkEquals(expected, actual.keySet());
            }
            if (trace()) {
                if (expected != null) {
                    print("expected");
                    for (Record record : expected) {
                        print("    %s", record);
                    }
                }
                print("actual");
                for (Map.Entry<Record, Integer> entry : actual.entrySet()) {
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

    protected final void testManyManyJoin(TestInput leftInput,
                                          TestInput rightInput,
                                          SpatialJoin.Filter<TestRecord, TestRecord> filter,
                                          SpatialJoin.Duplicates duplicates)

        throws IOException,InterruptedException

    {
        this.leftInput = leftInput;
        this.rightInput = rightInput;
        this.manyManyFilter = filter;
        this.duplicates = duplicates;
        this.testStats = new TestStats();
        try {
            Map<Pair<TestRecord, TestRecord>, Integer> actual = computeManyManySpatialJoin();
            Set<Pair<TestRecord, TestRecord>> expected = null;
            if (verify()) {
                if (duplicates == SpatialJoin.Duplicates.EXCLUDE) {
                    checkNoDuplicates(actual);
                }
                expected = computeExpectedManyManySpatialJoin();
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
                for (Map.Entry<Pair<TestRecord, TestRecord>, Integer> entry : actual.entrySet()) {
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
        SpatialIndex<TestRecord> spatialIndex = SpatialIndex.newSpatialIndex(space(), newIndex(true));
        TestInput testInput = new TestInput(spatialIndex, spatialObjectGenerator.description());
        load(n, spatialObjectGenerator, testInput);
        commit();
        return testInput;
    }

    protected abstract Space space();

    protected abstract Index<TestRecord> newIndex(boolean stableRecords);

    protected void commit()
    {
    }

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

    protected final void print(String template, Object... args)
    {
        System.out.println(String.format(template, args));
    }

    private void checkNoDuplicates(Map<?, Integer> actual)
    {
        for (Integer count : actual.values()) {
            assertEquals(1, count.intValue());
        }
    }

    private Map<Record, Integer> computeOneManySpatialJoin() throws IOException, InterruptedException
    {
        Map<Record, Integer> actual = new HashMap<>(); // Record -> occurrence count
        long start = System.nanoTime();
        Iterator<TestRecord> joinScan =
            SpatialJoin
                .newSpatialJoin(duplicates, oneManyFilter)
                .iterator(query, rightInput.spatialIndex());
        while (joinScan.hasNext()) {
            TestRecord record = joinScan.next();
            if (verify()) {
                Integer count = actual.get(record);
                if (count == null) {
                    count = 0;
                }
                actual.put(record, count + 1);
            }
            testStats.outputRowCount++;
        }
        long stop = System.nanoTime();
        testStats.joinTimeNsec += stop - start;
        return actual;
    }

    private Map<Pair<TestRecord, TestRecord>, Integer> computeManyManySpatialJoin()
        throws IOException, InterruptedException
    {
        Map<Pair<TestRecord, TestRecord>, Integer> actual = new HashMap<>(); // Pair -> occurrence count
        long start = System.nanoTime();
        Iterator<Pair<TestRecord, TestRecord>> joinScan =
            SpatialJoin
                .newSpatialJoin(duplicates, manyManyFilter)
                .iterator(leftInput.spatialIndex(), rightInput.spatialIndex());
        while (joinScan.hasNext()) {
            Pair<TestRecord, TestRecord> pair = joinScan.next();
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

    private Set<Record> computeExpectedOneManySpatialJoin()
    {
        Set<Record> expected = new HashSet<>();
        long start = System.nanoTime();
        for (TestRecord s : rightInput.records()) {
            if (overlap(query, s.spatialObject())) {
                expected.add(s);
            }
        }
        long stop = System.nanoTime();
        testStats.slowJoinTimeNsec += stop - start;
        return expected;
    }

    private Set<Pair<TestRecord, TestRecord>> computeExpectedManyManySpatialJoin()
    {
        Set<Pair<TestRecord, TestRecord>> expected = new HashSet<>();
        long start = System.nanoTime();
        for (TestRecord r : leftInput.records()) {
            for (TestRecord s : rightInput.records()) {
                if (overlap(r.spatialObject(), s.spatialObject())) {
                    expected.add(new TestPair<>(r, s));
                }
            }
        }
        long stop = System.nanoTime();
        testStats.slowJoinTimeNsec += stop - start;
        return expected;
    }

    private String describeTest()
    {
        return String.format("duplicates = %s\tLEFT: %s\tRIGHT: %s\t", duplicates, leftInput, rightInput);
    }

    private TestInput leftInput;
    private TestInput rightInput;
    private SpatialJoin.Filter<TestRecord, TestRecord> manyManyFilter;
    private SpatialJoin.Filter<SpatialObject, TestRecord> oneManyFilter;
    private SpatialJoin.Duplicates duplicates;
    private SpatialObject query;
    protected TestStats testStats;

    // Inner classes

    private static class TestPair<LEFT_RECORD extends Record, RIGHT_RECORD extends Record>
        extends Pair<LEFT_RECORD, RIGHT_RECORD>
    {
        TestPair(LEFT_RECORD left, RIGHT_RECORD right)
        {
            super(left, right);
        }
    }
}
