package com.geophile.z.space;

import java.util.Arrays;

public final class ApplicationSpace
{
    // ApplicationSpace interface

    public int dimensions()
    {
        return lo.length;
    }

    public double lo(int d)
    {
        return lo[d];
    }

    public double hi(int d)
    {
        return hi[d];
    }

    public ApplicationSpace(double[] lo, double[] hi)
    {
        if (lo.length != hi.length) {
            throw new IllegalArgumentException(String.format("lo.length = %s, hi.length = %s", lo.length, hi.length));
        }
        for (int d = 0; d < lo.length; d++) {
            if (lo[d] >= hi[d]) {
                throw new IllegalArgumentException(String.format("lo[%s] = %s, hi[%s] = %s", d, lo[d], d, hi[d]));
            }
        }
        this.lo = Arrays.copyOf(lo, lo.length);
        this.hi = Arrays.copyOf(hi, hi.length);
    }

    // Object state

    private double[] lo;
    private double[] hi;
}
