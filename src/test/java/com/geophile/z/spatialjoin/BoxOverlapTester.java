package com.geophile.z.spatialjoin;

import com.geophile.z.SpatialObject;
import com.geophile.z.spatialobject.d2.Box;

public class BoxOverlapTester implements OverlapTester
{
    @Override
    public boolean overlap(SpatialObject s, SpatialObject t)
    {
            Box a = (Box) s;
            Box b = (Box) t;
            return
                a.xLo() <= b.xHi() && b.xLo() <= a.xHi() &&
                a.yLo() <= b.yHi() && b.yLo() <= a.yHi();
    }
}
