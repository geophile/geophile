/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z;

import com.geophile.z.index.RecordWithSpatialObject;

import java.util.Comparator;

public class TestRecord extends RecordWithSpatialObject
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

    // Class state

    public static final Comparator<TestRecord> COMPARATOR =
        new Comparator<TestRecord>()
        {
            @Override
            public int compare(TestRecord r, TestRecord s)
            {
                return
                    r.z() < s.z()
                    ? -1
                    : r.z() > s.z()
                      ? 1
                      : r.soid < s.soid
                        ? -1
                        : r.soid > s.soid
                          ? 1
                          : 0;
            }
        };

    private static final int UNDEFINED_SOID = -1;

    // Object state

    private int soid = UNDEFINED_SOID;

    // Inner classes

    public static class Factory implements Record.Factory<TestRecord>
    {
        @Override
        public TestRecord newRecord()
        {
            return new TestRecord(spatialObject, id);
        }

        public Factory setup(SpatialObject spatialObject, int id)
        {
            this.spatialObject = spatialObject;
            this.id = id;
            return this;
        }

        private SpatialObject spatialObject;
        private int id;
    }
}
