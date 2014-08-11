/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index.sortedarray;

import com.geophile.z.Cursor;
import com.geophile.z.Record;

import java.io.IOException;

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
    public void goTo(Record key)
    {
        this.startAt = key;
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

    public SortedArrayCursor(SortedArray sortedArray)
    {
        super(sortedArray);
        this.sortedArray = sortedArray;
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
            current(record);
            record.copyTo(startAt);
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
        position = sortedArray.binarySearch(startAt);
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
    private Record startAt;
    private boolean forward;
    private int position;
    // Position of the last record returned via next() or previous(). Needed to support deleteCurrent().
    private int lastReportedPosition = UNDEFINED;
}
