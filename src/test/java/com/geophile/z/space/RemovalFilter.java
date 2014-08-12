/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.space;

import com.geophile.z.RecordFilter;
import com.geophile.z.SpatialObject;
import com.geophile.z.Record;

class RemovalFilter<RECORD extends Record> implements RecordFilter<RECORD>
{
    @Override
    public boolean select(RECORD record)
    {
        return record.spatialObject().equals(target);
    }

    public void spatialObject(SpatialObject target)
    {
        this.target = target;
    }

    private SpatialObject target;
}
