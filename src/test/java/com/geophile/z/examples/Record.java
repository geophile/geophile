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

import com.geophile.z.SpatialObject;
import com.geophile.z.index.RecordWithSpatialObject;

import java.util.Comparator;

public class Record extends RecordWithSpatialObject
{
    @Override
    public boolean equals(Object obj)
    {
        boolean eq = false;
        if (obj != null && obj instanceof Record) {
            Record that = (Record) obj;
            eq = this.z() == that.z() && this.id == that.id;
        }
        return eq;
    }

    public int id()
    {
        return id;
    }

    public void id(int newId)
    {
        id = newId;
    }

    public Record(SpatialObject spatialObject)
    {
        spatialObject(spatialObject);
        id = 0;
    }

    public Record(SpatialObject spatialObject, int id)
    {
        spatialObject(spatialObject);
        this.id = id;
    }

    public Record()
    {}

    public static final Comparator<Record> COMPARATOR =
        new Comparator<Record>()
        {
            @Override
            public int compare(Record r, Record s)
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
}
