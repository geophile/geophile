/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.spatialobject.d2;

import com.geophile.z.Region;
import com.geophile.z.RegionComparison;
import com.geophile.spatialobject.SpatialObject;

/**
 * A 2-dimensional box that can be stored by a {@link com.geophile.SpatialIndex}.
 */

public class Box implements SpatialObject
{
    // Object interface

    @Override
    public int hashCode()
    {
        long h =
            1000000007L * xLo ^
            1000000009L * xHi ^
            1000000021L * yLo ^
            1000000033L * yHi;
        return ((int) (h >>> 32)) ^ (int) h;
    }

    @Override
    public boolean equals(Object o)
    {
        boolean equals = false;
        if (o != null && o instanceof Box) {
            Box that = (Box) o;
            equals = 
                this.xLo == that.xLo &&
                this.xHi == that.xHi &&
                this.yLo == that.yLo &&
                this.yHi == that.yHi;
        }
        return equals;
    }

    @Override
    public String toString()
    {
        return String.format("(%s:%s, %s:%s)", xLo, xHi, yLo, yHi);
    }

    // SpatialObject interface

    public long[] arbitraryPoint()
    {
        return new long[]{xLo, yLo};
    }

    public boolean containedBy(Region region)
    {
        return
            region.lo(0) <= xLo && xHi <= region.hi(0) &&
            region.lo(1) <= yLo && yHi <= region.hi(1);
    }

    public RegionComparison compare(Region region)
    {
        long rXLo = region.lo(0);
        long rYLo = region.lo(1);
        long rXHi = region.hi(0);
        long rYHi = region.hi(1);
        if (xLo <= rXLo && rXHi <= xHi && yLo <= rYLo && rYHi <= yHi) {
            return RegionComparison.REGION_INSIDE_OBJECT;
        } else if (rXHi < xLo || rXLo > xHi || rYHi < yLo || rYLo > yHi) {
            return RegionComparison.REGION_OUTSIDE_OBJECT;
        } else {
            return RegionComparison.REGION_OVERLAPS_OBJECT;
        }
    }

    // Box interface

    /**
     * Returns the left boundary of this box.
     * @return The left boundary of this box.
     */
    public long xLo()
    {
        return xLo;
    }

    /**
     * Returns the right boundary of this box.
     * @return The right boundary of this box.
     */
    public long xHi()
    {
        return xHi;
    }

    /**
     * Returns the lower boundary of this box.
     * @return The lower boundary of this box.
     */
    public long yLo()
    {
        return yLo;
    }

    /**
     * Returns the upper boundary of this box.
     * @return The upper boundary of this box.
     */
    public long yHi()
    {
        return yHi;
    }

    /**
     * Creates a box containing points (x, y) such that xLo <= x <= xHi, and
     * yLo <= y <= yHi.
     * @param xLo The left boundary of the box.
     * @param xHi The right boundary of the box.
     * @param yLo The lower boundary of the box.
     * @param yHi The upper boundary of the box.
     */
    public Box(long xLo, long xHi, long yLo, long yHi)
    {
        this.xLo = xLo;
        this.xHi = xHi;
        this.yLo = yLo;
        this.yHi = yHi;
    }

    // Object state

    private final long xLo;
    private final long xHi;
    private final long yLo;
    private final long yHi;
}
