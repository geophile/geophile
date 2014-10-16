/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialobject.jts;

import com.geophile.z.Space;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class JTS
{
    public static JTSSpatialObject spatialObject(Space space, Point point)
    {
        return new JTSPoint(space, point);
    }

    public static JTSSpatialObject spatialObject(Space space, Geometry geometry)
    {
        return new JTSSpatialObjectWithBoundingBox(space, geometry);
    }
}
