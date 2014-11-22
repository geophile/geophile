/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index.sortedarray;

import com.geophile.z.Cursor;
import com.geophile.z.Record;

import java.io.IOException;

public class SortedArrayCursor<RECORD extends Record> extends Cursor<RECORD>
{
    // Cursor interface

    @Override
    public RECORD next() throws IOException, InterruptedException
    {
        return neighbor();
    }

    @Override
    public void goTo(RECORD key)
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

    public SortedArrayCursor(SortedArray<RECORD> sortedArray)
    {
        super(sortedArray);
        this.sortedArray = sortedArray;
    }

    // For use by this class

    private RECORD neighbor() throws IOException, InterruptedException
    {
        switch (state()) {
            case NEVER_USED:
                startIteration(true);
                break;
            case IN_USE:
                break;
            case DONE:
                assert current() == null;
                return null;
        }
        if (position != DONE) {
            RECORD record = record(position);
            current(record);
            startAt = record;
            lastReportedPosition = position;
            state(State.IN_USE);
            position++;
            assert position <= sortedArray.n : position;
            if (position == sortedArray.n) {
                position = DONE;
            }
        } else {
            close();
        }
        return current();
    }

    private void startIteration(boolean includeStartKey)
    {
        position = sortedArray.binarySearch(startAt);
        if (position < 0) {
            // Key not found
            position = -position - 1; // See javadoc for binarySearch
        } else {
            // Key found
            if (!includeStartKey) {
                position++;
            }
        }
        assert position <= sortedArray.n : position;
        if (position == -1 || position == sortedArray.n) {
            position = DONE;
        }
    }

    private RECORD record(int i)
    {
        return (RECORD) sortedArray.records[i];
    }

    // Object state

    private static final int DONE = -1;
    private static final int UNDEFINED = -1;

    private final SortedArray<RECORD> sortedArray;
    private RECORD startAt;
    private int position;
    // Position of the last record returned via next(). Needed to support deleteCurrent().
    private int lastReportedPosition = UNDEFINED;
}
