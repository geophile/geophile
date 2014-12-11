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

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TreeIndex implements the {@link com.geophile.z.Index} interface in terms of a {@link java.util.TreeMap}.
 * A TreeIndex is not safe for use for simultaneous use by multiple threads.
 */

public abstract class TreeIndex<RECORD extends Record> extends Index<RECORD>
{
    // Object interface

    @Override
    public String toString()
    {
        return name;
    }

    // Index interface

    @Override
    public void add(RECORD record)
    {
        RECORD copy = newRecord();
        record.copyTo(copy);
        boolean added = tree.add(copy);
        if (!added) {
            throw new DuplicateRecordException(copy);
        }
    }

    @Override
    public boolean remove(long z, Record.Filter<RECORD> filter)
    {
        boolean foundRecord = false;
        boolean zMatch = true;
        Iterator<RECORD> iterator = tree.tailSet(key(z)).iterator();
        while (zMatch && iterator.hasNext() && !foundRecord) {
            RECORD record = iterator.next();
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
    public Cursor<RECORD> cursor()
    {
        return new TreeIndexCursor<>(this);
    }

    @Override
    public abstract RECORD newRecord();

    @Override
    public boolean blindUpdates()
    {
        return false;
    }

    @Override
    public boolean stableRecords()
    {
        return stableRecords;
    }

    // TreeIndex

    public TreeIndex(Comparator<RECORD> recordComparator, boolean stableRecords)
    {
        this.tree = new TreeSet<>(recordComparator);
        this.stableRecords = stableRecords;
    }

    // For use by this package

    TreeSet<RECORD> tree()
    {
        return tree;
    }

    // For use by this class

    private RECORD key(long z)
    {
        RECORD keyRecord = newRecord();
        keyRecord.z(z);
        return keyRecord;
    }

    // Class state

    private static final AtomicInteger idGenerator = new AtomicInteger(0);

    // Object state

    private final String name = String.format("TreeIndex(%s)", idGenerator.getAndIncrement());
    private final TreeSet<RECORD> tree;
    private final boolean stableRecords;
}
