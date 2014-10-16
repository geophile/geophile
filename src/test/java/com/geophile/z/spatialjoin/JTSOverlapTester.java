package com.geophile.z.spatialjoin;

import com.geophile.z.SpatialObject;
import com.geophile.z.spatialobject.jts.JTSSpatialObject;

public class JTSOverlapTester implements OverlapTester
{
    @Override
    public boolean overlap(SpatialObject s, SpatialObject t)
    {
        JTSSpatialObject a = (JTSSpatialObject) s;
        JTSSpatialObject b = (JTSSpatialObject) t;
        return a.geometry().intersects(b.geometry());
    }
}
