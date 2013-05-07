/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.index;

import com.geophile.index.Cursor;
import com.geophile.spatialobject.SpatialObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiCursor<SPATIAL_OBJECT extends SpatialObject> extends Cursor<SPATIAL_OBJECT>
{
    // Cursor interface

    @Override
    public Record<SPATIAL_OBJECT> next()
    {
        assert state != State.BUILDING : state;
        Record<SPATIAL_OBJECT> next = null;
        if (state == State.SCANNING) {
            next = cursor.next();
            while (next == null && currentCursor < inputs.size()) {
                cursor = inputs.get(currentCursor++);
                next = cursor.next();
            }
        }
        if (next == null) {
            close();
        } else {
            current(cursor.current().getKey(), cursor.current().getValue());
        }
        return current();
    }

    @Override
    public Record<SPATIAL_OBJECT> previous()
    {
        assert false;
        return null;
    }

    @Override
    public void goTo(long z)
    {
        assert false;
    }

    @Override
    public void close()
    {
        if (state != State.CLOSED) {
            super.close();
            cursor = null;
            currentCursor = inputs.size();
            state = State.CLOSED;
        }
    }

    // MultiCursor interface

    public void addInput(Cursor<SPATIAL_OBJECT> cursor)
    {
        assert state == State.BUILDING : state;
        assert this.cursor == null;
        inputs.add(cursor);
    }

    public void start()
    {
        assert state == State.BUILDING : state;
        assert cursor == null;
        state = State.SCANNING;
        if (inputs.isEmpty()) {
            close();
        } else {
            cursor = inputs.get(currentCursor++);
        }
    }

    // Object state

    private final List<Cursor<SPATIAL_OBJECT>> inputs = new ArrayList<>();
    private int currentCursor = 0;
    private Cursor<SPATIAL_OBJECT> cursor;
    private State state = State.BUILDING;

    // Inner classes

    private enum State { BUILDING, SCANNING, CLOSED }
}
