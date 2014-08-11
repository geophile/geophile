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

package com.geophile.z.spatialobject.jts;

import com.geophile.z.Space;
import com.geophile.z.SpatialObject;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

public class JTSPolygon extends JTSBaseWithBoundingBox
{
    // SpatialObject interface (not implemented by JTSBase)

    @Override
    public double[] arbitraryPoint()
    {
        double[] point = new double[2];
        ensureGeometry();
        Coordinate coordinate = polygon().getCoordinate();
        point[0] = coordinate.x;
        point[1] = coordinate.y;
        return point;
    }

    // JTSPolygon interface

    public JTSPolygon(Space space, Polygon polygon)
    {
        super(space, polygon);
    }

    public JTSPolygon()
    {}

    // For use by this class

    private Polygon polygon()
    {
        return (Polygon) geometry;
    }
}
