/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index;

import com.geophile.z.Record;
import com.geophile.z.SpatialObject;

public class RecordWithSpatialObject implements Record
{
    // Object interface

    @Override
    public String toString()
    {
        return String.format("(0x%016x: %s)", z, spatialObject);
    }

    @Override
    public int hashCode()
    {
        return spatialObjectHash;
    }

    @Override
    public boolean equals(Object o)
    {
        boolean eq = false;
        if (o != null && o instanceof RecordWithSpatialObject) {
            RecordWithSpatialObject that = (RecordWithSpatialObject) o;
            eq =
                this.spatialObjectHash == that.spatialObjectHash &&
                this.spatialObject.equals(that.spatialObject);
        }
        return eq;
    }

    // Record interface

    public long z()
    {
        return z;
    }

    public void z(long newZ)
    {
        z = newZ;
    }

    public void copyTo(Record record)
    {
        if (record == this) {
            throw new IllegalArgumentException();
        }
        RecordWithSpatialObject that = (RecordWithSpatialObject) record;
        that.z = this.z;
        that.spatialObject = this.spatialObject;
        that.spatialObjectHash = this.spatialObjectHash;
    }

    // RecordWithSpatialObject interface

    public SpatialObject spatialObject()
    {
        return spatialObject;
    }

    public void spatialObject(SpatialObject spatialObject)
    {
        this.spatialObject = spatialObject;
        this.spatialObjectHash = spatialObject == null ? 0 : spatialObject.hashCode();
    }

    public RecordWithSpatialObject()
    {
    }

    // Object state

    private long z;
    private SpatialObject spatialObject;
    private int spatialObjectHash;

    // Inner classes

    public static class Factory implements Record.Factory<RecordWithSpatialObject>
    {
        @Override
        public RecordWithSpatialObject newRecord()
        {
            RecordWithSpatialObject record = new RecordWithSpatialObject();
            record.spatialObject(spatialObject);
            return record;
        }

        public Factory setup(SpatialObject spatialObject)
        {
            this.spatialObject = spatialObject;
            return this;
        }

        private SpatialObject spatialObject;
    }
}
