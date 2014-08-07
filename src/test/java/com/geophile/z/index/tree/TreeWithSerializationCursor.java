/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index.tree;

import com.geophile.z.Serializer;
import com.geophile.z.index.Cursor;
import com.geophile.z.index.Record;
import com.geophile.z.index.SpatialObjectKey;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class TreeWithSerializationCursor extends Cursor
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

    @Override
    public boolean deleteCurrent() throws IOException, InterruptedException
    {
        boolean removed = false;
        if (state() == State.IN_USE) {
            treeIterator.remove();
            removed = true;
        }
        return removed;
    }

    // TreeIndexCursor interface

    public TreeWithSerializationCursor(Serializer serializer,
                                       TreeWithSerialization treeIndex,
                                       SpatialObjectKey key)
    {
        super(treeIndex);
        this.serializer = serializer;
        this.tree = treeIndex.tree();
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
            Map.Entry<SpatialObjectKey, ByteBuffer> neighbor = treeIterator.next();
            ByteBuffer buffer = neighbor.getValue();
            buffer.mark();
            current(neighbor.getKey().z(), serializer.deserialize(buffer));
            buffer.reset();
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

    private final TreeMap<SpatialObjectKey, ByteBuffer> tree;
    private final Serializer serializer;
    private SpatialObjectKey startAt;
    private boolean forward;
    private Iterator<Map.Entry<SpatialObjectKey, ByteBuffer>> treeIterator;
}
