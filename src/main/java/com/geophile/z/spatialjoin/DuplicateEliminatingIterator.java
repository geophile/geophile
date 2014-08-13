/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialjoin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

class DuplicateEliminatingIterator<T> implements Iterator<T>
{
    // Iterator interface

    @Override
    public boolean hasNext()
    {
        tryToEnsureNext();
        return next != null;
    }

    @Override
    public T next()
    {
        tryToEnsureNext();
        if (next == null) {
            throw new NoSuchElementException();
        }
        T output = next;
        next = null;
        seen.add(output);
        return output;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    // DuplicateEliminatingIterator interface

    public DuplicateEliminatingIterator(Iterator<T> input)
    {
        this.input = input;
    }

    // For use by this class

    private void tryToEnsureNext()
    {
        while (next == null && input.hasNext()) {
            next = input.next();
            if (seen.contains(next)) {
                next = null;
            }
        }
    }

    // Object state

    private final Iterator<T> input;
    private final HashSet<T> seen = new HashSet<>();
    private T next;
}
