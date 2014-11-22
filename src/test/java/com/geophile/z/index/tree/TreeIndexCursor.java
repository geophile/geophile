/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index.tree;

import com.geophile.z.Cursor;
import com.geophile.z.Record;

import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

public class TreeIndexCursor<RECORD extends Record> extends Cursor<RECORD>
{
    // Cursor interface

    @Override
    public RECORD next() throws IOException, InterruptedException
    {
        return neighbor(true);
    }

    @Override
    public RECORD previous() throws IOException, InterruptedException
    {
        return neighbor(false);
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
        boolean removed = false;
        if (state() == State.IN_USE) {
            treeIterator.remove();
            removed = true;
        }
        return removed;
    }

    // TreeIndexCursor interface

    public TreeIndexCursor(TreeIndex<RECORD> treeIndex)
    {
        super(treeIndex);
        this.tree = treeIndex.tree();
    }

    // For use by this class

    private RECORD neighbor(boolean forwardMove) throws IOException, InterruptedException
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
            RECORD neighbor = treeIterator.next();
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
            ? tree.tailSet(startAt, includeStartKey).iterator()
            : tree.headSet(startAt, includeStartKey).descendingIterator();
        forward = forwardMove;
    }

    // Object state

    private final TreeSet<RECORD> tree;
    private RECORD startAt;
    private boolean forward;
    private Iterator<RECORD> treeIterator;
}
