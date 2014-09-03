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
                eq = this.x == that.x && this.y == that.y;
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
    public boolean containedBy(Space space)
    {
        return
            space.dimensions() == 2 &&
            space.lo(0) <= x && x <= space.hi(0) &&
            space.lo(1) <= y && y <= space.hi(1);
    }

    @Override
    public boolean containedBy(Region region)
    {
        return
            region.loLE(0, x) && region.hiGE(0, x) &&
            region.loLE(1, y) && region.hiGE(1, y);
    }

    @Override
    public RegionComparison compare(Region region)
    {
        return
            containedBy(region)
            ? RegionComparison.REGION_OVERLAPS_OBJECT
            : RegionComparison.REGION_OUTSIDE_OBJECT;
    }

    @Override
    public void readFrom(ByteBuffer buffer)
    {
        x = buffer.getDouble();
        y = buffer.getDouble();
    }

    @Override
    public void writeTo(ByteBuffer buffer)
    {
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

    private double x;
    private double y;
}
