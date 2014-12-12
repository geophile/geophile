package com.geophile.z.spatialjoin;

import com.geophile.z.SpatialIndex;
import com.geophile.z.SpatialObject;
import com.geophile.z.TestRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestInput
{
    public String toString()
    {
        return String.format("%s: %s", records.size(), description);
    }

    public void add(SpatialObject spatialObject) throws IOException, InterruptedException
    {
        spatialIndex.add(spatialObject, RECORD_FACTORY.setup(spatialObject, soidCounter++));
        records.add(RECORD_FACTORY.newRecord());
    }

    public List<TestRecord> records()
    {
        return records;
    }

    public SpatialIndex<TestRecord> spatialIndex()
    {
        return spatialIndex;
    }

    public TestRecord only()
    {
        assert records.size() == 1;
        return records.get(0);
    }

    public TestInput(SpatialIndex<TestRecord> spatialIndex, String description)
        throws IOException, InterruptedException
    {
        this.description = description;
        this.records = new ArrayList<>();
        this.spatialIndex = spatialIndex;
    }

    private static final TestRecord.Factory RECORD_FACTORY = new TestRecord.Factory();

    private final String description;
    private final List<TestRecord> records;
    private final SpatialIndex<TestRecord> spatialIndex;
    private int soidCounter = 0;
}
