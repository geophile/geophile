/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index.tree;

import com.geophile.z.Cursor;
import com.geophile.z.DuplicateRecordException;
import com.geophile.z.Index;
import com.geophile.z.Record;
import com.geophile.z.SerializedRecord;
import com.geophile.z.SpatialObjectSerializer;
import com.geophile.z.TestRecord;

import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

// Like TreeIndex, but with serialization of spatial objects

public class TreeWithSerialization extends Index<TestRecord>
{
    // Object interface

    @Override
    public String toString()
    {
        return name;
    }

    // Index interface

    @Override
    public void add(TestRecord record)
    {
        SerializedRecord copy = newRecord();
        record.copyTo(copy);
        copy.serialize();
        boolean added = tree.add(copy);
        if (!added) {
            throw new DuplicateRecordException(record);
        }
    }

    @Override
    public boolean remove(long z, Record.Filter<TestRecord> filter)
    {
        boolean foundRecord = false;
        boolean zMatch = true;
        Iterator<TestRecord> iterator = tree.tailSet(key(z)).iterator();
        while (zMatch && iterator.hasNext() && !foundRecord) {
            TestRecord record = iterator.next();
            if (record.z() == z) {
                foundRecord = filter.select(record);
            } else {
                zMatch = false;
            }
        }
        if (foundRecord) {
            iterator.remove();
        }
        return foundRecord;
    }

    @Override
    public Cursor<TestRecord> cursor()
    {
        return new TreeWithSerializationCursor(this);
    }

    @Override
    public SerializedRecord newRecord()
    {
        return new SerializedRecord(serializer);
    }

    @Override
    public boolean blindUpdates()
    {
        return false;
    }

    @Override
    public boolean stableRecords()
    {
        return true;
    }

    // TreeWithSerialization

    public TreeWithSerialization(SpatialObjectSerializer serializer)
    {
        this.serializer = serializer;
    }

    // For use by this package

    TreeSet<TestRecord> tree()
    {
        return tree;
    }

    // For use by this class

    private SerializedRecord key(long z)
    {
        return new SerializedRecord(serializer, z, null, 0);
    }

    // Class state

    private static final AtomicInteger idGenerator = new AtomicInteger(0);

    // Object state

    private final String name = String.format("TreeIndex(%s)", idGenerator.getAndIncrement());
    private final SpatialObjectSerializer serializer;
    private final TreeSet<TestRecord> tree = new TreeSet<>(TestRecord.COMPARATOR);
}
