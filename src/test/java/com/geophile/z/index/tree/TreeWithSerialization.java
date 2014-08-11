/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index.tree;

import com.geophile.z.*;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

// Like TreeIndex, but with serialization of spatial objects

public class TreeWithSerialization extends Index
{
    // Object interface

    @Override
    public String toString()
    {
        return name;
    }

    // Index interface

    @Override
    public void add(Record record)
    {
        SerializedRecord copy = (SerializedRecord) newRecord();
        record.copyTo(copy);
        copy.serialize();
        boolean added = tree.add(copy);
        if (!added) {
            throw new DuplicateRecordException(record);
        }
    }

    @Override
    public boolean remove(long z, RecordFilter recordFilter)
    {
        boolean foundRecord = false;
        boolean zMatch = true;
        Iterator<SerializedRecord> iterator = tree.tailSet(key(z)).iterator();
        while (zMatch && iterator.hasNext() && !foundRecord) {
            Record record = iterator.next();
            if (record.z() == z) {
                foundRecord = recordFilter.select(record);
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
    public Cursor cursor()
    {
        return new TreeWithSerializationCursor(this);
    }

    @Override
    public Record newRecord()
    {
        return new SerializedRecord(serializer);
    }

    // TreeWithSerialization

    public TreeWithSerialization(SpatialObjectSerializer serializer)
    {
        this.serializer = serializer;
    }

    // For use by this package

    TreeSet<SerializedRecord> tree()
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
    private static final Comparator<TestRecord> RECORD_COMPARATOR =
        new Comparator<TestRecord>()
        {
            @Override
            public int compare(TestRecord r, TestRecord s)
            {
                return r.keyCompare(s);
            }
        };

    // Object state

    private final String name = String.format("TreeIndex(%s)", idGenerator.getAndIncrement());
    private final SpatialObjectSerializer serializer;
    private final TreeSet<SerializedRecord> tree = new TreeSet<>(RECORD_COMPARATOR);
}
