package com.geophile.z.spatialjoin;

import com.geophile.z.SpatialObject;

public interface OverlapTester
{
    boolean overlap(SpatialObject s, SpatialObject t);
}
