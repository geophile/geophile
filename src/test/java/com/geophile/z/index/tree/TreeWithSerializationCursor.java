/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index.tree;

import com.geophile.z.Cursor;
import com.geophile.z.TestRecord;

import java.io.IOException;
import java.util.Iterator;

public class TreeWithSerializationCursor extends Cursor<TestRecord>
{
    // Cursor interface

    @Override
    public TestRecord next() throws IOException, InterruptedException
    {
        return neighbor(true);
    }

    @Override
    public void goTo(TestRecord key)
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

    public TreeWithSerializationCursor(TreeWithSerialization treeIndex)
    {
        super(treeIndex);
        this.treeIndex = treeIndex;
    }

    // For use by this class

    private TestRecord neighbor(boolean forwardMove) throws IOException, InterruptedException
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
            TestRecord neighbor = treeIterator.next();
            current(neighbor);
            startAt = neighbor;
            state(State.IN_USE);
        } else {
            close();
        }
        return current();
    }

    private void startIteration(boolean forwardMove, boolean includeStartKey)
    {
        treeIterator =
            forwardMove
            ? treeIndex.tree().tailSet(startAt, includeStartKey).iterator()
            : treeIndex.tree().headSet(startAt, includeStartKey).descendingIterator();
        forward = forwardMove;
    }

    // Object state

    private final TreeWithSerialization treeIndex;
    private TestRecord startAt;
    private boolean forward;
    private Iterator<TestRecord> treeIterator;
}
