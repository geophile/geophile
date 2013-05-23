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

import com.geophile.z.SpatialJoin;

import java.util.EnumSet;

public class SpatialJoinIteratorProfile extends SpatialJoinIteratorTestBase
{
    public static void main(String[] args)
    {
        new SpatialJoinIteratorProfile().run();
    }

    private void run()
    {
        // enableLogging(Level.FINE);
        final int SPATIAL_JOINS = 10_000;
        test(1, 100_000, 100_000, 1, SPATIAL_JOINS, EnumSet.of(SpatialJoin.Duplicates.INCLUDE));
        Counters counters = Counters.forThread();
        double entersPerJoin = (double) counters.enterZ() / SPATIAL_JOINS;
        print("enters per join: %s", entersPerJoin);
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
}
