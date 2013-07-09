/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialjoin;

import com.geophile.z.Space;
import com.geophile.z.SpatialObject;
import com.geophile.z.spatialobject.jts.JTSPolygon;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import java.util.Random;

public class JTSSquareGenerator extends SpatialObjectGenerator
{
    @Override
    public SpatialObject newSpatialObject()
    {
        long xLo = random.nextInt(nx - xySize);
        long yLo = random.nextInt(ny - xySize);
        long xHi = xLo + random.nextInt(xySize);
        long yHi = yLo + random.nextInt(xySize);
        corners[0] = new Coordinate(xLo, yLo);
        corners[1] = new Coordinate(xHi, yLo);
        corners[2] = new Coordinate(xHi, yHi);
        corners[3] = new Coordinate(xLo, yHi);
        corners[4] = corners[0];
        return new JTSPolygon(space, factory.createPolygon(factory.createLinearRing(corners), null));
    }

    @Override
    public String description()
    {
        return String.format("JTSSquare(%s)", xySize);
    }

    public JTSSquareGenerator(Space space, GeometryFactory factory, Random random, int xySize)
    {
        super(space.applicationSpace(), random);
        this.space = space;
        this.factory = factory;
        this.nx = (int) (appSpace.hi(0) - appSpace.lo(0));
        this.ny = (int) (appSpace.hi(1) - appSpace.lo(1));
        this.xySize = xySize;
    }

    private final Space space;
    private final GeometryFactory factory;
    private final int nx;
    private final int ny;
    private final int xySize;
    Coordinate[] corners = new Coordinate[5];
}
