package com.geophile.z.spatialjoin2;

import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestInput
{
    public String toString()
    {
        return String.format("%s: %s", spatialObjects.size(), description);
    }

    public void add(SpatialObject spatialObject) throws IOException, InterruptedException
    {
        spatialObjects.add(spatialObject);
        spatialIndex.add(spatialObject);
    }

    public List<SpatialObject> spatialObjects()
    {
        return spatialObjects;
    }

    public SpatialIndex spatialIndex()
    {
        return spatialIndex;
    }

    public TestInput(SpatialIndex spatialIndex, String description)
        throws IOException, InterruptedException
    {
        this.description = description;
        this.spatialObjects = new ArrayList<>();
        this.spatialIndex = spatialIndex;
        for (SpatialObject spatialObject : spatialObjects) {
            spatialIndex.add(spatialObject);
        }
    }

    private final String description;
    private final List<SpatialObject> spatialObjects;
    private final SpatialIndex spatialIndex;
}
