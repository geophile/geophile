/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialobject.d2;

import com.geophile.z.SpatialObject;
import com.geophile.z.space.Region;
import com.geophile.z.space.RegionComparison;
import com.geophile.z.spatialobject.SpatialObjectIdGenerator;

import java.nio.ByteBuffer;

/**
 * A 2-dimensional point that can be stored by a {@link com.geophile.z.SpatialIndex}.
 */

public class Point implements SpatialObject
{
    // Object interface

    @Override
    public int hashCode()
    {
        long h =
            (1000000087L * x) ^
            (1000000093L * y);
        return ((int) (h >>> 32)) ^ (int) h;
    }

    @Override
    public boolean equals(Object o)
    {
        return o != null && o instanceof Point && equalTo((Point) o);
    }

    @Override
    public String toString()
    {
        return String.format("(%s, %s)", x, y);
    }

    // SpatialObject interface

    @Override
    public long id()
    {
        return id;
    }

    @Override
    public long[] arbitraryPoint()
    {
        return new long[]{x, y};
    }

    @Override
    public int maxZ()
    {
        return 1;
    }

    @Override
    public boolean equalTo(SpatialObject spatialObject)
    {
        boolean eq = false;
        if (spatialObject != null && spatialObject instanceof Point) {
            Point that = (Point) spatialObject;
            eq = this.x == that.x && this.y == that.y;
        }
        return eq;
    }

    @Override
    public boolean containedBy(Region region)
    {
        return
            region.lo(0) <= x && x <= region.hi(0) &&
            region.lo(1) <= y && y <= region.hi(1);
    }

    @Override
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

    @Override
    public void readFrom(ByteBuffer buffer)
    {
        id = buffer.getLong();
        x = buffer.getLong();
        y = buffer.getLong();
    }

    @Override
    public void writeTo(ByteBuffer buffer)
    {
        //To change body of implemented methods use File | Settings | File Templates.
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

    private long id = SpatialObjectIdGenerator.newId();
    private long x;
    private long y;
}
