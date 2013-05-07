/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

import com.geophile.z.space.SpaceImpl;

/**
 * A {@link Space} represents the space in which {@link com.geophile.z.spatialobject.SpatialObject}s reside. The lower bound of each dimension
 * is zero, and the upper bound of a dimension is given only approximately, as a power of 2, (i.e., the number of
 * bits needed to represent a number for that dimension). More precise bounds, including non-zero lower bounds,
 * are the application's responsibility.
 */

public abstract class Space
{
    /**
     * Creates a {@link Space}. The space has xBits.length dimensions. A coordinate of dimension d
     * must lie between 0 inclusive and 2**xBits[d] - 1 exclusive.
     * @param xBits Specifies the dimensions and extent of the space.
     */
    public static Space newSpace(int[] xBits)
    {
        return new SpaceImpl(xBits, null);
    }

    /**
     * Creates a {@link Space}. The space has xBits.length dimensions. A coordinate of dimension d
     * must lie between 0 inclusive and 2**xBits[d] - 1 exclusive.
     * @param xBits Specifies the dimensions and extent of the space.
     * @param interleave Specifies the how bits of coordinates are interleaved. 0 <= interleave[i] < 2**xBits[d],
     *     0 <= d < 2**xBits.length. The more bits that are present for a given dimension earlier in the interleaving,
     *     the more the spatial index will be optimized for selectivity in that dimension.
     */
    public static Space newSpace(int[] xBits, int[] interleave)
    {
        return new SpaceImpl(xBits, interleave);
    }

    public static final int MAX_DIMENSIONS = 6;
}
