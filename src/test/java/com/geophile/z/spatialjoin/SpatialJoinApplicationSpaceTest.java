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

import com.geophile.z.ApplicationSpace;
import com.geophile.z.Space;
import com.geophile.z.SpatialJoin;
import com.geophile.z.SpatialObject;
import com.geophile.z.spatialobject.d2.Box;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.Level;

import static org.junit.Assert.assertEquals;

public class SpatialJoinApplicationSpaceTest extends SpatialJoinIteratorTestBase
{
    @Test
    public void test() throws IOException, InterruptedException
    {
        enableLogging();
        this.space = Space.newSpace(APPLICATION_SPACE, X_BITS, Y_BITS);
        TestInput data = loadData(100);
        for (int trial = 0; trial < TRIALS; trial++) {
            TestInput query = loadQuery();
            test(query, data, filter, SpatialJoin.Duplicates.EXCLUDE);
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
        long xLo = random.nextInt(APP_SPACE_WIDTH - maxXSize);
        long xHi = xLo + (maxXSize == 1 ? 0 : random.nextInt(maxXSize));
        long yLo = random.nextInt(APP_SPACE_WIDTH - maxYSize);
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
    protected boolean overlap(SpatialObject x, SpatialObject y)
    {
        Box a = (Box) x;
        Box b = (Box) y;
        return
            a.xLo() <= b.xHi() && b.xLo() <= a.xHi() &&
            a.yLo() <= b.yHi() && b.yLo() <= a.yHi();
    }

    @Override
    protected Level logLevel()
    {
        return Level.WARNING;
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
        TestInput input = new TestInput(space(), 1, 1, true);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double x = APP_SPACE_LO + i * gridCellSize + random.nextInt(gridCellSize);
                double y = APP_SPACE_LO + j * gridCellSize + random.nextInt(gridCellSize);
                input.add(new Box(x, x, y, y));
            }
        }
        return input;
    }

    private TestInput loadQuery() throws IOException, InterruptedException
    {
        int xSize = random.nextInt(GRID_CELL_WIDTH * 3);
        int ySize = random.nextInt(GRID_CELL_WIDTH * 3);
        TestInput input = new TestInput(space(), xSize, ySize, false);
        double xLo = APP_SPACE_LO + random.nextInt(APP_SPACE_WIDTH - xSize);
        double xHi = xLo + xSize;
        double yLo = APP_SPACE_LO + random.nextInt(APP_SPACE_WIDTH - ySize);
        double yHi = yLo + ySize;
        input.add(new Box(xLo, xHi, yLo, yHi));
        return input;
    }

    private static final double APP_SPACE_LO = 1_000_000.0;
    private static final double APP_SPACE_HI = 2_000_000.0;
    private static final ApplicationSpace APPLICATION_SPACE =
        appSpace(APP_SPACE_LO, APP_SPACE_HI, APP_SPACE_LO, APP_SPACE_HI);

    private static final int APP_SPACE_WIDTH = (int) (APP_SPACE_HI - APP_SPACE_LO);
    private static final int GRID_RESOLUTION = 100;
    private static final int GRID_CELL_WIDTH = APP_SPACE_WIDTH / GRID_RESOLUTION; // 10,000
    private static final int X_BITS = 10;
    private static final int Y_BITS = 10;
    private static final int TRIALS = 100;

    private Space space;
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
