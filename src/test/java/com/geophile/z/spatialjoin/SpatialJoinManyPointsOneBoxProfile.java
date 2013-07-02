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

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialjoin;

import com.geophile.z.*;
import com.geophile.z.index.treeindex.TreeIndex;
import com.geophile.z.spatialobject.d2.Box;
import com.geophile.z.spatialobject.d2.Point;
import com.geophile.z.spatialobject.jts.JTSPoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

public class SpatialJoinManyPointsOneBoxProfile extends SpatialJoinIteratorTestBase
{
    private static final boolean USE_JTS = Boolean.getBoolean("jts");

    public static void main(String[] args) throws IOException, InterruptedException
    {
        new SpatialJoinManyPointsOneBoxProfile().run();
    }

    private void run() throws IOException, InterruptedException
    {
        random = new Random(12345);
        final int TRIALS = 100_000;
        final int N_POINTS = 1_000_000;
        final int QUERY_X_SIZE = NX / 100;
        final int QUERY_Y_SIZE = NY / 100;
        SpatialIndex rightInput = loadPoints(N_POINTS);
        long totalOutputCount = 0;
        long totalMsec = 0;
        for (int trial = 0; trial < TRIALS; trial++) {
            SpatialIndex leftInput = loadOneBox(QUERY_X_SIZE, QUERY_Y_SIZE);
            long start = System.currentTimeMillis();
            Iterator<Pair> joinScan = SpatialJoin.newSpatialJoin(FILTER, SpatialJoin.Duplicates.INCLUDE)
                                                 .iterator(leftInput, rightInput);
            while (joinScan.hasNext()) {
                joinScan.next();
                totalOutputCount++;
            }
            long stop = System.currentTimeMillis();
            totalMsec += stop - start;
        }
        print("average output size: %s", (double) totalOutputCount / TRIALS);
        print("average join msec: %s", (double) totalMsec / TRIALS);
    }

    @Override
    protected Space space()
    {
        return SPACE;
    }

    @Override
    protected Box newLeftObject(int xSize, int ySize)
    {
        long xLo = random.nextInt(NX - xSize + 1);
        long xHi = xLo + xSize - 1;
        long yLo = random.nextInt(NY - ySize + 1);
        long yHi = yLo + ySize - 1;
        return new Box(xLo, xHi, yLo, yHi);
    }

    @Override
    protected void checkEquals(Object expected, Object actual)
    {
        assert expected.equals(actual);
    }

    @Override
    protected boolean verify()
    {
        return false;
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
    protected boolean printSummary()
    {
        return false;
    }

    protected SpatialIndex loadPoints(int n) throws IOException, InterruptedException
    {
        SpatialIndex index = SpatialIndex.newSpatialIndex(space(), new TreeIndex(), SpatialIndex.Options.SINGLE_CELL);
        for (int i = 0; i < n; i++) {
            index.add(testPoint());
        }
        return index;
    }

    protected SpatialIndex loadOneBox(int maxXSize, int maxYSize) throws IOException, InterruptedException
    {
        SpatialIndex index = SpatialIndex.newSpatialIndex(space(), new TreeIndex(), SpatialIndex.Options.DEFAULT);
        index.add(newLeftObject(maxXSize, maxYSize));
        return index;
    }

    private SpatialObject testPoint()
    {
        int x = random.nextInt(NX);
        int y = random.nextInt(NY);
        return
            USE_JTS
            ? new JTSPoint(SPACE, factory.createPoint(new Coordinate(x, y)))
            : new Point(x, y);
    }

    private static final int NX = 1_000_000;
    private static final int NY = 1_000_000;
    private static final int LOG_NX = 20;
    private static final int LOG_NY = 20;
    private static final Space SPACE = Space.newSpace(appSpace(0, NX, 0, NY), LOG_NX, LOG_NY);
    private static final SpatialJoinFilter FILTER =
        USE_JTS
        ? new TestFilterJTS()
        : new TestFilter();

    private Random random;
    private final GeometryFactory factory = new GeometryFactory();

    private static final class TestFilter implements SpatialJoinFilter
    {
        @Override
        public boolean overlap(SpatialObject a, SpatialObject b)
        {
            Box box = (Box) a;
            Point point = (Point) b;
            double px = point.x();
            double py = point.y();
            return
                box.xLo() <= px && px <= box.xHi() &&
                box.yLo() <= py && py <= box.yHi();
        }
    }

    private static final class TestFilterJTS implements SpatialJoinFilter
    {
        @Override
        public boolean overlap(SpatialObject a, SpatialObject b)
        {
            Box box = (Box) a;
            JTSPoint point = (JTSPoint) b;
            Coordinate coordinate = point.point().getCoordinate();
            double px = coordinate.x;
            double py = coordinate.y;
            return
                box.xLo() <= px && px <= box.xHi() &&
                box.yLo() <= py && py <= box.yHi();
        }
    }
}
