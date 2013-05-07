/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.space;

import java.util.Arrays;

/**
 * A {@link Region} represents a box-shaped subspace whose edges are parallel to the dimensions of the space.
 * Regions are only relevant to users of geophile who define {@link com.geophile.spatialobject.SpatialObject} subtypes.
 */

public class Region
{
    // Object interface

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append('(');
        for (int d = 0; d < dimensions; d++) {
            if (d > 0) {
                buffer.append(", ");
            }
            buffer.append(lo(d));
            buffer.append(':');
            buffer.append(hi(d));
        }
        buffer.append(')');
        return buffer.toString();
    }

    // Region interface

    /**
     * Returns the region's lower bound in the dth dimension.
     * @param d A dimension of the space.
     * @return The region's lower bound in the dth dimension.
     */
    public long lo(int d)
    {
        return lo[d];
    }

    /**
     * Returns the region's upper bound in the dth dimension.
     * @param d A dimension of the space.
     * @return The region's upper bound in the dth dimension.
     */
    public long hi(int d)
    {
        return hi[d];
    }

    /**
     * Returns true iff the region represents a single cell in the space.
     * @return True iff the region represents a single cell in the space.
     */
    public boolean isPoint()
    {
        return level == space.zBits;
    }

    // For use by this package

    void downLeft()
    {
        int d = interleave[level];
        hi[d] &= ~(1L << --xBitPosition[d]);
        level++;
    }

    void downRight()
    {
        int d = interleave[level];
        lo[d] |= 1L << --xBitPosition[d];
        level++;
    }

    void up()
    {
        level--;
        int d = interleave[level];
        lo[d] &= ~(1L << xBitPosition[d]);
        hi[d] |= 1L << xBitPosition[d];
        xBitPosition[d]++;
    }

    long z()
    {
        return space.shuffle(lo, level);
    }

    Region copy()
    {
        return new Region(this);
    }

    Region(SpaceImpl space, long[] lo, long[] hi, int level)
    {
        assert lo.length == space.dimensions;
        assert hi.length == space.dimensions;
        this.space = space;
        this.dimensions = space.dimensions;
        this.interleave = space.interleave;
        this.lo = Arrays.copyOf(lo, lo.length);
        this.hi = Arrays.copyOf(hi, hi.length);
        this.level = level;
        this.xBitPosition = new int[dimensions];
        for (int zBitPosition = space.zBits - 1; zBitPosition >= level; zBitPosition--) {
            int d = interleave[zBitPosition];
            xBitPosition[d]++;
        }
    }

    // For use by this class

    private Region(Region region)
    {
        this.space = region.space;
        this.dimensions = region.dimensions;
        this.interleave = region.interleave;
        this.lo = Arrays.copyOf(region.lo, region.lo.length);
        this.hi = Arrays.copyOf(region.hi, region.hi.length);
        this.level = region.level;
        this.xBitPosition = Arrays.copyOf(region.xBitPosition, region.xBitPosition.length);
    }

    // Object state

    private final SpaceImpl space;
    private final int dimensions;
    private final int[] interleave;
    private final long[] lo;
    private final long[] hi;
    private int level;
    private int[] xBitPosition;
}
