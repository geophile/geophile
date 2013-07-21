package com.geophile.z.index;

import com.geophile.z.SpatialObject;
import com.geophile.z.space.Region;
import com.geophile.z.space.RegionComparison;

import java.nio.ByteBuffer;

import static org.junit.Assert.fail;

public class TestSpatialObject implements SpatialObject
{
    @Override
    public void id(long id)
    {
    }

    @Override
    public long id()
    {
        return id;
    }

    @Override
    public double[] arbitraryPoint()
    {
        fail();
        return null;
    }

    @Override
    public int maxZ()
    {
        return 1;
    }

    @Override
    public boolean equalTo(SpatialObject that)
    {
        return this.id == ((TestSpatialObject)that).id;
    }

    @Override
    public boolean containedBy(Region region)
    {
        fail();
        return false;
    }

    @Override
    public RegionComparison compare(Region region)
    {
        fail();
        return null;
    }

    @Override
    public void readFrom(ByteBuffer buffer)
    {
        id = buffer.getLong();
    }

    @Override
    public void writeTo(ByteBuffer buffer)
    {
        buffer.putLong(id);
    }

    public TestSpatialObject(long id)
    {
        this.id = id;
    }

    public TestSpatialObject()
    {}

    private long id;
}
