package com.geophile.z.spatialjoin;

import com.geophile.z.Record;
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
        TestRecord record = new TestRecord(spatialObject, soidCounter++);
        records.add(record);
        spatialIndex.add(record);
    }

    public List<Record> records()
    {
        return records;
    }

    public SpatialIndex spatialIndex()
    {
        return spatialIndex;
    }

    public Record only()
    {
        assert records.size() == 1;
        return records.get(0);
    }

    public TestInput(SpatialIndex spatialIndex, String description)
        throws IOException, InterruptedException
    {
        this.description = description;
        this.records = new ArrayList<>();
        this.spatialIndex = spatialIndex;
    }

    private final String description;
    private final List<Record> records;
    private final SpatialIndex spatialIndex;
    private int soidCounter = 0;
}
