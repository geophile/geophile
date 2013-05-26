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
 * A 2-dimensional box that can be stored by a {@link com.geophile.z.SpatialIndex}.
 */

public class Box implements SpatialObject
{
    // Object interface

    @Override
    public int hashCode()
    {
        long h =
            (1000000007L * xLo) ^
            (1000000009L * xHi) ^
            (1000000021L * yLo) ^
            (1000000033L * yHi);
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
    public long id()
    {
        return id;
    }

    @Override
    public long[] arbitraryPoint()
    {
        return new long[]{xLo, yLo};
    }

    @Override
    public int maxZ()
    {
        // TODO: Don't rely on system variable
        return Integer.getInteger("maxz", 4);
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
        return
            region.lo(0) <= xLo && xHi <= region.hi(0) &&
            region.lo(1) <= yLo && yHi <= region.hi(1);
    }

    @Override
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

    @Override
    public void readFrom(ByteBuffer buffer)
    {
        id = buffer.getLong();
        xLo = buffer.getLong();
        xHi = buffer.getLong();
        yLo = buffer.getLong();
        yHi = buffer.getLong();
    }

    @Override
    public void writeTo(ByteBuffer buffer)
    {
        buffer.putLong(id);
        buffer.putLong(xLo);
        buffer.putLong(xHi);
        buffer.putLong(yLo);
        buffer.putLong(yHi);
    }

    // Box interface

    /**
     * Indicates whether this Box overlaps a given box.
     * @param that Box to compare to.
     * @return true if this Box overlaps that Box, false otherwise.
     */
    public boolean overlap(Box that)
    {
        return
            this.xLo <= that.xHi && that.xLo <= this.xHi &&
            this.yLo <= that.yHi && that.yLo <= this.yHi;
    }

    /**
     * Returns the left boundary of this box.
     *
     * @return The left boundary of this box.
     */
    public long xLo()
    {
        return xLo;
    }

    /**
     * Returns the right boundary of this box.
     *
     * @return The right boundary of this box.
     */
    public long xHi()
    {
        return xHi;
    }

    /**
     * Returns the lower boundary of this box.
     *
     * @return The lower boundary of this box.
     */
    public long yLo()
    {
        return yLo;
    }

    /**
     * Returns the upper boundary of this box.
     *
     * @return The upper boundary of this box.
     */
    public long yHi()
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
    public Box(long xLo, long xHi, long yLo, long yHi)
    {
        this.xLo = xLo;
        this.xHi = xHi;
        this.yLo = yLo;
        this.yHi = yHi;
    }

    // Object state

    private long id = SpatialObjectIdGenerator.newId();
    private long xLo;
    private long xHi;
    private long yLo;
    private long yHi;
}
