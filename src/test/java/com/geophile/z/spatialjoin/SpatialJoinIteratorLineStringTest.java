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
import com.geophile.z.spatialobject.jts.JTSLineString;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.Level;

import static org.junit.Assert.assertEquals;

public class SpatialJoinIteratorLineStringTest extends SpatialJoinIteratorTestBase
{
    @Test
    public void test() throws IOException, InterruptedException
    {
        enableLogging();
        this.space = Space.newSpace(appSpace(0, NX, 0, NY), X_BITS, Y_BITS);
        TestInput leftInput = load(Side.LEFT, COUNT, X_MAX_SEGMENT_DISTANCE, Y_MAX_SEGMENT_DISTANCE);
        TestInput rightInput;
        for (int trial = 0; trial < TRIALS; trial++) {
            rightInput = load(Side.RIGHT, COUNT, X_MAX_SEGMENT_DISTANCE, Y_MAX_SEGMENT_DISTANCE);
            test(leftInput, rightInput, filter, SpatialJoin.Duplicates.EXCLUDE);
        }
    }

    @Override
    protected Space space()
    {
        return space;
    }

    // Each line string comprises two line segments.
    @Override
    protected JTSLineString newLeftObject(int maxXSize, int maxYSize)
    {
        int nx = (int) (appSpace.hi(0) - appSpace.lo(0));
        int ny = (int) (appSpace.hi(1) - appSpace.lo(1));
        Coordinate[] coords = new Coordinate[3];
        int c = 0;
        long x = random.nextInt(nx);
        long y = random.nextInt(ny);
        coords[c++] = new Coordinate(x, y);
        for (int i = 0; i < 2; i++) {
            long xNew;
            long yNew;
            do {
                xNew = x - maxXSize + random.nextInt(2 * maxXSize);
                yNew = y - maxYSize + random.nextInt(2 * maxYSize);
            } while (!(xNew >= 0 && xNew < nx && yNew >= 0 && yNew < ny));
            coords[c++] = new Coordinate(xNew, yNew);
            x = xNew;
            y = yNew;
        }
        return new JTSLineString(space, factory.createLineString(coords));
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
    protected boolean printSummary()
    {
        return true;
    }

    @Override
    protected boolean overlap(SpatialObject x, SpatialObject y)
    {
        JTSLineString a = (JTSLineString) x;
        JTSLineString b = (JTSLineString) y;
        return a.geometry().intersects(b.geometry());
    }

    @Override
    protected Level logLevel()
    {
        return Level.WARNING;
    }


    private static final int COUNT = 10_000;
    private static final int NX = 1_000_000;
    private static final int NY = 1_000_000;
    private static final int X_BITS = 20;
    private static final int Y_BITS = 20;
    private static final int TRIALS = 20;
    // max x-distance between two successive points of the same linestring.
    private static final int X_MAX_SEGMENT_DISTANCE = 1_000;
    // max y-distance between two successive points of the same linestring.
    private static final int Y_MAX_SEGMENT_DISTANCE = 1_000;
    private Space space;
    private ApplicationSpace appSpace = appSpace(0, NX, 0, NY);
    private final GeometryFactory factory = new GeometryFactory();
    private final SpatialJoinFilter filter = new SpatialJoinFilter()
    {
        @Override
        public boolean overlap(SpatialObject x, SpatialObject y)
        {
            testStats.filterCount++;
            boolean overlap = SpatialJoinIteratorLineStringTest.this.overlap(x, y);
            if (overlap) {
                testStats.overlapCount++;
            }
            return overlap;
        }
    };
}
