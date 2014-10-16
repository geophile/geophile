/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

import com.geophile.z.spatialjoin.SpatialObjectGenerator;
import com.geophile.z.spatialobject.jts.JTS;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import java.util.Random;

public class JTSMultiPointGenerator extends SpatialObjectGenerator
{
    @Override
    public SpatialObject newSpatialObject()
    {
        int c = 0;
        long x = random.nextInt(nx);
        long y = random.nextInt(ny);
        coords[c++] = new Coordinate(x, y);
        for (int i = 0; i < N_POINTS - 1; i++) {
            long xNew;
            long yNew;
            do {
                xNew = x - maxX + random.nextInt(2 * maxX);
                yNew = y - maxY + random.nextInt(2 * maxY);
            } while (!(xNew >= 0 && xNew < nx && yNew >= 0 && yNew < ny));
            coords[c++] = new Coordinate(xNew, yNew);
        }
        return JTS.spatialObject(space, factory.createMultiPoint(coords));
    }

    @Override
    public String description()
    {
        return String.format("JTSLineString(%s, %s)", maxX, maxY);
    }

    public JTSMultiPointGenerator(Space space, GeometryFactory factory, Random random, int maxX, int maxY)
    {
        super(space, random);
        this.factory = factory;
        this.nx = (int) (space.hi(0) - space.lo(0));
        this.ny = (int) (space.hi(1) - space.lo(1));
        this.maxX = maxX;
        this.maxY = maxY;
    }

    private static final int N_POINTS = 5;

    private final GeometryFactory factory;
    private final int nx;
    private final int ny;
    private final int maxX;
    private final int maxY;
    Coordinate[] coords = new Coordinate[N_POINTS];
}
