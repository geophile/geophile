/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.space;

import com.geophile.z.Space;
import com.geophile.z.SpatialObjectException;
import com.geophile.z.spatialobject.d2.Box;
import com.geophile.z.spatialobject.d2.Point;
import com.geophile.z.spatialobject.jts.JTS;
import com.geophile.z.spatialobject.jts.JTSSpatialObject;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DecompositionTest
{
    @Test
    public void testPointsAtMajorIntersections()
     {
         testPoint(   0,    0, 0x0000000000000000L);
         testPoint(   0,  256, 0x1000000000000000L);
         testPoint(   0,  512, 0x4000000000000000L);
         testPoint(   0,  768, 0x5000000000000000L);
         testPoint(   0, 1024, 0x5555500000000000L);

         testPoint( 256,    0, 0x2000000000000000L);
         testPoint( 256,  256, 0x3000000000000000L);
         testPoint( 256,  512, 0x6000000000000000L);
         testPoint( 256,  768, 0x7000000000000000L);
         testPoint( 256, 1024, 0x7555500000000000L);

         testPoint( 512,    0, 0x8000000000000000L);
         testPoint( 512,  256, 0x9000000000000000L);
         testPoint( 512,  512, 0xc000000000000000L);
         testPoint( 512,  768, 0xd000000000000000L);
         testPoint( 512, 1024, 0xd555500000000000L);

         testPoint( 768,    0, 0xa000000000000000L);
         testPoint( 768,  256, 0xb000000000000000L);
         testPoint( 768,  512, 0xe000000000000000L);
         testPoint( 768,  768, 0xf000000000000000L);
         testPoint( 768, 1024, 0xf555500000000000L);

         testPoint(1024,    0, 0xaaaaa00000000000L);
         testPoint(1024,  256, 0xbaaaa00000000000L);
         testPoint(1024,  512, 0xeaaaa00000000000L);
         testPoint(1024,  768, 0xfaaaa00000000000L);
         testPoint(1024, 1024, 0xfffff00000000000L);
     }

    @Test
    public void testJTSPointsAtMajorIntersections()
    {
        testJTSPoint(   0,    0, 0x0000000000000000L);
        testJTSPoint(   0,  256, 0x1000000000000000L);
        testJTSPoint(   0,  512, 0x4000000000000000L);
        testJTSPoint(   0,  768, 0x5000000000000000L);
        testJTSPoint(   0, 1024, 0x5555500000000000L);

        testJTSPoint( 256,    0, 0x2000000000000000L);
        testJTSPoint( 256,  256, 0x3000000000000000L);
        testJTSPoint( 256,  512, 0x6000000000000000L);
        testJTSPoint( 256,  768, 0x7000000000000000L);
        testJTSPoint( 256, 1024, 0x7555500000000000L);

        testJTSPoint( 512,    0, 0x8000000000000000L);
        testJTSPoint( 512,  256, 0x9000000000000000L);
        testJTSPoint( 512,  512, 0xc000000000000000L);
        testJTSPoint( 512,  768, 0xd000000000000000L);
        testJTSPoint( 512, 1024, 0xd555500000000000L);

        testJTSPoint( 768,    0, 0xa000000000000000L);
        testJTSPoint( 768,  256, 0xb000000000000000L);
        testJTSPoint( 768,  512, 0xe000000000000000L);
        testJTSPoint( 768,  768, 0xf000000000000000L);
        testJTSPoint( 768, 1024, 0xf555500000000000L);

        testJTSPoint(1024,    0, 0xaaaaa00000000000L);
        testJTSPoint(1024,  256, 0xbaaaa00000000000L);
        testJTSPoint(1024,  512, 0xeaaaa00000000000L);
        testJTSPoint(1024,  768, 0xfaaaa00000000000L);
        testJTSPoint(1024, 1024, 0xfffff00000000000L);
    }

     @Test
    public void testAll()
    {
        Box box = new Box(0, 1024, 0, 1024);
        long[] zs = new long[4];
        SPACE.decompose(box, zs);
        assertEquals(SpaceImpl.z(0x0000000000000000L, 0), zs[0]);
        assertEquals(-1L, zs[1]);
        assertEquals(-1L, zs[2]);
        assertEquals(-1L, zs[3]);
    }

    @Test
    public void testLeft()
    {
        Box box = new Box(0, 512 - EPSILON, 0, 1024);
        long[] zs = new long[4];
        SPACE.decompose(box, zs);
        assertEquals(SpaceImpl.z(0x0000000000000000L, 1), zs[0]);
        assertEquals(-1L, zs[1]);
        assertEquals(-1L, zs[2]);
        assertEquals(-1L, zs[3]);
    }

    @Test
    public void testRight()
    {
        Box box = new Box(512 + EPSILON, 1024, 0, 1024);
        long[] zs = new long[4];
        SPACE.decompose(box, zs);
        assertEquals(SpaceImpl.z(0x8000000000000000L, 1), zs[0]);
        assertEquals(-1L, zs[1]);
        assertEquals(-1L, zs[2]);
        assertEquals(-1L, zs[3]);
    }

    @Test
    public void testBottom()
    {
        Box box = new Box(0, 1024, 0, 512 - EPSILON);
        long[] zs = new long[4];
        SPACE.decompose(box, zs);
        assertEquals(SpaceImpl.z(0x0000000000000000L, 2), zs[0]);
        assertEquals(SpaceImpl.z(0x8000000000000000L, 2), zs[1]);
        assertEquals(-1L, zs[2]);
        assertEquals(-1L, zs[3]);
    }

    @Test
    public void testTop()
    {
        Box box = new Box(0, 1024, 512 + EPSILON, 1024);
        long[] zs = new long[4];
        SPACE.decompose(box, zs);
        assertEquals(SpaceImpl.z(0x4000000000000000L, 2), zs[0]);
        assertEquals(SpaceImpl.z(0xc000000000000000L, 2), zs[1]);
        assertEquals(-1L, zs[2]);
        assertEquals(-1L, zs[3]);
    }

    @Test
    public void testTinyBoxInMiddle()
    {
        Box box = new Box(512 - EPSILON, 512 + EPSILON, 512 - EPSILON, 512 + EPSILON);
        long[] zs = new long[4];
        SPACE.decompose(box, zs);
        assertEquals(SpaceImpl.z(0x3ffff00000000000L, 20), zs[0]);
        assertEquals(SpaceImpl.z(0x6aaaa00000000000L, 20), zs[1]);
        assertEquals(SpaceImpl.z(0x9555500000000000L, 20), zs[2]);
        assertEquals(SpaceImpl.z(0xc000000000000000L, 20), zs[3]);
    }

    // Issue 1
    @Test
    public void testBoxNotContainedInSpace()
    {
        Box box = new Box(0, 1025, 0, 1024);
        long[] zs = new long[4];
        try {
            SPACE.decompose(box, zs);
            fail();
        } catch (SpatialObjectException e) {
            // Expected
        }
    }

    // Tests inspired by Issue 3. Decomposition would produce regions outside the space (for coordinates
    // at the high boundary of the space), and region.up() would then try to go above level 0 and blow up.

    @Test
    public void testPoint()
    {
        final int X_MAX = 100;
        final int Y_MAX = 100;
        Space space = Space.newSpace(new double[]{0, 0},
                                     new double[]{X_MAX, Y_MAX},
                                     new int[]{4, 4});
        for (int x = 0; x <= X_MAX; x++) {
            for (int y = 0; y <= Y_MAX; y++) {
                Point point = new Point(x, y);
                long[] zs = new long[point.maxZ()];
                space.decompose(point, zs);
            }
        }
    }

    @Test
    public void testBox()
    {
        final int X_MAX = 100;
        final int Y_MAX = 100;
        Space space = Space.newSpace(new double[]{0, 0},
                                     new double[]{X_MAX, Y_MAX},
                                     new int[]{4, 4});
        for (int boxWidth = 1; boxWidth <= 5; boxWidth++) {
            for (int x = 0; x <= X_MAX - boxWidth; x++) {
                for (int y = 0; y <= Y_MAX - boxWidth; y++) {
                    Box box = new Box(x, x + boxWidth, y, y + boxWidth);
                    long[] zs = new long[box.maxZ()];
                    space.decompose(box, zs);
                }
            }
        }
    }

    @Test
    public void testJTSPoint()
    {
        final int X_MAX = 100;
        final int Y_MAX = 100;
        Space space = Space.newSpace(new double[]{0, 0},
                                     new double[]{X_MAX, Y_MAX},
                                     new int[]{4, 4});
        for (int x = 0; x <= X_MAX; x++) {
            for (int y = 0; y <= Y_MAX; y++) {
                JTSSpatialObject point = JTS.spatialObject(space, GEOMETRY_FACTORY.createPoint(new Coordinate(x, y)));
                long[] zs = new long[point.maxZ()];
                space.decompose(point, zs);
            }
        }
    }

    @Test
    public void testJTSPolygon()
    {
        final int X_MAX = 100;
        final int Y_MAX = 100;
        Space space = Space.newSpace(new double[]{0, 0},
                                     new double[]{X_MAX, Y_MAX},
                                     new int[]{4, 4});
        Coordinate[] coords = new Coordinate[4];
        for (int width = 1; width <= 5; width++) {
            for (int x = 0; x <= X_MAX - width; x++) {
                for (int y = 0; y <= Y_MAX - width; y++) {
                    // Test a polygon defined by slicing a box in half diagonally. Use the upper half.
                    coords[0] = new Coordinate(x, y + width);
                    coords[1] = new Coordinate(x + width, y + width);
                    coords[2] = new Coordinate(x + width, y);
                    coords[3] = coords[0];
                    LinearRing ring = GEOMETRY_FACTORY.createLinearRing(coords);
                    JTSSpatialObject polygon = JTS.spatialObject(space, GEOMETRY_FACTORY.createPolygon(ring, null));
                    long[] zs = new long[polygon.maxZ()];
                    space.decompose(polygon, zs);
                }
            }
        }
    }

    // End of Issue 3 tests

    @Test
    public void testDecompositionCoversDecomposedObject()
    {
        final int X_MAX = 100;
        final int Y_MAX = 100;
        Space space = Space.newSpace(new double[]{0, 0},
                                     new double[]{X_MAX, Y_MAX},
                                     new int[]{4, 4});
        for (int boxWidth = 1; boxWidth <= 5; boxWidth++) {
            for (int x = 0; x <= X_MAX - boxWidth; x++) {
                for (int y = 0; y <= Y_MAX - boxWidth; y++) {
                    Box box = new Box(x, x + boxWidth, y, y + boxWidth);
                    long[] zs = new long[box.maxZ()];
                    space.decompose(box, zs);
                }
            }
        }
    }

    private void testPoint(int x, int y, long z)
    {
        Point point = new Point(x, y);
        long[] zs = new long[4];
        SPACE.decompose(point, zs);
        assertEquals(SpaceImpl.z(z, 20), zs[0]);
        assertEquals(-1L, zs[1]);
        assertEquals(-1L, zs[2]);
        assertEquals(-1L, zs[3]);
    }

    private void testJTSPoint(int x, int y, long z)
    {
        JTSSpatialObject point = JTS.spatialObject(SPACE, GEOMETRY_FACTORY.createPoint(new Coordinate(x, y)));
        long[] zs = new long[4];
        SPACE.decompose(point, zs);
        assertEquals(SpaceImpl.z(z, 20), zs[0]);
        assertEquals(-1L, zs[1]);
        assertEquals(-1L, zs[2]);
        assertEquals(-1L, zs[3]);
    }

    private static int[] ints(int... ints)
    {
        return ints;
    }

    private static double[] doubles(double ... doubles)
    {
        return doubles;
    }

    private static final double EPSILON = 0.00001;
    private static final SpaceImpl SPACE = new SpaceImpl(doubles(0, 0), doubles(1024, 1024), ints(10, 10), null);
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
}
