/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.space;

import com.geophile.z.RecordFilter;
import com.geophile.z.SpatialObject;
import com.geophile.z.index.Record;

class RemovalFilter implements RecordFilter
{
    @Override
    public boolean select(Record record)
    {
        return record.spatialObject().equalTo(target);
    }

    public void spatialObject(SpatialObject target)
    {
        this.target = target;
    }

    private SpatialObject target;
}
