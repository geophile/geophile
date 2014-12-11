/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.examples;

import com.geophile.z.Record;
import com.geophile.z.SpatialObject;
import com.geophile.z.index.RecordWithSpatialObject;

import java.util.Comparator;

public class ExampleRecord extends RecordWithSpatialObject
{
    @Override
    public boolean equals(Object obj)
    {
        boolean eq = false;
        if (obj != null && obj instanceof ExampleRecord) {
            ExampleRecord that = (ExampleRecord) obj;
            eq = super.equals(that) && this.id == that.id;
        }
        return eq;
    }

    @Override
    public void copyTo(Record record)
    {
        throw new UnsupportedOperationException();
    }

    public ExampleRecord(SpatialObject spatialObject, int id)
    {
        spatialObject(spatialObject);
        this.id = id;
    }

    public ExampleRecord()
    {}

    public static final Comparator<ExampleRecord> COMPARATOR =
        new Comparator<ExampleRecord>()
        {
            @Override
            public int compare(ExampleRecord r, ExampleRecord s)
            {
                return
                    r.z() < s.z()
                    ? -1
                    : r.z() > s.z()
                      ? 1
                      : r.id < s.id
                        ? -1
                        : r.id > s.id
                          ? 1
                          : 0;
            }
        };

    private int id;

    // Inner classes

    public static class Factory implements Record.Factory<ExampleRecord>
    {
        @Override
        public ExampleRecord newRecord()
        {
            return new ExampleRecord(spatialObject, id);
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
