/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.space;

import com.geophile.z.Space;

import java.util.Arrays;

/**
 * A {@link Region} represents a box-shaped subspace obtained by recursive partitioning of the space.
 * Regions are only of interest to users of geophile who define {@link com.geophile.z.SpatialObject} subtypes.
 *
 * Region coordinates are defined in terms of Geophile's grid, (see {@link com.geophile.z.space.SpaceImpl} for
 * a discussion of the grid.) Region boundaries must be handled very carefully, (by definers of
 * {@link com.geophile.z.SpatialObject} subtypes to ensure that spatial object decompositions are correct.
 *
 * Suppose we have a grid of size 64 x 64, (which is far too coarse in practice), and a Region occuping the
 * lower-left quadrant, 0-32 x 0-32. The Region contains all points (x, y) such that 0 &lt;= x &lt; 16 and
 * 0 &lt;= y &lt; 16.
 * I.e., the top and and right boundaries do <i>not</i> belong to the region. However, these excluded boundaries
 * <i>do</i> belong to Regions at the top and right edges of the space. So, for example, the upper right quadrant,
 * 32-64 x 32-64 contains the points 32 &lt;= x &lt;= 64 and 32 &lt;= y &lt;= 64. The reason for this exception,
 * along the top and right edges of the space, is that we must deal with spatial objects that touch these edges,
 * and these objects must be completely covered by Regions.
 *
 * To ensure that this logic is implemented correctly, the Region API does not expose the Region's boundaries directly.
 * Instead, there are methods for comparing the boundaries to given coordinates. For example, hiGT(int d, double coord)
 * indicates whether the high bound of the Region, in dimension d, is greater than coord. The implementation depends
 * on whether the region is at the upper edge of the space in dimension d.
 */

