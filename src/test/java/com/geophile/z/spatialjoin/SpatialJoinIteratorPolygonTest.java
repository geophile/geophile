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

package com.geophile.z.spatialjoin;

import com.geophile.z.spatialobject.jts.JTSPolygon;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;

public class SpatialJoinIteratorPolygonTest extends SpatialJoinIteratorJTSTestBase
{
    @Override
    protected JTSPolygon newLeftObject(int maxXSize, int maxYSize)
    {
        int nx = (int) (appSpace.hi(0) - appSpace.lo(0));
        int ny = (int) (appSpace.hi(1) - appSpace.lo(1));
        Coordinate[] coords = new Coordinate[4];
        int c = 0;
        long x = random.nextInt(nx);
        long y = random.nextInt(ny);
        coords[c++] = new Coordinate(x, y);
        for (int i = 0; i < 2; i++) {
            long xNew;
            long yNew;
            do {
                xNew = x - maxXSize + random.nextInt(2 * maxXSize);
                yNew = y - maxYSize + random.nextInt(2 * maxYSize);
            } while (!(xNew >= 0 && xNew < nx && yNew >= 0 && yNew < ny));
            coords[c++] = new Coordinate(xNew, yNew);
            x = xNew;
            y = yNew;
        }
        coords[c] = coords[0]; // Close the ring
        LinearRing ring = factory.createLinearRing(coords);
        return new JTSPolygon(space, factory.createPolygon(ring, null));
    }
}
