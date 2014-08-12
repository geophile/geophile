/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialobject.jts;

import com.geophile.z.Space;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryCollection;

public class JTSCollection extends JTSBaseWithBoundingBox
{
    // SpatialObject interface (not implemented by JTSBase)

    @Override
    public double[] arbitraryPoint()
    {
        double[] point = new double[2];
        ensureGeometry();
        Coordinate coordinate = geometryCollection().getCoordinate();
        point[0] = coordinate.x;
        point[1] = coordinate.y;
        return point;
    }

    // JTSCollection interface

    public JTSCollection(Space space, GeometryCollection geometryCollection)
    {
        super(space, geometryCollection);
    }

    public JTSCollection()
    {}

    // For use by this class

    private GeometryCollection geometryCollection()
    {
        return (GeometryCollection) geometry;
    }
}
