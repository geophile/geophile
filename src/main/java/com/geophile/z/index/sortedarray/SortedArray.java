/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index.sortedarray;

import com.geophile.z.Index;
import com.geophile.z.SpatialObject;
import com.geophile.z.index.Cursor;
import com.geophile.z.index.Record;
import com.geophile.z.index.SpatialObjectKey;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SortedArray implements the {@link com.geophile.z.Index} interface in terms of an array.
 * Intended to be used internally, for a spatial join between a SpatialIndex and a SpatialObject.
 */

public class SortedArray implements Index
{
    // Object interface

    @Override
    public String toString()
    {
        return name;
    }

    // Index interface

    @Override
    public boolean blindUpdates()
    {
        return false;
    }

    @Override
    public void add(long z, SpatialObject spatialObject)
    {
        ensureSpace(n + 1);
        Record record = new Record();
        record.set(z, spatialObject);
        records[n++] = record;
        sorted = false;
    }

    @Override
    public boolean remove(long z, long soid)
    {
        boolean removed = false;
        Record record = new Record();
        record.set(z, soid);
        int position = Arrays.binarySearch(records, 0, n, record, SortedArray.RECORD_COMPARATOR);
        if (position >= 0) {
            System.arraycopy(records, position + 1, records, position, n - 1 - position);
            n--;
            removed = true;
        }
        return removed;
    }

    @Override
    public Cursor cursor(long z)
    {
        ensureSorted();
        return new SortedArrayCursor(this, key(z));
    }

    @Override
    public SpatialObjectKey key(long z)
    {
        return SpatialObjectKey.keyLowerBound(z);
    }

    @Override
    public SpatialObjectKey key(long z, long soid)
    {
        return SpatialObjectKey.key(z, soid);
    }

    // SortedArray

    public void reset()
    {
        n = 0;
    }

    // For use by this class

    private void ensureSorted()
    {
        if (!sorted) {
            if (n == 0) {
                records = new Object[0];
            } else {
                Arrays.sort(records, 0, n, RECORD_COMPARATOR);
            }
            sorted = true;
        }
    }

    private void ensureSpace(int n)
    {
        if (records == null || records.length < n) {
            records = Arrays.copyOf(records, Math.max(MIN_ARRAY_SIZE, (int) (n * 1.5)));
        }
    }

    // Class state

    private static final AtomicInteger idGenerator = new AtomicInteger(0);
    private static final int MIN_ARRAY_SIZE = 20;
    static final Comparator RECORD_COMPARATOR =
        new Comparator()
        {
            @Override
            public int compare(Object x, Object y)
            {
                return ((Record)x).key().compareTo(((Record)y).key());
            }
        };

    // Object state

    private final String name = String.format("SortedArray(%s)", idGenerator.getAndIncrement());
    Object[] records;
    int n = 0;
    private boolean sorted = false;
}
