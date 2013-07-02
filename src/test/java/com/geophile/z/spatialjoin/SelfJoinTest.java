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
        SpatialJoinFilter filter =
            new SpatialJoinFilter()
            {
                @Override
                public boolean overlap(SpatialObject a, SpatialObject b)
                {
                    return a.equalTo(b);
                }
            };
        for (int maxXSize : MAX_SIZES) {
            for (int maxYSize : MAX_SIZES) {
                TestInput input = load(Side.LEFT, COUNT, maxXSize, maxYSize);
                Set<SpatialObject> actual = new HashSet<>();
                Iterator<Pair> iterator =
                    SpatialJoin.newSpatialJoin(filter, SpatialJoin.Duplicates.EXCLUDE)
                               .iterator(input.spatialIndex(), input.spatialIndex());
                while (iterator.hasNext()) {
                    Pair pair = iterator.next();
                    SpatialObject box = pair.left();
                    assertTrue(box.equalTo(pair.right()));
                    actual.add(box);
                }
                assertEquals(new HashSet<>(input.spatialObjects()), actual);
            }
        }
    }

    @Override
    protected Space space()
    {
        return SPACE;
    }

    @Override
    protected Box newLeftObject(int maxXSize, int maxYSize)
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
        return true;
    }

    private static final int NX = 1_000_000;
    private static final int NY = 1_000_000;
    private static final int LOG_NX = 20;
    private static final int LOG_NY = 20;
    private static final Space SPACE = Space.newSpace(appSpace(0, NX, 0, NY), LOG_NX, LOG_NY);
    private static final int COUNT = 10_000;
    private static final int[] MAX_SIZES = new int[]{1, 10_000, /* 1% */ 100_000 /* 10% */};
}
