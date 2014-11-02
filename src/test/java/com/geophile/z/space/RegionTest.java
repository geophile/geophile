/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.space;

import com.geophile.z.Space;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class RegionTest
{
    // Regions not at end of space

    @Test
    public void loLT()
    {
        Region lowerLeft = region(24, 40, 4); // (16:32, 32:48)
        assertTrue(!lowerLeft.loLT(0, 8.0));
        assertTrue(!lowerLeft.loLT(0, 15.0));
        assertTrue(!lowerLeft.loLT(0, 16.0));
        assertTrue(lowerLeft.loLT(0, 17.0));
        assertTrue(lowerLeft.loLT(0, 24.0));
        assertTrue(lowerLeft.loLT(0, 32.0));
        assertTrue(lowerLeft.loLT(0, 40.0));
        assertTrue(!lowerLeft.loLT(1, 24.0));
        assertTrue(!lowerLeft.loLT(1, 31.0));
        assertTrue(!lowerLeft.loLT(1, 32.0));
        assertTrue(lowerLeft.loLT(1, 33.0));
        assertTrue(lowerLeft.loLT(1, 40.0));
        assertTrue(lowerLeft.loLT(1, 48.0));
        assertTrue(lowerLeft.loLT(1, 56.0));
    }

    @Test
    public void loLE()
    {
        Region lowerLeft = region(24, 40, 4); // (16:32, 32:48)
        assertTrue(!lowerLeft.loLE(0, 8.0));
        assertTrue(!lowerLeft.loLE(0, 15.0));
        assertTrue(lowerLeft.loLE(0, 16.0));
        assertTrue(lowerLeft.loLE(0, 17.0));
        assertTrue(lowerLeft.loLE(0, 24.0));
        assertTrue(lowerLeft.loLE(0, 32.0));
        assertTrue(lowerLeft.loLE(0, 40.0));
        assertTrue(!lowerLeft.loLE(1, 24.0));
        assertTrue(!lowerLeft.loLE(1, 31.0));
        assertTrue(lowerLeft.loLE(1, 32.0));
        assertTrue(lowerLeft.loLE(1, 33.0));
        assertTrue(lowerLeft.loLE(1, 40.0));
        assertTrue(lowerLeft.loLE(1, 48.0));
        assertTrue(lowerLeft.loLE(1, 56.0));
    }

    @Test
    public void loGT()
    {
        Region lowerLeft = region(24, 40, 4); // (16:32, 32:48)
        assertTrue(lowerLeft.loGT(0, 8.0));
        assertTrue(lowerLeft.loGT(0, 15.0));
        assertTrue(!lowerLeft.loGT(0, 16.0));
        assertTrue(!lowerLeft.loGT(0, 17.0));
        assertTrue(!lowerLeft.loGT(0, 24.0));
        assertTrue(!lowerLeft.loGT(0, 32.0));
        assertTrue(!lowerLeft.loGT(0, 40.0));
        assertTrue(lowerLeft.loGT(1, 24.0));
        assertTrue(lowerLeft.loGT(1, 31.0));
        assertTrue(!lowerLeft.loGT(1, 32.0));
        assertTrue(!lowerLeft.loGT(1, 33.0));
        assertTrue(!lowerLeft.loGT(1, 40.0));
        assertTrue(!lowerLeft.loGT(1, 48.0));
        assertTrue(!lowerLeft.loGT(1, 56.0));
    }

    @Test
    public void loGE()
    {
        Region lowerLeft = region(24, 40, 4); // (16:32, 32:48)
        assertTrue(lowerLeft.loGE(0, 8.0));
        assertTrue(lowerLeft.loGE(0, 15.0));
        assertTrue(lowerLeft.loGE(0, 16.0));
        assertTrue(!lowerLeft.loGE(0, 17.0));
        assertTrue(!lowerLeft.loGE(0, 24.0));
        assertTrue(!lowerLeft.loGE(0, 32.0));
        assertTrue(!lowerLeft.loGE(0, 40.0));
        assertTrue(lowerLeft.loGE(1, 24.0));
        assertTrue(lowerLeft.loGE(1, 31.0));
        assertTrue(lowerLeft.loGE(1, 32.0));
        assertTrue(!lowerLeft.loGE(1, 33.0));
        assertTrue(!lowerLeft.loGE(1, 40.0));
        assertTrue(!lowerLeft.loGE(1, 48.0));
        assertTrue(!lowerLeft.loGE(1, 56.0));
    }

    @Test
    public void hiLT()
    {
        Region lowerLeft = region(24, 40, 4); // (16:32, 32:48)
        assertTrue(!lowerLeft.hiLT(0, 8.0));
        assertTrue(!lowerLeft.hiLT(0, 16.0));
        assertTrue(!lowerLeft.hiLT(0, 24.0));
        assertTrue(!lowerLeft.hiLT(0, 31.0));
        assertTrue(lowerLeft.hiLT(0, 32.0));
        assertTrue(lowerLeft.hiLT(0, 33.0));
        assertTrue(lowerLeft.hiLT(0, 40.0));
        assertTrue(!lowerLeft.hiLT(1, 24.0));
        assertTrue(!lowerLeft.hiLT(1, 32.0));
        assertTrue(!lowerLeft.hiLT(1, 40.0));
        assertTrue(!lowerLeft.hiLT(1, 47.0));
        assertTrue(lowerLeft.hiLT(1, 48.0));
        assertTrue(lowerLeft.hiLT(1, 49.0));
        assertTrue(lowerLeft.hiLT(1, 56.0));
    }

    @Test
    public void hiLE()
    {
        Region lowerLeft = region(24, 40, 4); // (16:32, 32:48)
        assertTrue(!lowerLeft.hiLE(0, 8.0));
        assertTrue(!lowerLeft.hiLE(0, 16.0));
        assertTrue(!lowerLeft.hiLE(0, 24.0));
        assertTrue(!lowerLeft.hiLE(0, 31.0));
        assertTrue(lowerLeft.hiLE(0, 32.0));
        assertTrue(lowerLeft.hiLE(0, 33.0));
        assertTrue(lowerLeft.hiLE(0, 40.0));
        assertTrue(!lowerLeft.hiLE(1, 24.0));
        assertTrue(!lowerLeft.hiLE(1, 32.0));
        assertTrue(!lowerLeft.hiLE(1, 40.0));
        assertTrue(!lowerLeft.hiLE(1, 47.0));
        assertTrue(lowerLeft.hiLE(1, 48.0));
        assertTrue(lowerLeft.hiLE(1, 49.0));
        assertTrue(lowerLeft.hiLE(1, 56.0));
    }

    @Test
    public void hiGT()
    {
        Region lowerLeft = region(24, 40, 4); // (16:32, 32:48)
        assertTrue(lowerLeft.hiGT(0, 8.0));
        assertTrue(lowerLeft.hiGT(0, 16.0));
        assertTrue(lowerLeft.hiGT(0, 24.0));
        assertTrue(lowerLeft.hiGT(0, 31.0));
        assertTrue(!lowerLeft.hiGT(0, 32.0));
        assertTrue(!lowerLeft.hiGT(0, 33.0));
        assertTrue(!lowerLeft.hiGT(0, 40.0));
        assertTrue(lowerLeft.hiGT(1, 24.0));
        assertTrue(lowerLeft.hiGT(1, 32.0));
        assertTrue(lowerLeft.hiGT(1, 40.0));
        assertTrue(lowerLeft.hiGT(1, 47.0));
        assertTrue(!lowerLeft.hiGT(1, 48.0));
        assertTrue(!lowerLeft.hiGT(1, 49.0));
        assertTrue(!lowerLeft.hiGT(1, 56.0));
    }

    @Test
    public void hiGE()
    {
        Region lowerLeft = region(24, 40, 4); // (16:32, 32:48)
        assertTrue(lowerLeft.hiGE(0, 8.0));
        assertTrue(lowerLeft.hiGE(0, 16.0));
        assertTrue(lowerLeft.hiGE(0, 24.0));
        assertTrue(lowerLeft.hiGE(0, 31.0));
        assertTrue(!lowerLeft.hiGE(0, 32.0));
        assertTrue(!lowerLeft.hiGE(0, 33.0));
        assertTrue(!lowerLeft.hiGE(0, 40.0));
        assertTrue(lowerLeft.hiGE(1, 24.0));
        assertTrue(lowerLeft.hiGE(1, 32.0));
        assertTrue(lowerLeft.hiGE(1, 40.0));
        assertTrue(lowerLeft.hiGE(1, 47.0));
        assertTrue(!lowerLeft.hiGE(1, 48.0));
        assertTrue(!lowerLeft.hiGE(1, 49.0));
        assertTrue(!lowerLeft.hiGE(1, 56.0));
    }

    // Regions at end of space

    @Test
    public void loLTAtEnd()
    {
        Region lowerLeft = region(52, 52, 4); // (48:64, 48:64)
        assertTrue(!lowerLeft.loLT(0, 32.0));
        assertTrue(!lowerLeft.loLT(0, 47.0));
        assertTrue(!lowerLeft.loLT(0, 48.0));
        assertTrue(lowerLeft.loLT(0, 49.0));
        assertTrue(lowerLeft.loLT(0, 63.0));
        assertTrue(lowerLeft.loLT(0, 64.0));
        assertTrue(!lowerLeft.loLT(1, 32.0));
        assertTrue(!lowerLeft.loLT(1, 47.0));
        assertTrue(!lowerLeft.loLT(1, 48.0));
        assertTrue(lowerLeft.loLT(1, 49.0));
        assertTrue(lowerLeft.loLT(1, 63.0));
        assertTrue(lowerLeft.loLT(1, 64.0));
    }

    @Test
    public void loLEAtEnd()
    {
        Region lowerLeft = region(52, 52, 4); // (48:64, 48:64)
        assertTrue(!lowerLeft.loLE(0, 32.0));
        assertTrue(!lowerLeft.loLE(0, 47.0));
        assertTrue(lowerLeft.loLE(0, 48.0));
        assertTrue(lowerLeft.loLE(0, 49.0));
        assertTrue(lowerLeft.loLE(0, 63.0));
        assertTrue(lowerLeft.loLE(0, 64.0));
        assertTrue(!lowerLeft.loLE(1, 32.0));
        assertTrue(!lowerLeft.loLE(1, 47.0));
        assertTrue(lowerLeft.loLE(1, 48.0));
        assertTrue(lowerLeft.loLE(1, 49.0));
        assertTrue(lowerLeft.loLE(1, 63.0));
        assertTrue(lowerLeft.loLE(1, 64.0));
    }

    @Test
    public void loGTAtEnd()
    {
        Region lowerLeft = region(52, 52, 4); // (48:64, 48:64)
        assertTrue(lowerLeft.loGT(0, 32.0));
        assertTrue(lowerLeft.loGT(0, 47.0));
        assertTrue(!lowerLeft.loGT(0, 48.0));
        assertTrue(!lowerLeft.loGT(0, 49.0));
        assertTrue(!lowerLeft.loGT(0, 63.0));
        assertTrue(!lowerLeft.loGT(0, 64.0));
        assertTrue(lowerLeft.loGT(1, 32.0));
        assertTrue(lowerLeft.loGT(1, 47.0));
        assertTrue(!lowerLeft.loGT(1, 48.0));
        assertTrue(!lowerLeft.loGT(1, 49.0));
        assertTrue(!lowerLeft.loGT(1, 63.0));
        assertTrue(!lowerLeft.loGT(1, 64.0));
    }

    @Test
    public void loGEAtEnd()
    {
        Region lowerLeft = region(52, 52, 4); // (48:64, 48:64)
        assertTrue(lowerLeft.loGE(0, 32.0));
        assertTrue(lowerLeft.loGE(0, 47.0));
        assertTrue(lowerLeft.loGE(0, 48.0));
        assertTrue(!lowerLeft.loGE(0, 49.0));
        assertTrue(!lowerLeft.loGE(0, 63.0));
        assertTrue(!lowerLeft.loGE(0, 64.0));
        assertTrue(lowerLeft.loGE(1, 32.0));
        assertTrue(lowerLeft.loGE(1, 47.0));
        assertTrue(lowerLeft.loGE(1, 48.0));
        assertTrue(!lowerLeft.loGE(1, 49.0));
        assertTrue(!lowerLeft.loGE(1, 63.0));
        assertTrue(!lowerLeft.loGE(1, 64.0));
    }

    @Test
    public void hiLTAtEnd()
    {
        Region lowerLeft = region(52, 52, 4); // (48:64, 48:64)
        assertTrue(!lowerLeft.hiLT(0, 32.0));
        assertTrue(!lowerLeft.hiLT(0, 48.0));
        assertTrue(!lowerLeft.hiLT(0, 63.0));
        assertTrue(!lowerLeft.hiLT(0, 64.0));
        assertTrue(!lowerLeft.hiLT(1, 32.0));
        assertTrue(!lowerLeft.hiLT(1, 48.0));
        assertTrue(!lowerLeft.hiLT(1, 63.0));
        assertTrue(!lowerLeft.hiLT(1, 64.0));
    }

    @Test
    public void hiLEAtEnd()
    {
        Region lowerLeft = region(52, 52, 4); // (48:64, 48:64)
        assertTrue(!lowerLeft.hiLE(0, 32.0));
        assertTrue(!lowerLeft.hiLE(0, 48.0));
        assertTrue(!lowerLeft.hiLE(0, 63.0));
        assertTrue(lowerLeft.hiLE(0, 64.0));
        assertTrue(!lowerLeft.hiLE(1, 32.0));
        assertTrue(!lowerLeft.hiLE(1, 48.0));
        assertTrue(!lowerLeft.hiLE(1, 63.0));
        assertTrue(lowerLeft.hiLE(1, 64.0));
    }

    @Test
    public void hiGTAtEnd()
    {
        Region lowerLeft = region(52, 52, 4); // (48:64, 48:64)
        assertTrue(lowerLeft.hiGT(0, 32.0));
        assertTrue(lowerLeft.hiGT(0, 48.0));
        assertTrue(lowerLeft.hiGT(0, 63.0));
        assertTrue(!lowerLeft.hiGT(0, 64.0));
        assertTrue(lowerLeft.hiGT(1, 32.0));
        assertTrue(lowerLeft.hiGT(1, 48.0));
        assertTrue(lowerLeft.hiGT(1, 63.0));
        assertTrue(!lowerLeft.hiGT(1, 64.0));
    }

    @Test
    public void hiGEAtEnd()
    {
        Region lowerLeft = region(52, 52, 4); // (48:64, 48:64)
        assertTrue(lowerLeft.hiGE(0, 32.0));
        assertTrue(lowerLeft.hiGE(0, 48.0));
        assertTrue(lowerLeft.hiGE(0, 63.0));
        assertTrue(lowerLeft.hiGE(0, 64.0));
        assertTrue(lowerLeft.hiGE(1, 32.0));
        assertTrue(lowerLeft.hiGE(1, 48.0));
        assertTrue(lowerLeft.hiGE(1, 63.0));
        assertTrue(lowerLeft.hiGE(1, 64.0));
    }

    private static Region region(double x, double y, int level)
    {
        Region region = new Region(SPACE, doubles(x, y));
        while (region.level() > level) {
            region.up();
        }
        return region;
    }

    private static double[] doubles(double... x)
    {
        return x;
    }

    private static int[] ints(int... x)
    {
        return x;
    }

    private static final SpaceImpl SPACE = (SpaceImpl) Space.newSpace(doubles(0, 0),
                                                                      doubles(64, 64),
                                                                      ints(6, 6));
}