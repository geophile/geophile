package com.geophile.z.spatialjoin;

import com.geophile.z.Space;
import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialObject;
import com.geophile.z.index.treeindex.TreeIndex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestInput
{
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

    public int maxXSize()
    {
        return maxXSize;
    }

    public int maxYSize()
    {
        return maxYSize;
    }

    public TestInput(Space space, int maxXSize, int maxYSize, boolean singleCell)
        throws IOException, InterruptedException
    {
        this.spatialObjects = new ArrayList<>();
        this.spatialIndex = SpatialIndex.newSpatialIndex
            (space,
             new TreeIndex(),
             singleCell ? SpatialIndex.Options.SINGLE_CELL : SpatialIndex.Options.DEFAULT);
        this.maxXSize = maxXSize;
        this.maxYSize = maxYSize;
    }

    private final List<SpatialObject> spatialObjects;
    private final SpatialIndex spatialIndex;
    private final int maxXSize;
    private final int maxYSize;
}
