/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialjoin;

public class Counters
{
    public long ancestorFind()
    {
        return ancestorFind;
    }

    public void countAncestorFind()
    {
        ancestorFind++;
    }

    public long ancestorInCache()
    {
        return ancestorInCache;
    }

    public void countAncestorInCache()
    {
        ancestorInCache++;
    }

    public static Counters forThread()
    {
        return THREAD_COUNTERS.get();
    }

    private static final ThreadLocal<Counters> THREAD_COUNTERS =
        new ThreadLocal<Counters>()
        {
            @Override
            protected Counters initialValue()
            {
                return new Counters();
            }
        };

    private long ancestorFind = 0;
    private long ancestorInCache = 0;
}
