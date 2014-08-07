/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialobject.d2;

import com.geophile.z.Space;
import com.geophile.z.SpatialObject;
import com.geophile.z.space.Region;
import com.geophile.z.space.RegionComparison;
import com.geophile.z.space.SpaceImpl;

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
            (1000000087L * Double.doubleToLongBits(x)) ^
            (1000000093L * Double.doubleToLongBits(y));
        return ((int) (h >>> 32)) ^ (int) h;
    }

        @Override
        public boolean equals(Object o)
        {
            boolean eq = false;
            if (o != null && o instanceof Point) {
                Point that = (Point) o;
                eq = this.id == that.id && this.equalTo(that);
            }
            return eq;
        }

    @Override
    public String toString()
    {
        return String.format("(%s, %s)", x, y);
    }

    // SpatialObject interface

    @Override
    public void id(long id)
    {
        this.id = id;
    }

    @Override
    public long id()
    {
        return id;
    }

    @Override
    public double[] arbitraryPoint()
    {
        return new double[]{x, y};
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
        SpaceImpl space = (SpaceImpl) region.space();
        long zx = space.appToZ(0, x);
        long zy = space.appToZ(1, y);
        return
            region.lo(0) <= zx && zx <= region.hi(0) &&
            region.lo(1) <= zy && zy <= region.hi(1);
    }

    @Override
    public boolean containedBy(Space space)
    {
        return
            space.lo(0) <= x && x <= space.hi(0) &&
            space.lo(1) <= y && y <= space.hi(1);
    }

    @Override
    public RegionComparison compare(Region region)
    {
        long rXLo = region.lo(0);
        long rYLo = region.lo(1);
        long rXHi = region.hi(0);
        long rYHi = region.hi(1);
        SpaceImpl space = (SpaceImpl) region.space();
        long zx = space.appToZ(0, x);
        long zy = space.appToZ(1, y);
        if (region.isPoint() && rXLo == zx && rYLo == zy) {
            return RegionComparison.REGION_INSIDE_OBJECT;
        } else if (rXHi < zx || rXLo > zx || rYHi < zy || rYLo > zy) {
            return RegionComparison.REGION_OUTSIDE_OBJECT;
        } else {
            return RegionComparison.REGION_OVERLAPS_OBJECT;
        }
    }

    @Override
    public void readFrom(ByteBuffer buffer)
    {
        id = buffer.getLong();
        x = buffer.getDouble();
        y = buffer.getDouble();
    }

    @Override
    public void writeTo(ByteBuffer buffer)
    {
        buffer.putLong(id);
        buffer.putDouble(x);
        buffer.putDouble(y);
    }

    // Point interface

    /**
     * Returns the point's x coordinate.
     * @return The point's x coordinate.
     */
    public double x()
    {
        return x;
    }

    /**
     * Returns the point's y coordinate.
     * @return The point's y coordinate.
     */
    public double y()
    {
        return y;
    }

    /**
     * Creates a point at (x, y).
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    public Point(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public Point()
    {}

    // Object state

    private long id;
    private double x;
    private double y;
}
