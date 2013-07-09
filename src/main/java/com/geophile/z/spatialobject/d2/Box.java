/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialobject.d2;

import com.geophile.z.SpatialObject;
import com.geophile.z.space.Region;
import com.geophile.z.space.RegionComparison;
import com.geophile.z.space.SpaceImpl;

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
        return o != null && o instanceof Box && equalTo((Box) o);
    }

    @Override
    public String toString()
    {
        return String.format("(%s:%s, %s:%s)", xLo, xHi, yLo, yHi);
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
        return new double[]{xLo, yLo};
    }

    @Override
    public int maxZ()
    {
        // TODO: Don't rely on system variable
        return
            xLo == xHi && yLo == yHi
            ? 1
            : Integer.getInteger("maxz", 4);
    }

    @Override
    public boolean equalTo(SpatialObject spatialObject)
    {
        boolean eq = false;
        if (spatialObject != null && spatialObject instanceof Box) {
            Box that = (Box) spatialObject;
            eq =
                this.xLo == that.xLo &&
                this.xHi == that.xHi &&
                this.yLo == that.yLo &&
                this.yHi == that.yHi;
        }
        return eq;
    }

    @Override
    public boolean containedBy(Region region)
    {
        SpaceImpl space = (SpaceImpl) region.space();
        long zxLo = space.appToZ(0, xLo);
        long zxHi = space.appToZ(0, xHi);
        long zyLo = space.appToZ(1, yLo);
        long zyHi = space.appToZ(1, yHi);
        return
            region.lo(0) <= zxLo && zxHi <= region.hi(0) &&
            region.lo(1) <= zyLo && zyHi <= region.hi(1);
    }

    @Override
    public RegionComparison compare(Region region)
    {
        long rXLo = region.lo(0);
        long rYLo = region.lo(1);
        long rXHi = region.hi(0);
        long rYHi = region.hi(1);
        SpaceImpl space = (SpaceImpl) region.space();
        long zxLo = space.appToZ(0, xLo);
        long zxHi = space.appToZ(0, xHi);
        long zyLo = space.appToZ(1, yLo);
        long zyHi = space.appToZ(1, yHi);
        if (zxLo <= rXLo && rXHi <= zxHi && zyLo <= rYLo && rYHi <= zyHi) {
            return RegionComparison.REGION_INSIDE_OBJECT;
        } else if (rXHi < zxLo || rXLo > zxHi || rYHi < zyLo || rYLo > zyHi) {
            return RegionComparison.REGION_OUTSIDE_OBJECT;
        } else {
            return RegionComparison.REGION_OVERLAPS_OBJECT;
        }
    }

    @Override
    public void readFrom(ByteBuffer buffer)
    {
        id = buffer.getLong();
        xLo = buffer.getDouble();
        xHi = buffer.getDouble();
        yLo = buffer.getDouble();
        yHi = buffer.getDouble();
    }

    @Override
    public void writeTo(ByteBuffer buffer)
    {
        buffer.putLong(id);
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
     * Creates a box containing points (x, y) such that xLo <= x <= xHi, and
     * yLo <= y <= yHi.
     *
     * @param xLo The left boundary of the box.
     * @param xHi The right boundary of the box.
     * @param yLo The lower boundary of the box.
     * @param yHi The upper boundary of the box.
     */
    public Box(double xLo, double xHi, double yLo, double yHi)
    {
        this.xLo = xLo;
        this.xHi = xHi;
        this.yLo = yLo;
        this.yHi = yHi;
    }

    public Box()
    {}

    // Object state

    private long id;
    private double xLo;
    private double xHi;
    private double yLo;
    private double yHi;
}
