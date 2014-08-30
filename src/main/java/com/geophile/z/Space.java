/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

import com.geophile.z.space.ApplicationSpace;
import com.geophile.z.space.SpaceImpl;

/**
 * A Space represents the space in which {@link SpatialObject}s reside.
 * Conceptually, a Space is a multi-dimensional grid of cells.
 */

public abstract class Space
{
    /**
     * The number of dimensions of this Space.
     * @return The number of dimensions of this space.
     */
    public abstract int dimensions();

    /**
     * The low bound of dimension d.
     * @param d A dimension of the space, 0 <= d < dimensions()
     * @return The low coordinate of dimension d.
     */
    public abstract double lo(int d);

    /**
     * The high bound of dimension d.
     * @param d A dimension of the space, 0 <= d < dimensions()
     * @return The high coordinate of dimension d.
     */
    public abstract double hi(int d);

    /**
     * Decompose spatialObject into z-values, stored in the zs array. The maximum number of z-values is
     * zs.length. If fewer are needed, then the unused array positions are denoted by Z_NULL at the end of the array.
     * @param spatialObject The SpatialObject to be decomposed.
     * @param zs The array containing the z-values resulting from the decomposition.
     */
    public abstract void decompose(SpatialObject spatialObject, long[] zs);

    /**
     * Returns the lower bound of the given z-value;
     * @return The lower bound of the given z-value;
     */
    public static long zLo(long z)
    {
        return SpaceImpl.zLo(z);
    }

    /**
     * Returns the upper bound of the given z-value;
     * @return The upper bound of the given z-value;
     */
    public static long zHi(long z)
    {
        return SpaceImpl.zHi(z);
    }

    /**
     * Creates a Space.
     * The space has xBits.length dimensions. A coordinate of dimension d
     * must lie between 0 inclusive and 2**xBits[d] exclusive. The sum of the xBits must not exceed 57.
     * @param lo Low coordinates of the space.
     * @param hi High coordinates of the space.
     * @param xBits Specifies the number of bits of resolution along each dimension.
     */
    public static Space newSpace(double[] lo, double[] hi, int[] xBits)
    {
        return newSpace(lo, hi, xBits, null);
    }

    /**
     * Creates a Space, providing a greater degree of control over performance than
     * {@link Space#newSpace(double[], double[], int[])}.
     * The space has xBits.length dimensions. A coordinate of dimension d
     * must lie between 0 inclusive and 2**xBits[d] exclusive.
     * The sum of the xBits must not exceed 57.
     * @param lo Low coordinates of the space.
     * @param hi High coordinates of the space.
     * @param xBits Specifies the dimensions and extent of the space.
     * @param interleave Specifies the how bits of coordinates are interleaved. 0 <= interleave[i] < 2**xBits[d],
     *     0 <= d < 2**xBits.length. The more bits that are present for a given dimension earlier in the interleaving,
     *     the more the spatial index will be optimized for selectivity in that dimension.
     */
    public static Space newSpace(double[] lo, double[] hi, int[] xBits, int[] interleave)
    {
        return new SpaceImpl(lo, hi, xBits, interleave);
    }

    /**
     * The maximum number of dimensions of a Space.
     */
    public static final int MAX_DIMENSIONS = 6;

    // For use by subclasses

    protected Space(double[] lo, double[] hi)
    {
        this.applicationSpace = new ApplicationSpace(lo, hi);
    }

    // Class state

    public static final long Z_NULL = -1;

    // Object state

    protected ApplicationSpace applicationSpace;
}
