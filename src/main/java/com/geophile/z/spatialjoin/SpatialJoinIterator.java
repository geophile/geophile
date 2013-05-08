package com.geophile.z.spatialjoin;

import com.geophile.z.Pair;
import com.geophile.z.SpatialIndex;
import com.geophile.z.spatialobject.SpatialObject;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SpatialJoinIterator<LEFT extends SpatialObject, RIGHT extends SpatialObject>
    implements Iterator<Pair<LEFT, RIGHT>>
{
    // Iterator interface

    @Override
    public boolean hasNext()
    {
        return pair != null;
    }

    @Override
    public Pair<LEFT, RIGHT> next()
    {
        Pair<LEFT, RIGHT> next;
        if (pair == null) {
            throw new NoSuchElementException();
        } else {
            next = pair;
            findNextPair();
        }
        return next;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }


    // SpatialJoinIterator interface

    public SpatialJoinIterator(SpatialIndex<LEFT> leftSpatialIndex,
                               SpatialIndex<RIGHT> rightSpatialIndex,
                               boolean excludeDuplicates)
    {
        this.left = new SpatialJoinInput<>(leftSpatialIndex);
        this.right = new SpatialJoinInput<>(rightSpatialIndex);
        this.excludeDuplicates = excludeDuplicates;
        findNextPair();
    }

    // For use by this class

    private void findNextPair()
    {

    }

    // Object state

    private final SpatialJoinInput<LEFT> left;
    private final SpatialJoinInput<RIGHT> right;
    private final boolean excludeDuplicates;
    private Pair<LEFT, RIGHT> pair;
}
