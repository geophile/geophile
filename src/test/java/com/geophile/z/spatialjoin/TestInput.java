package com.geophile.z.spatialjoin;

import com.geophile.z.Space;
import com.geophile.z.SpatialIndex;
import com.geophile.z.index.treeindex.TreeIndex;
import com.geophile.z.spatialobject.d2.Box;

import java.util.ArrayList;
import java.util.List;

public class TestInput
{
    public void addBox(Box box)
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

    public TestInput(Space space)
    {
        this.boxes = new ArrayList<>();
        this.spatialIndex = SpatialIndex.newSpatialIndex(space, new TreeIndex<Box>());
    }

    private final List<Box> boxes;
    private final SpatialIndex<Box> spatialIndex;
}
