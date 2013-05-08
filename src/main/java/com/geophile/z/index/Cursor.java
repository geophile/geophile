/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index;

import com.geophile.z.spatialobject.SpatialObject;

/**
 * A {@link Cursor} provides access to the spatial objects satisfying a spatial search, carried out by
 * {@link com.geophile.z.SpatialIndex#overlapping(com.geophile.z.spatialobject.SpatialObject, com.geophile.z.SpatialIndex.Duplicates)}.
 * @param <SPATIAL_OBJECT> The type of {@link com.geophile.z.spatialobject.SpatialObject} that will be returned by this cursor.
 */

public abstract class Cursor<SPATIAL_OBJECT extends SpatialObject>
{
    /**
     * Returns the next spatial object of the cursor.
     * @return The next spatial object of the cursor, or null if the cursor is complete.
     */
    public abstract Record<SPATIAL_OBJECT> next();

    /**
     * Returns the previous spatial object of the cursor.
     * @return The previous spatial object of the cursor, or null if the cursor is complete.
     */
    public abstract Record<SPATIAL_OBJECT> previous();

    public abstract void goTo(SpatialObjectKey key);

    /**
     * After calling close(), subsequent calls to {@link #next()} will return null.
     */
    public void close()
    {
        state = State.DONE;
    }

    public final Record<SPATIAL_OBJECT> current()
    {
        return state == State.DONE ? null : current;
    }

    protected final void current(long z, SPATIAL_OBJECT spatialObject)
    {
        current.set(z, spatialObject);
    }

    protected State state()
    {
        return state;
    }

    protected void state(State newState)
    {
        state = newState;
    }

    // Object state

    private final Record<SPATIAL_OBJECT> current = new Record<>();
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
        DONE;

        public boolean isDone()
        {
            return this == DONE;
        }
    }
}
