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

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialjoin;

import com.geophile.z.Index;
import com.geophile.z.Pair;
import com.geophile.z.Space;
import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialJoin;
import com.geophile.z.SpatialObject;
import com.geophile.z.TestIndex;
import com.geophile.z.TestRecord;
import com.geophile.z.index.sortedarray.SortedArray;
import com.geophile.z.spatialobject.d2.Box;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SelfJoinTest extends SpatialJoinTestBase
{
    @Test
    public void selfJoin() throws IOException, InterruptedException
    {
        SpatialJoin.Filter<TestRecord, TestRecord> filter =
            new SpatialJoin.Filter<TestRecord, TestRecord>()
            {
                @Override
                public boolean overlap(TestRecord r, TestRecord s)
                {
                    testStats.filterCount++;
                    boolean overlap = OVERLAP_TESTER.overlap(r.spatialObject(), s.spatialObject());
                    if (overlap) {
                        testStats.overlapCount++;
                    }
                    return overlap;
                }
            };
        for (int maxXSize : MAX_SIZES) {
            for (int maxYSize : MAX_SIZES) {
                BoxGenerator boxGenerator = new BoxGenerator(SPACE, random, maxXSize, maxYSize);
                TestInput input = newTestInput(COUNT, boxGenerator);
                testJoin(input, input, filter, null, SpatialJoin.Duplicates.EXCLUDE);
            }
        }
    }

    // Issue #2
    @Test
    public void checkEqualSpatialObjects() throws IOException, InterruptedException
    {
        Index<TestRecord> index =
            new SortedArray<TestRecord>()
            {
                @Override
                public TestRecord newRecord()
                {
                    return new TestRecord();
                }
            };
        SpatialIndex<TestRecord> spatialIndex = SpatialIndex.newSpatialIndex(SPACE, index);
        Box box = new Box(10, 20, 30, 40);
        TestRecord.Factory recordFactory = new TestRecord.Factory();
        spatialIndex.add(box, recordFactory.setup(box, 1));
        box = new Box(10, 20, 30, 40);
        spatialIndex.add(box, recordFactory.setup(box, 2));
        Iterator<Pair<TestRecord, TestRecord>> iterator =
            SpatialJoin.newSpatialJoin(SpatialJoin.Duplicates.EXCLUDE, KEEP_ALL).iterator(spatialIndex, spatialIndex);
        // Should see all combinations of b1 and b2
        int count = 0;
        int mask = 0;
        while (iterator.hasNext()) {
            Pair pair = iterator.next();
            TestRecord left = (TestRecord) pair.left();
            TestRecord right = (TestRecord) pair.right();
            if (left.soid() == 1 && right.soid() == 1) {
                mask |= 0x1;
            } else if (left.soid() == 1 && right.soid() == 2) {
                mask |= 0x2;
            } else if (left.soid() == 2 && right.soid() == 1) {
                mask |= 0x4;
            } else if (left.soid() == 2 && right.soid() == 2) {
                mask |= 0x8;
            } else {
                fail();
            }
            count++;
        }
        assertEquals(4, count);
        assertEquals(0xf, mask);
    }

    @Override
    protected Space space()
    {
        return SPACE;
    }

    @Override
    protected Index<TestRecord> newIndex(boolean stableRecords)
    {
        return new TestIndex();
    }

    @Override
    protected boolean overlap(SpatialObject s, SpatialObject t)
    {
        return OVERLAP_TESTER.overlap(s, t);
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
    protected boolean trace()
    {
        return false;
    }

    private TestInput newTestInput(int n, BoxGenerator boxGenerator) throws IOException, InterruptedException
    {
        Index<TestRecord> index = newIndex(true);
        SpatialIndex<TestRecord> spatialIndex = SpatialIndex.newSpatialIndex(SPACE, index);
        TestInput testInput = new TestInput(spatialIndex, boxGenerator.description());
        load(n, boxGenerator, testInput);
        return testInput;
    }

    private static final int NX = 1_000_000;
    private static final int NY = 1_000_000;
    private static final int X_BITS = 20;
    private static final int Y_BITS = 20;
    private static final Space SPACE = Space.newSpace(new double[]{0, 0},
                                                      new double[]{NX, NY},
                                                      new int[]{X_BITS, Y_BITS});
    private static final int COUNT = 10_000;
    private static final int[] MAX_SIZES = new int[]{1, 10_000, /* 1% */ 100_000 /* 10% */};
    private static final BoxOverlapTester OVERLAP_TESTER = new BoxOverlapTester();
    private static final SpatialJoin.Filter KEEP_ALL =
        new SpatialJoin.Filter()
        {
            @Override
            public boolean overlap(Object left, Object right)
            {
                return true;
            }
        };
    private final Random random = new Random(654321);
}
