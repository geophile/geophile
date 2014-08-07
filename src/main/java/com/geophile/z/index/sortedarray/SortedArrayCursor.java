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

    @Override
    public boolean deleteCurrent() throws IOException, InterruptedException
    {
        boolean deleted = false;
        if (state() == State.IN_USE) {
            assert lastReportedPosition != UNDEFINED;
            sortedArray.deleteRecord(lastReportedPosition);
            if (position != DONE && position > lastReportedPosition) {
                position--;
            }
            deleted = true;
        }
        return deleted;
    }

    // SortedArrayCursor interface

    public SortedArrayCursor(SortedArray sortedArray, SpatialObjectKey startAt)
    {
        super(sortedArray);
        this.sortedArray = sortedArray;
        this.startAt = sortedArray.newRecord();
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
                assert current() == null;
                return null;
        }
        if (position != DONE) {
            Record record = record(position);
            current(record.key().z(), record.spatialObject());
            current().copyTo(startAt);
            lastReportedPosition = position;
            state(State.IN_USE);
            position += forwardMove ? 1 : -1;
            assert position >= -1 : position;
            assert position <= sortedArray.n : position;
            if (position == -1 || position == sortedArray.n) {
                position = DONE;
            }
        } else {
            close();
        }
        return current();
    }

    private void startIteration(boolean forwardMove, boolean includeStartKey)
    {
        position = Arrays.binarySearch(sortedArray.records, 0, sortedArray.n, startAt, SortedArray.RECORD_COMPARATOR);
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
        assert position <= sortedArray.n : position;
        if (position == -1 || position == sortedArray.n) {
            position = DONE;
        }
        forward = forwardMove;
    }

    private Record record(int i)
    {
        return (Record) sortedArray.records[i];
    }

    // Object state

    private static final int DONE = -1;
    private static final int UNDEFINED = -1;

    private final SortedArray sortedArray;
    private final Record startAt;
    private boolean forward;
    private int position;
    // Position of the last record returned via next() or previous(). Needed to support deleteCurrent().
    private int lastReportedPosition = UNDEFINED;
}
