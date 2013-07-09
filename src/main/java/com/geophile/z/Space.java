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
    public final ApplicationSpace applicationSpace()
    {
        return applicationSpace;
    }

    /**
     * Creates a {@link Space}.
     * The space has xBits.length dimensions. A coordinate of dimension d
     * must lie between 0 inclusive and 2**xBits[d] exclusive.
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
