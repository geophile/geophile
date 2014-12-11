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
import com.geophile.z.spatialobject.jts.JTS;
import com.geophile.z.spatialobject.jts.JTSPoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

public class SpatialJoinManyPointsOneBoxProfile extends SpatialJoinTestBase
{
    private static final boolean USE_JTS = Boolean.getBoolean("jts");

    public static void main(String[] args) throws IOException, InterruptedException
    {
        new SpatialJoinManyPointsOneBoxProfile().run();
    }

    private void run() throws IOException, InterruptedException
    {
        final int TRIALS = 100_000_000;
        final int N_POINTS = 1_000_000;
        final int QUERY_X_SIZE = NX / 100;
        final int QUERY_Y_SIZE = NY / 100;
        SpatialIndex<TestRecord> rightInput = loadPoints(N_POINTS);
        BoxGenerator boxGenerator = new BoxGenerator(SPACE, random, QUERY_X_SIZE, QUERY_Y_SIZE);
        long totalOutputCount = 0;
        long totalMsec = 0;
        for (int trial = 0; trial < TRIALS; trial++) {
            long start = System.currentTimeMillis();
            Iterator<TestRecord> joinScan =
                SpatialJoin.newSpatialJoin(SpatialJoin.Duplicates.INCLUDE, FILTER)
                           .iterator(boxGenerator.newSpatialObject(), rightInput);
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
    protected Index<TestRecord> newIndex(boolean stableRecords)
    {
        return new TestIndex();
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
    protected boolean verify()
    {
        return false;
    }

    @Override
    protected boolean trace()
    {
        return false;
    }

    @Override
    protected boolean printSummary()
    {
        return false;
    }

    @Override
    protected void checkEquals(Object expected, Object actual)
    {
        assert expected.equals(actual);
    }

    protected SpatialIndex<TestRecord> loadPoints(int n) throws IOException, InterruptedException
    {
        SpatialIndex<TestRecord> index =
            SpatialIndex.newSpatialIndex(SPACE, newIndex(true), SpatialIndex.Options.SINGLE_CELL);
        TestRecord.Factory recordFactory = new TestRecord.Factory();
        for (int i = 0; i < n; i++) {
            SpatialObject point = testPoint();
            index.add(point, recordFactory.setup(point, i));
        }
        return index;
    }

    private SpatialObject testPoint()
    {
        int x = random.nextInt(NX);
        int y = random.nextInt(NY);
        return
            USE_JTS
            ? JTS.spatialObject(SPACE, factory.createPoint(new Coordinate(x, y)))
            : new Point(x, y);
    }

    private static final int NX = 1_000_000;
    private static final int NY = 1_000_000;
    private static final int X_BITS = 20;
    private static final int Y_BITS = 20;
    private static final Space SPACE = Space.newSpace(new double[]{0, 0},
                                                      new double[]{NX, NY},
                                                      new int[]{X_BITS, Y_BITS});
    private static final SpatialJoin.Filter FILTER =
        USE_JTS
        ? new TestFilterJTS()
        : new TestFilter();

    private final Random random = new Random(12345);
    private final GeometryFactory factory = new GeometryFactory();

    private static final class TestFilter implements SpatialJoin.Filter<Box, TestRecord>
    {
        @Override
        public boolean overlap(Box box, TestRecord record)
        {
            Point point = (Point) record.spatialObject();
            double px = point.x();
            double py = point.y();
            return
                box.xLo() <= px && px <= box.xHi() &&
                box.yLo() <= py && py <= box.yHi();
        }
    }

    private static final class TestFilterJTS implements SpatialJoin.Filter<Box, TestRecord>
    {
        @Override
        public boolean overlap(Box box, TestRecord record)
        {
            JTSPoint point = (JTSPoint) record.spatialObject();
            Coordinate coordinate = point.point().getCoordinate();
            double px = coordinate.x;
            double py = coordinate.y;
            return
                box.xLo() <= px && px <= box.xHi() &&
                box.yLo() <= py && py <= box.yHi();
        }
    }
}
