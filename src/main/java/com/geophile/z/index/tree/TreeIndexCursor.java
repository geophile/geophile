/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index.tree;

import com.geophile.z.SpatialObject;
import com.geophile.z.index.Cursor;
import com.geophile.z.index.Record;
import com.geophile.z.index.SpatialObjectKey;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class TreeIndexCursor extends Cursor
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
        this.startAt = key;
        state(State.NEVER_USED);
    }


    // TreeIndexCursor interface

    public TreeIndexCursor(TreeMap<SpatialObjectKey, SpatialObject> tree, SpatialObjectKey key)
    {
        this.tree = tree;
        this.startAt = key;
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
        if (treeIterator.hasNext()) {
            Map.Entry<SpatialObjectKey, SpatialObject> neighbor = treeIterator.next();
            current(neighbor.getKey().z(), neighbor.getValue());
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

    private final TreeMap<SpatialObjectKey, SpatialObject> tree;
    private SpatialObjectKey startAt;
    private boolean forward;
    private Iterator<Map.Entry<SpatialObjectKey, SpatialObject>> treeIterator;
}
