package com.geophile.index;

import com.geophile.spatialobject.SpatialObject;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class CursorIterator<SPATIAL_OBJECT extends SpatialObject> implements Iterator<SPATIAL_OBJECT>
{
    // Iterator interface

    @Override
    public boolean hasNext()
    {
        return current != null;
    }

    @Override
    public SPATIAL_OBJECT next()
    {
        SPATIAL_OBJECT next = current;
        if (current == null) {
            throw new NoSuchElementException();
        } else {
            getCurrent();
        }
        return next;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }


    // CursorIterator interface

    public CursorIterator(Cursor<SPATIAL_OBJECT> cursor)
    {
        this.cursor = cursor;
        getCurrent();
    }

    // For use by this class

    private void getCurrent()
    {
        Record<SPATIAL_OBJECT> entry = cursor.next();
        this.current = entry == null ? null : entry.getValue();
    }

    // Object state

    private final Cursor<SPATIAL_OBJECT> cursor;
    private SPATIAL_OBJECT current;
}
