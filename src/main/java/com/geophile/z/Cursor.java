/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

import java.io.IOException;

public abstract class Cursor<RECORD extends Record>
{
    // Cursor interface

    /**
     * <ul>
     * <li><b>If the Cursor has just been created:</b>
     *     The result of calling this method is undefined.
     *
     * <li><b>If the Cursor has just been positioned using {@link #goTo(Record)}:</b>
     *     This method moves the Cursor to the
     *     {@link com.geophile.z.Record} with the key passed to goTo, or to the smallest
     *     {@link com.geophile.z.Record} whose key is greater than that key. If the key
     *     is greater than that largest key in the index, then the Cursor is closed and null is returned.
     *
     * <li><b>If the Cursor has just been accessed using next():</b>
     *     This method moves the Cursor to the {@link com.geophile.z.Record}
     *     with the next larger key, or to null if the Cursor was already positioned at the last
     *     {@link com.geophile.z.Record} of the index.
     * </ul>
     * @return The {@link com.geophile.z.Record} at the new Cursor position, or null if the Cursor
     * was moved past the last record.
     * @throws IOException
     * @throws InterruptedException
     */
    public abstract RECORD next() throws IOException, InterruptedException;

    /**
     * Position the Cursor at the {@link com.geophile.z.Record} with the given key. If there is
     * no such record, then the Cursor position is "between" records, and a call to {@link #next()}
     * will position the Cursor at one of the bounding records.
     * @param key The key to search for.
     * @throws IOException
     * @throws InterruptedException
     */
    public abstract void goTo(RECORD key) throws IOException, InterruptedException;

    /**
     * Delete the record returned by the immediately preceding call to next.
     * The return value is true if a record was deleted. If the record returned was null,
     * or if no record has been returned yet, (i.e., next has not been called), then the return
     * value is false. One other possibility
     * is that the record in question is no longer present, due to a concurrent operation
     * on the underlying Index, in which case the return value is also false.
     * This is unlikely, but could occur if the index is being
     * accessed at a low level of isolation, e.g. READ COMMITTED.
     * @return true iff a record was deleted.
     * @throws IOException
     * @throws InterruptedException
     */
    public abstract boolean deleteCurrent() throws IOException, InterruptedException;

    /**
     * Mark the Cursor as no longer usable. Subsequent calls to {@link #goTo(Record)}, {@link #next()},
     * or {@link #deleteCurrent()} will have undefined results.
     */
    public void close()
    {
        state = State.DONE;
        current = null;
    }

    // For use by subclasses

    protected final RECORD current() throws IOException, InterruptedException
    {
        return current;
    }

    protected final void current(RECORD record)
    {
        assert state != State.DONE;
        if (stableRecords) {
            current = record;
        } else {
            record.copyTo(current);
        }
    }

    protected State state()
    {
        return state;
    }

    protected void state(State newState)
    {
        assert newState != State.DONE;
        state = newState;
    }

    protected Cursor(Index<RECORD> index)
    {
        stableRecords = index.stableRecords();
        current = stableRecords ? null : index.newRecord();
    }

    // Object state

    private final boolean stableRecords;
    private RECORD current;
    private State state = State.NEVER_USED;

    // Inner classes

    protected enum State
    {
        // The cursor has been created, but has never been used to retrieve a record. If the key used to create
        // the cursor is present, then next() will retrieve the associated record. This state
        // is also used when a cursor is repositioned using goTo().
        NEVER_USED,

        // The cursor has been created and used to retrieve at least one record. next() moves the
        // cursor before retrieving a record.
        IN_USE,

        // The cursor has returned all records. A subsequent call to next() will return null.
        DONE
    }
}
