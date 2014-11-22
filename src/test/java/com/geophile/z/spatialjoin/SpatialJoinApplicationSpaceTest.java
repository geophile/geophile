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
import com.geophile.z.Space;
import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialJoin;
import com.geophile.z.SpatialObject;
import com.geophile.z.TestIndex;
import com.geophile.z.TestRecord;
import com.geophile.z.spatialobject.d2.Box;
import com.geophile.z.spatialobject.d2.Point;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

public class SpatialJoinApplicationSpaceTest extends SpatialJoinTestBase
{
    @Test
    public void test() throws IOException, InterruptedException
    {
        TestInput data = loadData(100);
        for (int trial = 0; trial < TRIALS; trial++) {
            TestInput query = loadQuery();
            testJoin(query, data, manyManyFilter, oneManyFilter, SpatialJoin.Duplicates.EXCLUDE);
        }
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
    protected boolean overlap(SpatialObject x, SpatialObject y)
    {
        Box b = (Box) x;
        Point p = (Point) y;
        return
            b.xLo() <= p.x() && p.x() <= b.xHi() &&
            b.yLo() <= p.y() && p.y() <= b.yHi();
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

    private TestInput loadData(int n) throws IOException, InterruptedException
    {
        // An n x n grid is superimposed on the space, and a random point is selected from each grid cell.
        int gridCellSize = APP_SPACE_WIDTH / n;
        Index<TestRecord> index = new TestIndex();
        SpatialIndex<TestRecord> spatialIndex = SpatialIndex
            .newSpatialIndex(SPACE, index, SpatialIndex.Options.SINGLE_CELL);
        TestInput input = new TestInput(spatialIndex, String.format("grid(%s x %s)", gridCellSize, gridCellSize));
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double x = APP_SPACE_LO + i * gridCellSize + random.nextInt(gridCellSize);
                double y = APP_SPACE_LO + j * gridCellSize + random.nextInt(gridCellSize);
                input.add(new Point(x, y));
            }
        }
        return input;
    }

    private TestInput loadQuery() throws IOException, InterruptedException
    {
        int xSize = random.nextInt(GRID_CELL_WIDTH * 3);
        int ySize = random.nextInt(GRID_CELL_WIDTH * 3);
        Index<TestRecord> index = newIndex(true);
        SpatialIndex<TestRecord> spatialIndex = SpatialIndex.newSpatialIndex(SPACE, index);
        TestInput input = new TestInput(spatialIndex, String.format("box(%s x %s)", xSize, ySize));
        double xLo = APP_SPACE_LO + random.nextInt(APP_SPACE_WIDTH - xSize);
        double xHi = xLo + xSize;
        double yLo = APP_SPACE_LO + random.nextInt(APP_SPACE_WIDTH - ySize);
        double yHi = yLo + ySize;
        input.add(new Box(xLo, xHi, yLo, yHi));
        return input;
    }

    private static final double APP_SPACE_LO = 1_000_000.0;
    private static final double APP_SPACE_HI = 2_000_000.0;
    private static final int APP_SPACE_WIDTH = (int) (APP_SPACE_HI - APP_SPACE_LO);
    private static final int GRID_RESOLUTION = 100;
    private static final int GRID_CELL_WIDTH = APP_SPACE_WIDTH / GRID_RESOLUTION; // 10,000
    private static final int X_BITS = 10;
    private static final int Y_BITS = 10;
    private static final int TRIALS = 100;
    private static final Space SPACE = Space.newSpace(new double[]{APP_SPACE_LO, APP_SPACE_LO},
                                                      new double[]{APP_SPACE_HI, APP_SPACE_HI},
                                                      new int[]{X_BITS, Y_BITS});
    private static final BoxPointOverlapTester OVERLAP_TESTER = new BoxPointOverlapTester();

    private final SpatialJoin.Filter<TestRecord, TestRecord> manyManyFilter =
        new SpatialJoin.Filter<TestRecord, TestRecord>()
        {
            @Override
            public boolean overlap(TestRecord left, TestRecord right)
            {
                testStats.filterCount++;
                boolean overlap = OVERLAP_TESTER.overlap(left.spatialObject(), right.spatialObject());
                if (overlap) {
                    testStats.overlapCount++;
                }
                return overlap;
            }
        };
    private final SpatialJoin.Filter<SpatialObject, TestRecord> oneManyFilter =
        new SpatialJoin.Filter<SpatialObject, TestRecord>()
        {
            @Override
            public boolean overlap(SpatialObject left, TestRecord right)
            {
                testStats.filterCount++;
                boolean overlap = OVERLAP_TESTER.overlap(left, right.spatialObject());
                if (overlap) {
                    testStats.overlapCount++;
                }
                return overlap;
            }
        };
    private final Random random = new Random(123454321);
}
