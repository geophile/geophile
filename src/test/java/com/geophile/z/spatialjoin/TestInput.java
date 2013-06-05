package com.geophile.z.spatialjoin;

import com.geophile.z.Space;
import com.geophile.z.SpatialIndex;
import com.geophile.z.index.treeindex.TreeIndex;
import com.geophile.z.spatialobject.d2.Box;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestInput
{
    public void addBox(Box box) throws IOException, InterruptedException
    {
        boxes.add(box);
        spatialIndex.add(box);
    }

    public List<Box> boxes()
    {
        return boxes;
    }

    public SpatialIndex<Box> spatialIndex()
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
        this.boxes = new ArrayList<>();
        this.spatialIndex = SpatialIndex.newSpatialIndex
            (space,
             new TreeIndex<Box>(),
             singleCell ? SpatialIndex.Options.SINGLE_CELL : SpatialIndex.Options.DEFAULT);
        this.maxXSize = maxXSize;
        this.maxYSize = maxYSize;
    }

    private final List<Box> boxes;
    private final SpatialIndex<Box> spatialIndex;
    private final int maxXSize;
    private final int maxYSize;
}
