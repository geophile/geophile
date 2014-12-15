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
 * A 2-dimensional box that can be stored by a {@link com.geophile.z.SpatialIndex}.
 */

public class Box implements SpatialObject
{
    // Object interface

    @Override
    public int hashCode()
    {
        long h =
            (1000000007L * Double.doubleToLongBits(xLo)) ^
            (1000000009L * Double.doubleToLongBits(xHi)) ^
            (1000000021L * Double.doubleToLongBits(yLo)) ^
            (1000000033L * Double.doubleToLongBits(yHi));
        return ((int) (h >>> 32)) ^ (int) h;
    }

    @Override
    public boolean equals(Object o)
    {
        boolean eq = false;
        if (o != null && o instanceof Box) {
            Box that = (Box) o;
            eq =
                this.xLo == that.xLo &&
                this.xHi == that.xHi &&
                this.yLo == that.yLo &&
                this.yHi == that.yHi;
        }
        return eq;
    }

    @Override
    public String toString()
    {
        return String.format("(%s:%s, %s:%s)", xLo, xHi, yLo, yHi);
    }

    // SpatialObject interface

    @Override
    public double[] arbitraryPoint()
    {
        return new double[]{xLo, yLo};
    }

    @Override
    public int maxZ()
    {
        return MAX_Z;
    }

    @Override
    public boolean containedBy(Space space)
    {
        return
            space.dimensions() == 2 &&
            space.lo(0) <= xLo && xHi <= space.hi(0) &&
            space.lo(1) <= yLo && yHi <= space.hi(1);
    }

    @Override
    public boolean containedBy(Region region)
    {
        return
            region.loLE(0, xLo) && region.hiGE(0, xHi) &&
            region.loLE(1, yLo) && region.hiGE(1, yHi);
    }

    @Override
    public RegionComparison compare(Region region)
    {
        if (region.loGE(0, xLo) && region.hiLT(0, xHi) &&
            region.loGE(1, yLo) && region.hiLT(1, yHi)) {
            return RegionComparison.REGION_INSIDE_OBJECT;
        } else if (region.hiLT(0, xLo) || region.loGT(0, xHi) ||
                   region.hiLT(1, yLo) || region.loGT(1, yHi)) {
            return RegionComparison.REGION_OUTSIDE_OBJECT;
        } else {
            return RegionComparison.REGION_OVERLAPS_OBJECT;
        }
    }

    @Override
    public void readFrom(ByteBuffer buffer)
    {
        xLo = buffer.getDouble();
        xHi = buffer.getDouble();
        yLo = buffer.getDouble();
        yHi = buffer.getDouble();
    }

    @Override
    public void writeTo(ByteBuffer buffer)
    {
        buffer.putDouble(xLo);
        buffer.putDouble(xHi);
        buffer.putDouble(yLo);
        buffer.putDouble(yHi);
    }

    // Box interface

    /**
     * Returns the left boundary of this box.
     *
     * @return The left boundary of this box.
     */
    public double xLo()
    {
        return xLo;
    }

    /**
     * Returns the right boundary of this box.
     *
     * @return The right boundary of this box.
     */
    public double xHi()
    {
        return xHi;
    }

    /**
     * Returns the lower boundary of this box.
     *
     * @return The lower boundary of this box.
     */
    public double yLo()
    {
        return yLo;
    }

    /**
     * Returns the upper boundary of this box.
     *
     * @return The upper boundary of this box.
     */
    public double yHi()
    {
        return yHi;
    }

    /**
     * Creates a box containing points (x, y) such that xLo &lt;= x &lt;= xHi, and
     * yLo &lt;= y &lt;= yHi.
     *
     * @param xLo The left boundary of the box.
     * @param xHi The right boundary of the box.
     * @param yLo The lower boundary of the box.
     * @param yHi The upper boundary of the box.
     */
    public Box(double xLo, double xHi, double yLo, double yHi)
    {
        if (xLo > xHi || yLo > yHi) {
            throw new IllegalArgumentException();
        }
        this.xLo = xLo;
        this.xHi = xHi;
        this.yLo = yLo;
        this.yHi = yHi;
    }

    public Box()
    {}

    // Class state

    private static final int MAX_Z = 8;

    // Object state

    private double xLo;
    private double xHi;
    private double yLo;
    private double yHi;
}
