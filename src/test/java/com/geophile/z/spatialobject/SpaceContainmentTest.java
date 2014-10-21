/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialobject;

import com.geophile.z.Space;
import com.geophile.z.SpatialObject;
import com.geophile.z.spatialobject.d2.Box;
import com.geophile.z.spatialobject.d2.Point;
import com.geophile.z.spatialobject.jts.JTS;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SpaceContainmentTest
{
    @Test
    public void testPoint()
    {
        // Boundary
        checkInside(new Point(XLO, YLO));
        checkInside(new Point(XLO, YHI));
        checkInside(new Point(XHI, YLO));
        checkInside(new Point(XHI, YHI));
        // Interior
        checkInside(new Point(XLO + 1, YLO + 1));
        checkInside(new Point(XLO + 1, YHI - 1));
        checkInside(new Point(XHI - 1, YLO + 1));
        checkInside(new Point(XHI - 1, YHI - 1));
        // Outside
        checkOutside(new Point(XLO - 1, YLO));
        checkOutside(new Point(XLO, YLO - 1));
        checkOutside(new Point(XLO - 1, YHI));
        checkOutside(new Point(XLO, YHI + 1));
        checkOutside(new Point(XHI + 1, YLO));
        checkOutside(new Point(XHI, YLO - 1));
        checkOutside(new Point(XHI + 1, YHI));
        checkOutside(new Point(XHI, YHI + 1));
    }

    @Test
    public void testBox()
    {
        // Boundary
        checkInside(new Box(XLO, XHI, YLO, YHI));
        // Interior
        checkInside(new Box(XLO + 1, XHI - 1, YLO + 1, YHI - 1));
        // Outside
        checkOutside(new Box(XLO - 1, XHI, YLO, YHI));
        checkOutside(new Box(XLO, XHI + 1, YLO, YHI));
        checkOutside(new Box(XLO, XHI, YLO - 1, YHI));
        checkOutside(new Box(XLO, XHI, YLO, YHI + 1));
    }

    @Test
    public void testJTSPoint()
    {
        // Boundary
        checkInside(jtsPoint(XLO, YLO));
        checkInside(jtsPoint(XLO, YHI));
        checkInside(jtsPoint(XHI, YLO));
        checkInside(jtsPoint(XHI, YHI));
        // Interior
        checkInside(jtsPoint(XLO + 1, YLO + 1));
        checkInside(jtsPoint(XLO + 1, YHI - 1));
        checkInside(jtsPoint(XHI - 1, YLO + 1));
        checkInside(jtsPoint(XHI - 1, YHI - 1));
        // Outside
        checkOutside(jtsPoint(XLO - 1, YLO));
        checkOutside(jtsPoint(XLO, YLO - 1));
        checkOutside(jtsPoint(XLO - 1, YHI));
        checkOutside(jtsPoint(XLO, YHI + 1));
        checkOutside(jtsPoint(XHI + 1, YLO));
        checkOutside(jtsPoint(XHI, YLO - 1));
        checkOutside(jtsPoint(XHI + 1, YHI));
        checkOutside(jtsPoint(XHI, YHI + 1));
    }

    @Test
    public void testJTSPolygon()
    {
        // Boundary
        checkInside(jtsPolygon(XLO, YLO,
                               XLO, YHI,
                               XHI, YHI,
                               XHI, YLO,
                               XLO, YLO));
        // Interior
        checkInside(jtsPolygon(XLO + 1, YLO + 1,
                               XLO + 1, YHI - 1,
                               XHI - 1, YHI - 1,
                               XHI - 1, YLO + 1,
                               XLO + 1, YLO + 1));
        // Outside
        checkOutside(jtsPolygon(XLO, YLO - 1,
                                XLO, YHI,
                                XHI, YHI,
                                XHI, YLO,
                                XLO, YLO - 1));
    }

    private SpatialObject jtsPoint(double x, double y)
    {
        return JTS.spatialObject(SPACE, FACTORY.createPoint(new Coordinate(x, y)));
    }

    private SpatialObject jtsPolygon(double ... xy)
    {
        assertTrue(xy.length % 2 == 0);
        int nPoints = xy.length / 2;
        Coordinate[] coords = new Coordinate[nPoints + 1];
        int i = 0;
        int c = 0;
        while (i < xy.length) {
            double x = xy[i++];
            double y = xy[i++];
            coords[c++] = new Coordinate(x, y);
        }
        coords[c] = coords[0];
        return JTS.spatialObject(SPACE, FACTORY.createPolygon(FACTORY.createLinearRing(coords), null));
    }

    private void checkInside(SpatialObject spatialObject)
    {
        assertTrue(spatialObject.containedBy(SPACE));
    }

    private void checkOutside(SpatialObject spatialObject)
    {
        assertTrue(!spatialObject.containedBy(SPACE));
    }

    private static final double XLO = -100;
    private static final double YLO = -1000;
    private static final double XHI = 9999100;
    private static final double YHI = 99991000;

    private static final Space SPACE = Space.newSpace(new double[]{XLO, YLO},
                                                      new double[]{XHI, YHI},
                                                      new int[]{20, 20});
    private static final GeometryFactory FACTORY = new GeometryFactory();
}
