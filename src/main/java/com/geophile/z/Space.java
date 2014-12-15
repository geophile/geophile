/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

import com.geophile.z.space.ApplicationSpace;
import com.geophile.z.space.SpaceImpl;

/**
 * A Space represents the space in which {@link com.geophile.z.SpatialObject}s reside. The space can be
 * of any number of dimensions, but Geophile is unlikely to be effective beyond six dimensions. For higher
 * dimensions, spatial indexing will probably not perform better than either a full scan of the objects of interest,
 * or possibly specialized high-dimension data structures.
 *
 * Geophile deals with two coordinate systems. First, there is the coordinate system defined by the application.
 * The bounds of this coordinate system are defined by the <tt>lo</tt> and <tt>hi</tt> arguments to
 * {@link com.geophile.z.Space#newSpace(double[] lo, double[] hi, int[] gridBits)}.
 *
 * Second, there is the coordinate system used by Geophile internally. This is a grid of cells,
 * defined by the <tt>gBits</tt> argument to
 * {@link com.geophile.z.Space#newSpace(double[] lo, double[] hi, int[] gridBits)}.
 * Geophile works with an approximate representation of spatial objects, constructed by noting which
 * grid cells contain any part of a spatial object being indexed. The grid is not stored explicitly,
 * (see {@link com.geophile.z.space.Region} for more details).
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
     * @param d A dimension of the space, 0 ^lt;= d &lt; dimensions()
     * @return The low coordinate of dimension d.
     */
    public abstract double lo(int d);

    /**
     * The high bound of dimension d.
     * @param d A dimension of the space, 0 &lt;= d &lt; dimensions()
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
     * @param gridBits Specifies the number of bits of resolution along each dimension.
     * @return A new Space
     */
    public static Space newSpace(double[] lo, double[] hi, int[] gridBits)
    {
        return newSpace(lo, hi, gridBits, null);
    }

    /**
     * Creates a Space, providing a greater degree of control over performance than
     * {@link Space#newSpace(double[], double[], int[])}.
     * The space has xBits.length dimensions. A coordinate of dimension d
     * must lie between 0 inclusive and 2**xBits[d] exclusive.
     * The sum of the xBits must not exceed 57.
     * @param lo Low coordinates of the space.
     * @param hi High coordinates of the space.
     * @param gridBits Specifies the dimensions and extent of the space.
     * @param interleave Specifies the how bits of coordinates are interleaved. 0 &lt;= interleave[i] &lt; 2**xBits[d],
     * @return A new Space
     *     0 &lt;= d &lt; 2**xBits.length. The more bits that are present for a given dimension earlier in the interleaving,
     *     the more the spatial index will be optimized for selectivity in that dimension.
     */
    public static Space newSpace(double[] lo, double[] hi, int[] gridBits, int[] interleave)
    {
        return new SpaceImpl(lo, hi, gridBits, interleave);
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
