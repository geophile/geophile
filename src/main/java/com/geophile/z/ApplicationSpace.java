package com.geophile.z;

import java.util.Arrays;

/**
 * An ApplicationSpace represents the application's space containing spatial objects.
 */

public final class ApplicationSpace
{
    // ApplicationSpace interface

    /**
     * The dimensionality of this ApplicationSpace.
     * @return The dimensionality of this ApplicationSpace.
     */
    public int dimensions()
    {
        return lo.length;
    }

    /**
     * lo(d) is the lowest coordinate value for dimension d.
     * @param d a dimension of the space, 0 <= d < dimensions()
     * @return the lowest coordinate for dimension d.
     */
    public double lo(int d)
    {
        return lo[d];
    }

    /**
     * hi(d) is the highest coordinate value for dimension d.
     * @param d a dimension of the space, 0 <= d < dimensions()
     * @return the highest coordinate for dimension d.
     */
    public double hi(int d)
    {
        return hi[d];
    }

    /**
     * Creates a new application space. The number of dimensions is lo.length, which must match hi.length.
     * The range of coordinates for dimension d is lo[d] to hi[d] inclusive.
     * @param lo The low coordinates for the space.
     * @param hi The high coordinates for the space.
     * @return A new ApplicationSpace.
     */
    public static ApplicationSpace newApplicationSpace(double[] lo, double[] hi)
    {
        return new ApplicationSpace(lo, hi);
    }

    // For use by this class

    private ApplicationSpace(double[] lo, double[] hi)
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
