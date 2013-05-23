/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialjoin;

import com.geophile.z.Pair;
import com.geophile.z.SpatialJoin;
import com.geophile.z.spatialobject.d2.Box;
import org.junit.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SpatialJoinIteratorTest extends SpatialJoinIteratorTestBase
{
    @Test
    public void test()
    {
        for (int nLeft : COUNTS) {
            int nRight = MAX_COUNT / nLeft;
            assertEquals(MAX_COUNT, nLeft * nRight);
            for (int maxLeftXSize : MAX_X_SIZES) {
                for (int maxRightXSize : MAX_X_SIZES) {
                    test(nLeft,
                         maxLeftXSize,
                         nRight,
                         maxRightXSize,
                         TRIALS,
                         EnumSet.of(SpatialJoin.Duplicates.INCLUDE, SpatialJoin.Duplicates.EXCLUDE));
                }
            }
        }
    }

    @Test
    public void selfJoin()
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
        for (int maxXSize : MAX_X_SIZES) {
            TestInput input = loadBoxes(10_000, maxXSize);
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
}
