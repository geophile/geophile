package com.geophile.z.spatialjoin;

import com.geophile.z.SpatialIndex;
import com.geophile.z.index.Cursor;
import com.geophile.z.index.Record;
import com.geophile.z.spatialobject.SpatialObject;
import com.geophile.z.space.SpaceImpl;
import com.geophile.z.space.SpatialIndexImpl;

import java.util.ArrayDeque;
import java.util.Deque;

class SpatialJoinInput<SPATIAL_OBJECT extends SpatialObject>
{
    public void enterRegion()
    {
        assert nextToEnter != null;
        fixNest();
        nextToEnter = cursor.next();
    }

    public void exitRegion()
    {
        assert !nest.isEmpty();
        nest.pop();
    }

    public long nextEntry()
    {
        return nextToEnter == null ? EOF : nextToEnter.key().z();
    }

    public long nextExit()
    {
        return nest.isEmpty() ? EOF : nest.peek().key().z();
    }

    public SpatialJoinInput(SpatialIndex<SPATIAL_OBJECT> spatialIndex)
    {
        space = (SpaceImpl) spatialIndex.space();
        cursor = ((SpatialIndexImpl<SPATIAL_OBJECT>)spatialIndex).index().cursor(Long.MIN_VALUE);
    }

    private void fixNest()
    {
        long z = nextToEnter.key().z();
        Record<SPATIAL_OBJECT> top;
        while ((top = nest.peek()) != null && !space.contains(top.key().z(), z)) {
            nest.pop();
        }
        nest.push(nextToEnter);
    }

    public static long EOF = Long.MAX_VALUE;

    private final SpaceImpl space;
    private final Cursor<SPATIAL_OBJECT> cursor;
    private final Deque<Record<SPATIAL_OBJECT>> nest = new ArrayDeque<>();
    private Record<SPATIAL_OBJECT> nextToEnter;
}
