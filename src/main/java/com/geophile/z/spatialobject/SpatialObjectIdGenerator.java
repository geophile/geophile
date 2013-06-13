package com.geophile.z.spatialobject;

import java.util.concurrent.atomic.AtomicLong;

public class SpatialObjectIdGenerator
{
    public static long newId()
    {
        return idCounter.getAndIncrement();
    }

    public static void reset(long maxIdSoFar)
    {
        idCounter.set(maxIdSoFar + 1);
    }

    private static final AtomicLong idCounter = new AtomicLong(0);
}
