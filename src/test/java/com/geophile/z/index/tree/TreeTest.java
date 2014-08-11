/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index.tree;

import com.geophile.z.Index;
import com.geophile.z.Record;
import com.geophile.z.TestRecord;
import com.geophile.z.index.IndexTestBase;

public class TreeTest extends IndexTestBase
{
    @Override
    protected Index newIndex()
    {
        return
            new TreeIndex()
            {
                @Override
                public Record newRecord()
                {
                    return new TestRecord();
                }
            };
    }
}
