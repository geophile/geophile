/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialobject;

import com.geophile.z.Space;
import com.geophile.z.SpatialObject;
import com.geophile.z.SpatialObjectSerializer;
import com.geophile.z.spatialobject.d2.Box;
import com.geophile.z.spatialobject.d2.Point;
import com.geophile.z.spatialobject.jts.JTS;
import com.geophile.z.spatialobject.jts.JTSPoint;
import com.geophile.z.spatialobject.jts.JTSSpatialObjectWithBoundingBox;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SerializationTest
{
    @BeforeClass
    public static void beforeClass()
    {
        SERIALIZER.register(1, Point.class);
        SERIALIZER.register(2, Box.class);
        SERIALIZER.register(3, JTSPoint.class);
        SERIALIZER.register(4, JTSSpatialObjectWithBoundingBox.class);
    }

    @Test
    public void bufferHasToGrow()
    {
        Point point = new Point(123, 456);
        ByteBuffer buffer = ByteBuffer.allocate(1);
        int growCount = 0;
        boolean ok = false;
        while (!ok) {
            try {
                test(point, buffer);
                ok = true;
            } catch (BufferOverflowException e) {
                buffer = ByteBuffer.allocate(buffer.capacity() * 2);
                growCount++;
            }
        }
        assertTrue(growCount > 0);
    }

    @Test
    public void testPoint()
    {
        test(new Point(123, 456));
    }

    @Test
    public void testBox()
    {
        test(new Box(123, 456, 78, 90));
    }

    @Test
    public void testJTSPoint()
    {
        test(JTS.spatialObject(SPACE, FACTORY.createPoint(new Coordinate(123.4, 567.8))));
    }

    @Test
    public void testJTSLineString()
    {
        Coordinate[] coords = new Coordinate[3];
        coords[0] = new Coordinate(123, 456);
        coords[1] = new Coordinate(789, 12);
        coords[2] = new Coordinate(345, 678);
        test(JTS.spatialObject(SPACE, FACTORY.createLineString(coords)));
    }

    @Test
    public void testJTSPolygon()
    {
        Coordinate[] coords = new Coordinate[4];
        coords[0] = new Coordinate(123, 456);
        coords[1] = new Coordinate(789, 12);
        coords[2] = new Coordinate(345, 678);
        coords[3] = coords[0];
        test(JTS.spatialObject(SPACE, FACTORY.createPolygon(FACTORY.createLinearRing(coords), null)));
    }

    @Test
    public void testJTSCollection()
    {
        Coordinate[] coords = new Coordinate[3];
        coords[0] = new Coordinate(123, 456);
        coords[1] = new Coordinate(789, 12);
        coords[2] = new Coordinate(345, 678);
        test(JTS.spatialObject(SPACE, FACTORY.createMultiPoint(coords)));
    }

    private void test(SpatialObject original)
    {
        test(original, buffer);
    }

    private void test(SpatialObject original, ByteBuffer buffer)
    {
        SERIALIZER.serialize(original, buffer);
        buffer.flip();
        SpatialObject reconstituted = SERIALIZER.deserialize(buffer);
        assertSame(original.getClass(), reconstituted.getClass());
        assertTrue(reconstituted.equals(original));
    }

    private static final SpatialObjectSerializer SERIALIZER = SpatialObjectSerializer.newSerializer();
    private static final Space SPACE = Space.newSpace(new double[]{0, 0},
                                                      new double[]{1_000_000, 1_000_000},
                                                      new int[]{20, 20});
    private static final GeometryFactory FACTORY = new GeometryFactory();

    private final ByteBuffer buffer = ByteBuffer.allocate(1000);
}
