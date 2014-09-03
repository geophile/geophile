package com.geophile.z.index;

import com.geophile.z.Space;
import com.geophile.z.SpatialObject;
import com.geophile.z.space.Region;
import com.geophile.z.space.RegionComparison;

import java.nio.ByteBuffer;

import static org.junit.Assert.fail;

public class TestSpatialObject implements SpatialObject
{
    @Override
    public String toString()
    {
        return String.format("TestSpatialObject(%d)", id);
    }

    @Override
    public int hashCode()
    {
        return (int) id * 9987001;
    }

    @Override
    public boolean equals(Object that)
    {
        return that != null && that instanceof TestSpatialObject && this.id == ((TestSpatialObject)that).id;
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
    public boolean containedBy(Space space)
    {
        fail();
        return false;
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
