/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

import com.geophile.z.space.SpaceImpl;

/**
 * A Space represents the space in which {@link SpatialObject}s reside. The lower bound
 * of each dimension is zero, and the upper bounds are given when the Space is created. Conceptually, a Space is a
 * multi-dimensional grid of cells.
 */

public abstract class Space
{
    /**
     * Returns the {@link com.geophile.z.ApplicationSpace} associated with this Space.
     * @return The {@link com.geophile.z.ApplicationSpace} associated with this Space.
     */
    public final ApplicationSpace applicationSpace()
    {
        return applicationSpace;
    }

    /**
     * Decompose spatialObject into z-values, stored in the zs array. The maximum number of z-values is
     * zs.length. If fewer are needed, then the unused array positions are denoted by -1 at the end of the array.
     * @param spatialObject The SpatialObject to be decomposed.
     * @param zs The array containing the z-values resulting from the decomposition.
     */
    public abstract void decompose(SpatialObject spatialObject, long[] zs);

    /**
     * Returns the z-value for a point in the application space.
     * @param point Point coordinates.
     * @return z-value of the point.
     */
    public abstract long shuffle(long[] point);

    /**
     * Convert a coordinate in application space to geophile space.
     * @param d dimension of the space.
     * @param appCoord coordinate in application space to transform
     * @return position in geophile space.
     */
    public abstract long appToZ(int d, double appCoord);

    public static long zLo(long z)
    {
        return SpaceImpl.zLo(z);
    }

    public static long zHi(long z)
    {
        return SpaceImpl.zHi(z);
    }

    /**
     * Creates a {@link Space}.
     * The space has xBits.length dimensions. A coordinate of dimension d
     * must lie between 0 inclusive and 2**xBits[d] exclusive. The sum of the xBits must not exceed 57.
     * @param xBits Specifies the number of bits of resolution along each dimension.
     */
    public static Space newSpace(ApplicationSpace applicationSpace, int ... xBits)
    {
        return newSpace(applicationSpace, xBits, null);
    }

    /**
     * Creates a {@link Space}, providing a greater degree of control over performance than
     * {@link Space#newSpace(ApplicationSpace, int...)}.
     * The space has xBits.length dimensions. A coordinate of dimension d
     * must lie between 0 inclusive and 2**xBits[d] exclusive.
     * The sum of the xBits must not exceed 57.
     * @param xBits Specifies the dimensions and extent of the space.
     * @param interleave Specifies the how bits of coordinates are interleaved. 0 <= interleave[i] < 2**xBits[d],
     *     0 <= d < 2**xBits.length. The more bits that are present for a given dimension earlier in the interleaving,
     *     the more the spatial index will be optimized for selectivity in that dimension.
     */
    public static Space newSpace(ApplicationSpace applicationSpace, int[] xBits, int[] interleave)
    {
        return new SpaceImpl(applicationSpace, xBits, interleave);
    }

    /**
     * The maximum number of dimensions of a Space.
     */
    public static final int MAX_DIMENSIONS = 6;

    protected Space(ApplicationSpace applicationSpace)
    {
        this.applicationSpace = applicationSpace;
    }

    protected ApplicationSpace applicationSpace;
}
