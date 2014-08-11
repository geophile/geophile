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

/**
 * TreeIndex implements the {@link com.geophile.z.Index} interface in terms of a {@link java.util.TreeMap}.
 * A TreeIndex is not safe for use for simultaneous use by multiple threads.
 */

public class TreeIndex extends Index
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
        TestRecord copy = (TestRecord) newRecord();
        record.copyTo(copy);
        boolean added = tree.add(copy);
        if (!added) {
            throw new DuplicateRecordException(copy);
        }
    }

    @Override
    public boolean remove(long z, RecordFilter recordFilter)
    {
        boolean foundRecord = false;
        boolean zMatch = true;
        Iterator<TestRecord> iterator = tree.tailSet(key(z)).iterator();
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
        return new TreeIndexCursor(this);
    }

    @Override
    public Record newRecord()
    {
        return new TestRecord();
    }

    // TreeIndex

    public TreeIndex()
    {
    }

    // For use by this package

    TreeSet<TestRecord> tree()
    {
        return tree;
    }

    // For use by this class

    private TestRecord key(long z)
    {
        TestRecord keyRecord = new TestRecord(null, 0);
        keyRecord.z(z);
        return keyRecord;
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
    private final TreeSet<TestRecord> tree = new TreeSet<>(RECORD_COMPARATOR);
}
