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

public class SpatialJoinIteratorProfile extends SpatialJoinIteratorTestBase
{
    public static void main(String[] args)
    {
        new SpatialJoinIteratorProfile().run();
    }

    private void run()
    {
        test(1, 100_000, 100_000, 1, 10_000);
        Counters counters = Counters.forThread();
        double hitRate = (double) counters.ancestorInCache() / counters.ancestorFind();
        print("Ancestor cache hit rate: %s", hitRate);
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
