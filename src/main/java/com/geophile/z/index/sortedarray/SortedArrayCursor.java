/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index.sortedarray;

import com.geophile.z.index.Cursor;
import com.geophile.z.index.Record;
import com.geophile.z.index.SpatialObjectKey;

import java.io.IOException;
import java.util.Arrays;

public class SortedArrayCursor extends Cursor
{
    // Cursor interface

    @Override
    public Record next() throws IOException, InterruptedException
    {
        return neighbor(true);
    }

    @Override
    public Record previous() throws IOException, InterruptedException
    {
        return neighbor(false);
    }

    @Override
    public void goTo(SpatialObjectKey key)
    {
        this.startAt.set(key.z(), key.soid());
        state(State.NEVER_USED);
    }


    // TreeIndexCursor interface

    public SortedArrayCursor(SortedArray sortedArray, SpatialObjectKey startAt)
    {
        this.records = sortedArray.records;
        this.n = sortedArray.n;
        this.startAt.set(startAt.z(), startAt.soid());
    }

    // For use by this class

    private Record neighbor(boolean forwardMove) throws IOException, InterruptedException
    {
        switch (state()) {
            case NEVER_USED:
                startIteration(forwardMove, true);
                break;
            case IN_USE:
                if (forward != forwardMove) {
                    startIteration(forwardMove, false);
                }
                break;
            case DONE:
                assert current().eof();
                return current();
        }
        if (position != DONE) {
            Record record = record(position);
            current(record.key().z(), record.spatialObject());
            state(State.IN_USE);
            current().copyTo(startAt);
            position += forwardMove ? 1 : -1;
            assert position >= -1 : position;
            assert position <= n : position;
            if (position == -1 || position == n) {
                position = DONE;
            }
        } else {
            close();
        }
        return current();
    }

    private void startIteration(boolean forwardMove, boolean includeStartKey)
    {
        position = Arrays.binarySearch(records, 0, n, startAt, SortedArray.RECORD_COMPARATOR);
        if (position < 0) {
            // Key not found
            position = -position - 1; // See javadoc for binarySearch
            if (!forwardMove) {
                position--;
            }
        } else {
            // Key found
            if (!includeStartKey) {
                if (forwardMove) {
                    position++;
                } else {
                    position--;
                }
            }
        }
        assert position >= -1 : position;
        assert position <= n : position;
        if (position == -1 || position == n) {
            position = DONE;
        }
        forward = forwardMove;
    }

    private Record record(int i)
    {
        return (Record) records[i];
    }

    // Object state

    private static final int DONE = -1;

    private final Object[] records;
    private final int n;
    private final Record startAt = new Record();
    private boolean forward;
    private int position;
}
