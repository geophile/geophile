/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index;

import com.geophile.Space;
import com.geophile.z.spatialobject.SpatialObject;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class TreeIndexCursor<SPATIAL_OBJECT extends SpatialObject> extends Cursor<SPATIAL_OBJECT>
{
    // Cursor interface

    @Override
    public Record<SPATIAL_OBJECT> next()
    {
        return neighbor(true);
    }

    @Override
    public Record<SPATIAL_OBJECT> previous()
    {
        return neighbor(false);
    }

    @Override
    public void goTo(long z)
    {
        this.startAt = z;
        state(State.NEVER_USED);
    }


    // TreeIndexCursor interface

    public TreeIndexCursor(Space space, TreeMap<Long, SPATIAL_OBJECT> tree, long z)
    {
        this.tree = tree;
        this.startAt = z;
    }

    // For use by this class

    private Record<SPATIAL_OBJECT> neighbor(boolean forwardMove)
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
                return null;
        }
        if (treeIterator.hasNext()) {
            Map.Entry<Long, SPATIAL_OBJECT> neighbor = treeIterator.next();
            current(neighbor.getKey(), neighbor.getValue());
            state(State.IN_USE);
            startAt = neighbor.getKey();
        } else {
            close();
        }
        return current();
    }

    private void startIteration(boolean forwardMove, boolean includeStartKey)
    {
        treeIterator =
            forwardMove
            ? tree.tailMap(startAt, includeStartKey).entrySet().iterator()
            : tree.headMap(startAt, includeStartKey).descendingMap().entrySet().iterator();
        forward = forwardMove;
    }

    // Object state

    private final TreeMap<Long, SPATIAL_OBJECT> tree;
    private long startAt;
    private boolean forward;
    private Iterator<Map.Entry<Long, SPATIAL_OBJECT>> treeIterator;
}
