package com.geophile.z.spatialjoin2;

import com.geophile.z.SpatialObject;
import com.geophile.z.spatialobject.jts.JTSBase;

public class JTSOverlapTester implements OverlapTester
{
    @Override
    public boolean overlap(SpatialObject s, SpatialObject t)
    {
        JTSBase a = (JTSBase) s;
        JTSBase b = (JTSBase) t;
        return a.geometry().intersects(b.geometry());
    }
}
