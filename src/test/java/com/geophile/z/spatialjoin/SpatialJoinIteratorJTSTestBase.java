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

import com.geophile.z.ApplicationSpace;
import com.geophile.z.Space;
import com.geophile.z.SpatialJoin;
import com.geophile.z.SpatialObject;
import com.geophile.z.spatialobject.jts.JTSBase;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.Level;

import static org.junit.Assert.assertEquals;

public abstract class SpatialJoinIteratorJTSTestBase extends SpatialJoinIteratorTestBase
{
    @Test
    public void test() throws IOException, InterruptedException
    {
        enableLogging();
        appSpace = appSpace(0, NX, 0, NY);
        space = Space.newSpace(appSpace, X_BITS, Y_BITS);
        TestInput leftInput = load(Side.LEFT, COUNT, X_MAX_SEGMENT_DISTANCE, Y_MAX_SEGMENT_DISTANCE);
        TestInput rightInput;
        for (int trial = 0; trial < TRIALS; trial++) {
            rightInput = load(Side.RIGHT, COUNT, X_MAX_SEGMENT_DISTANCE, Y_MAX_SEGMENT_DISTANCE);
            test(leftInput, rightInput, filter, SpatialJoin.Duplicates.EXCLUDE);
        }
    }

    @Override
    protected final Space space()
    {
        return space;
    }

    @Override
    protected final void checkEquals(Object expected, Object actual)
    {
        assertEquals(expected, actual);
    }

    @Override
    protected final boolean verify()
    {
        return true;
    }

    @Override
    protected final boolean printSummary()
    {
        return true;
    }

    @Override
    protected final boolean overlap(SpatialObject x, SpatialObject y)
    {
        JTSBase a = (JTSBase) x;
        JTSBase b = (JTSBase) y;
        return a.geometry().intersects(b.geometry());
    }

    @Override
    protected final Level logLevel()
    {
        return Level.WARNING;
    }

    private static final int COUNT = 10_000;
    private static final int NX = 1_000_000;
    private static final int NY = 1_000_000;
    private static final int X_BITS = 20;
    private static final int Y_BITS = 20;
    private static final int TRIALS = 20;
    // max x-distance between two successive points of the same shape.
    private static final int X_MAX_SEGMENT_DISTANCE = 1_000;
    // max y-distance between two successive points of the same shape.
    private static final int Y_MAX_SEGMENT_DISTANCE = 1_000;
    protected final GeometryFactory factory = new GeometryFactory();
    protected Space space;
    protected ApplicationSpace appSpace;
    private final SpatialJoinFilter filter = new SpatialJoinFilter()
    {
        @Override
        public boolean overlap(SpatialObject x, SpatialObject y)
        {
            testStats.filterCount++;
            boolean overlap = SpatialJoinIteratorJTSTestBase.this.overlap(x, y);
            if (overlap) {
                testStats.overlapCount++;
            }
            return overlap;
        }
    };
}
