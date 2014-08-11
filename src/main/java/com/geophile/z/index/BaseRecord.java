/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index;

import com.geophile.z.Record;
import com.geophile.z.SpatialObject;

/*
 * Encapsulates a z-value and a SpatialObject. This is unlikely to be useful by itself, because there is nothing
 * to guarantee uniqueness. It will normally be necessary to provide a Record implementation that either extends
 * BaseRecord, or implements Record directly.
 */

public class BaseRecord implements Record
{
    // Object interface

    @Override
    public String toString()
    {
        return String.format("0x%016x: %s)", z, spatialObject);
    }

    @Override
    public int hashCode()
    {
        return spatialObjectHash;
    }

    @Override
    public boolean equals(Object obj)
    {
        boolean eq = false;
        if (obj != null && obj instanceof BaseRecord) {
            BaseRecord that = (BaseRecord) obj;
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

    public SpatialObject spatialObject()
    {
        return spatialObject;
    }

    public void copyTo(Record record)
    {
        if (record == this) {
            throw new IllegalArgumentException();
        }
        BaseRecord that = (BaseRecord) record;
        that.z = this.z;
        that.spatialObject = this.spatialObject;
        that.spatialObjectHash = this.spatialObjectHash;
    }

    public int keyCompare(Record record)
    {
        BaseRecord that = (BaseRecord) record;
        return this.z < that.z ? -1 : this.z > that.z ? 1 : 0;
    }

    @Override
    public int keyHash()
    {
        return spatialObjectHash;
    }

    // BaseRecord interface

    public BaseRecord()
    {}

    public void spatialObject(SpatialObject spatialObject)
    {
        this.spatialObject = spatialObject;
        this.spatialObjectHash = spatialObject == null ? 0 : spatialObject.hashCode();
    }

    // Object state

    private long z;
    private SpatialObject spatialObject;
    private int spatialObjectHash;
}
