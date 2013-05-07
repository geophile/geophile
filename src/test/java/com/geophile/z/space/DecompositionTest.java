/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.space;

import com.geophile.z.spatialobject.d2.Box;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DecompositionTest
{
    @Test
    public void testAll()
    {
        Box box = new Box(0, 1023, 0, 1023);
        long[] zs = new long[4];
        space.decompose(box, zs);
        assertEquals(space.z(0x0000000000000000L, 0), zs[0]);
        assertEquals(-1L, zs[1]);
        assertEquals(-1L, zs[2]);
        assertEquals(-1L, zs[3]);
    }

    @Test
    public void testLeft()
    {
        Box box = new Box(0, 511, 0, 1023);
        long[] zs = new long[4];
        space.decompose(box, zs);
        assertEquals(space.z(0x0000000000000000L, 1), zs[0]);
        assertEquals(-1L, zs[1]);
        assertEquals(-1L, zs[2]);
        assertEquals(-1L, zs[3]);
    }

    @Test
    public void testRight()
    {
        Box box = new Box(512, 1023, 0, 1023);
        long[] zs = new long[4];
        space.decompose(box, zs);
        assertEquals(space.z(0x8000000000000000L, 1), zs[0]);
        assertEquals(-1L, zs[1]);
        assertEquals(-1L, zs[2]);
        assertEquals(-1L, zs[3]);
    }

    @Test
    public void testBottom()
    {
        Box box = new Box(0, 1023, 0, 511);
        long[] zs = new long[4];
        space.decompose(box, zs);
        assertEquals(space.z(0x0000000000000000L, 2), zs[0]);
        assertEquals(space.z(0x8000000000000000L, 2), zs[1]);
        assertEquals(-1L, zs[2]);
        assertEquals(-1L, zs[3]);
    }

    @Test
    public void testTop()
    {
        Box box = new Box(0, 1023, 512, 1023);
        long[] zs = new long[4];
        space.decompose(box, zs);
        assertEquals(space.z(0x4000000000000000L, 2), zs[0]);
        assertEquals(space.z(0xc000000000000000L, 2), zs[1]);
        assertEquals(-1L, zs[2]);
        assertEquals(-1L, zs[3]);
    }

    @Test
    public void testTinyBoxInMiddle()
    {
        Box box = new Box(511, 512, 511, 512);
        long[] zs = new long[4];
        space.decompose(box, zs);
        assertEquals(space.z(0x3ffff00000000000L, 20), zs[0]);
        assertEquals(space.z(0x6aaaa00000000000L, 20), zs[1]);
        assertEquals(space.z(0x9555500000000000L, 20), zs[2]);
        assertEquals(space.z(0xc000000000000000L, 20), zs[3]);
    }

    private static int[] ints(int... ints)
    {
        return ints;
    }

    private final SpaceImpl space = new SpaceImpl(ints(10, 10), null);
}
