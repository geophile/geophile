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

    private void startIteration(boolean includeStartKey)
    {
        treeIterator = tree.tailSet(startAt, includeStartKey).iterator();
    }

    // Object state

    private final TreeSet<RECORD> tree;
    private RECORD startAt;
    private Iterator<RECORD> treeIterator;
}
