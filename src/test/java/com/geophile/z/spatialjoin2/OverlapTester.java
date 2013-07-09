package com.geophile.z.spatialjoin2;

import com.geophile.z.SpatialObject;

public interface OverlapTester
{
    boolean overlap(SpatialObject s, SpatialObject t);
}
