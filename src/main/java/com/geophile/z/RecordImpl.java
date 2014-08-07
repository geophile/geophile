/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

public class RecordImpl implements Record
{
    // Object interface

    @Override
    public String toString()
    {
        return key.toString();
    }

    // Record interface

    public SpatialObjectKey key()
    {
        return key;
    }

    public SpatialObject spatialObject()
    {
        return spatialObject;
    }

    public void copyTo(Record record)
    {
        if (record == this) {
            throw new IllegalArgumentException();
        }
        RecordImpl that = (RecordImpl) record;
        that.key = this.key;
        that.spatialObject = this.spatialObject;
    }

    public void set(long z, SpatialObject spatialObject)
    {
        this.key = SpatialObjectKey.key(z, spatialObject.id());
        this.spatialObject = spatialObject;
    }

    // For creating a Record used just for its key, e.g. in a binary search of an array of Records.
    public void set(long z, long soid)
    {
        this.key = SpatialObjectKey.key(z, soid);
        this.spatialObject = null;
    }

    public RecordImpl()
    {
    }

    // Object state

    private SpatialObjectKey key;
    private SpatialObject spatialObject;
}
