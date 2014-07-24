package com.geophile.util;

public class Stopwatch
{
    public void start()
    {
        start = System.nanoTime();
    }

    public long stop()
    {
        long stop = System.nanoTime();
        long delta = stop - start;
        accumulated += delta;
        return delta;
    }

    public void reset()
    {
        accumulated = 0;
        start();
    }

    public long nSec()
    {
        return accumulated;
    }

    public Stopwatch()
    {
        start();
    }

    private long start;
    private long accumulated = 0;
}
