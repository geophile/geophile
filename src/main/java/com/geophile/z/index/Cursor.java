/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index;

import com.geophile.z.SpatialObject;

import java.io.IOException;

public abstract class Cursor
{
    public abstract Record next() throws IOException, InterruptedException;

    public abstract Record previous() throws IOException, InterruptedException;

    public abstract void goTo(SpatialObjectKey key) throws IOException, InterruptedException;

    public void close()
    {
        state = State.DONE;
    }

    public final Record current() throws IOException, InterruptedException
    {
        return state == State.DONE ? null : current;
    }

    protected final void current(long z, SpatialObject spatialObject)
    {
        assert state != State.DONE;
        current.set(z, spatialObject);
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

    // Object state

    private final Record current = new Record();
    private State state = State.NEVER_USED;

    // Inner classes

    protected enum State
    {
        // The cursor has been created, but has never been used to retrieve a record. If the key used to create
        // the cursor is present, then both next() and previous() will retrieve the associated record. This state
        // is also used when a cursor is repositioned using goTo().
        NEVER_USED,

        // The cursor has been created and used to retrieve at least one record. next() and previous() move the
        // cursor before retrieving a record.
        IN_USE,

        // The cursor has run off one end. A call to next() or previous() will return null.
        DONE
    }
}
