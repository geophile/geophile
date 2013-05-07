/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialobject.d2;

import com.geophile.z.space.Region;
import com.geophile.z.space.RegionComparison;
import com.geophile.z.spatialobject.SpatialObject;

/**
 * A 2-dimensional point that can be stored by a {@link com.geophile.SpatialIndex}.
 */

public class Point implements SpatialObject
{
    // Object interface

    @Override
    public int hashCode()
    {
        long h =
            1000000087L * x ^
            1000000093L * y;
        return ((int) (h >>> 32)) ^ (int) h;
    }

    @Override
    public boolean equals(Object o)
    {
        boolean equals = false;
        if (o != null && o instanceof Point) {
            Point that = (Point) o;
            equals = this.x == that.x && this.y == that.y;
        }
        return equals;
    }

    @Override
    public String toString()
    {
        return String.format("(%s, %s)", x, y);
    }

    // SpatialObject interface

    public long[] arbitraryPoint()
    {
        return new long[]{x, y};
    }

    public boolean containedBy(Region region)
    {
        return
            region.lo(0) <= x && x <= region.hi(0) &&
            region.lo(1) <= y && y <= region.hi(1);
    }

    public RegionComparison compare(Region region)
    {
        long rXLo = region.lo(0);
        long rYLo = region.lo(1);
        long rXHi = region.hi(0);
        long rYHi = region.hi(1);
        if (region.isPoint() && rXLo == x && rYLo == y) {
            return RegionComparison.REGION_INSIDE_OBJECT;
        } else if (rXHi < x || rXLo > x || rYHi < y || rYLo > y) {
            return RegionComparison.REGION_OUTSIDE_OBJECT;
        } else {
            return RegionComparison.REGION_OVERLAPS_OBJECT;
        }
    }

    // Point interface

    /**
     * Returns the point's x coordinate.
     * @return The point's x coordinate.
     */
    public long x()
    {
        return x;
    }

    /**
     * Returns the point's y coordinate.
     * @return The point's y coordinate.
     */
    public long y()
    {
        return y;
    }

    /**
     * Creates a point at (x, y).
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    public Point(long x, long y)
    {
        this.x = x;
        this.y = y;
    }

    // Object state

    private final long x;
    private final long y;
}
