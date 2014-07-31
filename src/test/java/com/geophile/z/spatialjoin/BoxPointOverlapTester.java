/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialjoin;

import com.geophile.z.SpatialObject;
import com.geophile.z.spatialobject.d2.Box;
import com.geophile.z.spatialobject.d2.Point;

public class BoxPointOverlapTester implements OverlapTester
{
    @Override
    public boolean overlap(SpatialObject s, SpatialObject t)
    {
            Box b = (Box) s;
            Point p = (Point) t;
            return
                b.xLo() <= p.x() && p.x() <= b.xHi() &&
                b.yLo() <= p.y() && p.y() <= b.yHi();
    }
}
