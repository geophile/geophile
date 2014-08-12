/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

import com.geophile.z.index.BaseRecord;

public class TestRecord extends BaseRecord
{
    // Object interface

    @Override
    public String toString()
    {
        return String.format("(%s: %s)", soid, super.toString());
    }

    @Override
    public int hashCode()
    {
        return super.hashCode() ^ soid;
    }

    @Override
    public boolean equals(Object obj)
    {
        boolean eq = false;
        if (super.equals(obj) && obj instanceof TestRecord) {
            TestRecord that = (TestRecord) obj;
            eq = this.soid == that.soid;
        }
        return eq;
    }

    // Record interface

    @Override
    public void copyTo(Record record)
    {
        super.copyTo(record);
        ((TestRecord)record).soid = this.soid;
    }

    @Override
    public int keyCompare(Record record)
    {
        int c = super.keyCompare(record);
        if (c == 0) {
            TestRecord that = (TestRecord) record;
            c = this.soid - that.soid;
        }
        return c;
    }

    @Override
    public int keyHash()
    {
        return soid * 9987001;
    }

    // TestRecord interface

    public int soid()
    {
        return soid;
    }

    public void soid(int newId)
    {
        soid = newId;
    }

    public TestRecord(SpatialObject spatialObject)
    {
        spatialObject(spatialObject);
        soid = 0;
    }

    public TestRecord(SpatialObject spatialObject, int soid)
    {
        spatialObject(spatialObject);
        this.soid = soid;
    }

    public TestRecord()
    {}

    private static final int UNDEFINED_SOID = -1;

    private int soid = UNDEFINED_SOID;
}
