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
import com.geophile.z.Pair;
import com.geophile.z.Space;
import com.geophile.z.SpatialJoin;
import com.geophile.z.spatialobject.d2.Box;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SelfJoinTest extends SpatialJoinIteratorTestBase
{
    @Test
    public void selfJoin() throws IOException, InterruptedException
    {
        SpatialJoinFilter<Box, Box> filter =
            new SpatialJoinFilter<Box, Box>()
            {
                @Override
                public boolean overlap(Box a, Box b)
                {
                    return a.equalTo(b);
                }
            };
        for (int maxXSize : MAX_SIZES) {
            for (int maxYSize : MAX_SIZES) {
                TestInput input = loadBoxes(COUNT, maxXSize, maxYSize);
                Set<Box> actual = new HashSet<>();
                Iterator<Pair<Box, Box>> iterator =
                    SpatialJoin.newSpatialJoin(filter, SpatialJoin.Duplicates.EXCLUDE)
                               .iterator(input.spatialIndex(), input.spatialIndex());
                while (iterator.hasNext()) {
                    Pair<Box, Box> pair = iterator.next();
                    Box box = pair.left();
                    assertTrue(box.equalTo(pair.right()));
                    actual.add(box);
                }
                assertEquals(new HashSet<>(input.boxes()), actual);
            }
        }
    }

    @Override
    protected Space space()
    {
        return SPACE;
    }

    @Override
    protected Box testBox(int maxXSize, int maxYSize)
    {
        long xLo = random.nextInt(NX - maxXSize);
        long xHi = xLo + (maxXSize == 1 ? 0 : random.nextInt(maxXSize));
        long yLo = random.nextInt(NY - maxYSize);
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

    private static final ApplicationSpace APP_SPACE =
        new ApplicationSpace()
        {
            @Override
            public int dimensions()
            {
                return 2;
            }

            @Override
            public double lo(int d)
            {
                return 0;
            }

            @Override
            public double hi(int d)
            {
                switch (d) {
                    case 0: return NX;
                    case 1: return NY;
                }
                assert false;
                return Double.NaN;
            }
        };

    private static final int NX = 1_000_000;
    private static final int NY = 1_000_000;
    private static final int LOG_NX = 20;
    private static final int LOG_NY = 20;
    private static final Space SPACE = Space.newSpace(APP_SPACE, LOG_NX, LOG_NY);
    private static final int COUNT = 10_000;
    private static final int[] MAX_SIZES = new int[]{1, 10_000, /* 1% */ 100_000 /* 10% */};
}
