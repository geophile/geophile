/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index.sortedarray;

import com.geophile.z.Cursor;
import com.geophile.z.Index;
import com.geophile.z.Record;
import com.geophile.z.index.RecordWithSpatialObject;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SortedArray implements the {@link com.geophile.z.Index} interface in terms of an array.
 * Intended to be used internally, for a spatial join between a SpatialIndex and a SpatialObject.
 */

public abstract class SortedArray<RECORD extends Record> extends Index<RECORD>
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
        ensureSpace(n + 1);
        records[n++] = record;
        sorted = false;
    }

    @Override
    public boolean remove(long z, Record.Filter<RECORD> filter)
    {
        boolean removeRecordFound = false;
        RECORD key = newKeyRecord();
        key.z(z);
        int binarySearchPosition = binarySearch(key);
        if (binarySearchPosition >= 0) {
            // There might be multiple occurrences of the same z. Search backward and forward for matching zs, looking
            // for a record satisfying the record filter.
            boolean sameZ = true;
            int position = binarySearchPosition;
            while (position >= 0 && sameZ && !removeRecordFound) {
                RECORD record = (RECORD) records[position];
                if (record.z() == z) {
                    if (filter.select(record)) {
                        removeRecordFound = true;
                    } else {
                        position--;
                    }
                } else {
                    sameZ = false;
                }
            }
            if (!removeRecordFound) {
                // Search forward
                sameZ = true;
                position = binarySearchPosition + 1;
                while (position < n && sameZ && !removeRecordFound) {
                    RECORD record = (RECORD) records[position];
                    if (record.z() == z) {
                        if (filter.select(record)) {
                            removeRecordFound = true;
                        } else {
                            position++;
                        }
                    } else {
                        sameZ = false;
                    }
                }
            }
            if (removeRecordFound) {
                System.arraycopy(records, position + 1, records, position, n - 1 - position);
                n--;
            }
        }
        return removeRecordFound;
    }

    @Override
    public Cursor<RECORD> cursor()
    {
        ensureSorted();
        return new SortedArrayCursor<RECORD>(this);
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
        return true;
    }

    // SortedArray

    public void reset()
    {
        n = 0;
    }

    public SortedArray()
    {}

    // For use by this package

    int binarySearch(RECORD key)
    {
        return Arrays.binarySearch(records, 0, n, key, SortedArray.Z_COMPARATOR);
    }

    void deleteRecord(int at)
    {
        System.arraycopy(records, at + 1, records, at, n - at - 1);
        records[--n] = null;
    }

    // For use by this class

    private void ensureSorted()
    {
        if (!sorted) {
            if (n == 0) {
                records = new Object[0];
            } else {
                Arrays.sort(records, 0, n, Z_COMPARATOR);
            }
            sorted = true;
        }
    }

    private void ensureSpace(int n)
    {
        int newLength = Math.max(MIN_ARRAY_SIZE, (int) (n * 1.5));
        if (records == null) {
            records = new Object[newLength];
        } else if (records.length < n) {
            records = Arrays.copyOf(records, newLength);
        }
    }

    // Class state

    private static final AtomicInteger idGenerator = new AtomicInteger(0);
    private static final int MIN_ARRAY_SIZE = 20;
    static final Comparator Z_COMPARATOR =
        new Comparator()
        {
            @Override
            public int compare(Object r, Object s)
            {
                long rz = ((Record)r).z();
                long sz = ((Record)s).z();
                return rz < sz ? -1 : rz > sz ? 1 : 0;
            }
        };

    // Object state

    private final String name = String.format("SortedArray(%s)", idGenerator.getAndIncrement());
    Object[] records;
    int n = 0;
    private boolean sorted = false;

    // Inner classes

    public static class OfBaseRecord extends SortedArray<RecordWithSpatialObject>
    {
        @Override
        public RecordWithSpatialObject newRecord()
        {
            return new RecordWithSpatialObject();
        }
    }
}