public class Region
{
    // Object interface

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append('(');
        buffer.append(level);
        buffer.append(": ");
        for (int d = 0; d < space.dimensions; d++) {
            if (d > 0) {
                buffer.append(", ");
            }
            buffer.append(lo(d));
            buffer.append(" : ");
            buffer.append(hi(d));
        }
        buffer.append(')');
        return buffer.toString();
    }

    // Region interface

    /**
     * Returns this region's {@link com.geophile.z.Space}.
     * @return This region's {@link com.geophile.z.Space}.
     */
    public Space space()
    {
        return space;
    }

    /**
     * Indicates whether the low bound of the region in dimension d is less than the given coord.
     * @param d The dimension of interest.
     * @param coord The coordinate being compared.
     * @return true iff the low bound of the region in dimension d is less than the given coord.
     */
    public boolean loLT(int d, double coord)
    {
        return lo(d) < coord;
    }

    /**
     * Indicates whether the low bound of the region in dimension d is less than or equal to the given coord.
     * @param d The dimension of interest.
     * @param coord The coordinate being compared.
     * @return true iff the low bound of the region in dimension d is less than or equal to the given coord.
     */
    public boolean loLE(int d, double coord)
    {
        return lo(d) <= coord;
    }

    /**
     * Indicates whether the low bound of the region in dimension d is greater than the given coord.
     * @param d The dimension of interest.
     * @param coord The coordinate being compared.
     * @return true iff the low bound of the region in dimension d is greater than the given coord.
     */
    public boolean loGT(int d, double coord)
    {
        return lo(d) > coord;
    }

    /**
     * Indicates whether the low bound of the region in dimension d is greater than or equal to the given coord.
     * @param d The dimension of interest.
     * @param coord The coordinate being compared.
     * @return true iff the low bound of the region in dimension d is greater than or equal to the given coord.
     */
    public boolean loGE(int d, double coord)
    {
        return lo(d) >= coord;
    }

    /**
     * Indicates whether the high bound of the region in dimension d is less than the given coord.
     * @param d The dimension of interest.
     * @param coord The coordinate being compared.
     * @return true iff the high bound of the region in dimension d is less than the given coord.
     */
    public boolean hiLT(int d, double coord)
    {
        // If the numeric value of hi(d) = coord, then hi(d) is considered to be less than the coord.
        // Except if the region is bounded by the high boundary of the space in dimension d, (indicated
        // by hiCell[d] == space.gHi[d]). In this case, hi(d) cannot be less than any coordinate,
        // because hi(d) = space.hi(d).
        return
            hiCell[d] == space.gHi[d]
            ? false
            : hi(d) <= coord;
    }

    /**
     * Indicates whether the high bound of the region in dimension d is less than or equal to the given coord.
     * @param d The dimension of interest.
     * @param coord The coordinate being compared.
     * @return true iff the high bound of the region in dimension d is less than or equal to the given coord.
     */
    public boolean hiLE(int d, double coord)
    {
        // This is deceptively simple. If the region is bounded by the high boundary of the space in dimension d,
        // then hi(d) <= coord is correct. But hi(d) < coord cannot actually occur, because hi(d) = space.hi(d),
        // and it must be true that coord <= space.hi(d). Otherwise <= is the correct comparison.
        return hi(d) <= coord;
    }

    /**
     * Indicates whether the high bound of the region in dimension d is greater than the given coord.
     * @param d The dimension of interest.
     * @param coord The coordinate being compared.
     * @return true iff the high bound of the region in dimension d is greater than the given coord.
     */
    public boolean hiGT(int d, double coord)
    {
        // Just the inverse of hiLE.
        return hi(d) > coord;
    }

    /**
     * Indicates whether the high bound of the region in dimension d is greater than or equal to the given coord.
     * @param d The dimension of interest.
     * @param coord The coordinate being compared.
     * @return true iff the high bound of the region in dimension d is greater than or equal to the given coord.
     */
    public boolean hiGE(int d, double coord)
    {
        // Just the inverse of hiLT
        return
            hiCell[d] == space.gHi[d]
            ? true
            : hi(d) > coord;
    }
    
    /**
     * Returns the level of this Region.
     * @return the level of this Region.
     */
    public int level()
    {
        return level;
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
        int d = interleave[level++];
        hiCell[d] &= ~(1L << --xBitPosition[d]);
    }

    void downRight()
    {
        int d = interleave[level++];
        loCell[d] |= 1L << --xBitPosition[d];
    }

    void up()
    {
        int d = interleave[--level];
        long mask = 1L << xBitPosition[d]++;
        loCell[d] &= ~mask;
        hiCell[d] |= mask;
    }

    long z()
    {
        return space.shuffle(loCell, level);
    }

    Region copy()
    {
        return new Region(this);
    }

    Region(SpaceImpl space, double[] point)
    {
        assert point.length == space.dimensions;
        this.space = space;
        this.interleave = space.interleave;
        this.appToGridScale = space.appToGridScale;
        this.appLo = space.appLo;
        this.loCell = new long[space.dimensions];
        this.hiCell = new long[space.dimensions];
        for (int d = 0; d < space.dimensions; d++) {
            this.loCell[d] = space.cellCoord(d, point[d]);
            this.hiCell[d] = this.loCell[d];
        }
        this.level = space.zBits;
        this.xBitPosition = new int[space.dimensions];
        for (int zBitPosition = space.zBits - 1; zBitPosition >= level; zBitPosition--) {
            int d = interleave[zBitPosition];
            xBitPosition[d]++;
        }
    }

    // For use by this class

    private double lo(int d)
    {
        return loCell[d] / appToGridScale[d] + appLo[d];
    }

    private double hi(int d)
    {
        return (hiCell[d] + 1) / appToGridScale[d] + appLo[d];
    }

    private Region(Region region)
    {
        this.space = region.space;
        this.interleave = region.interleave;
        this.appToGridScale = region.appToGridScale;
        this.appLo = region.appLo;
        this.loCell = Arrays.copyOf(region.loCell, region.loCell.length);
        this.hiCell = Arrays.copyOf(region.hiCell, region.hiCell.length);
        this.level = region.level;
        this.xBitPosition = Arrays.copyOf(region.xBitPosition, region.xBitPosition.length);
    }

    // Object state

    private final SpaceImpl space;
    private final int[] interleave;
    // loCell and hiCell are cell numbers. E.g. if the grid is 16 x 16, then cell numbers go from 0 to 15, inclusive.
    // Note that coordinates, as returned by lo() and hi() are different. Those are coordinates, and they
    // would go from 0 to 16 inclusive. In other words, loCell and hiCell number the cells of the grid,
    // while lo() and hi() are positions of grid lines.
    private final long[] loCell;
    private final long[] hiCell;
    private int level;
    private int[] xBitPosition;
    private double[] appToGridScale;
    private double[] appLo;
}
