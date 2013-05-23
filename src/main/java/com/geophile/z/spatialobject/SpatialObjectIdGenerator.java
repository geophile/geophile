package com.geophile.z.spatialobject;

import java.util.concurrent.atomic.AtomicLong;

public class SpatialObjectIdGenerator
{
    public static long newId()
    {
        return idCounter.getAndIncrement();
    }

    private static final AtomicLong idCounter = new AtomicLong(0);
}
