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

package com.geophile.z.index.sortedarray;

import com.geophile.z.Index;
import com.geophile.z.TestRecord;
import com.geophile.z.index.IndexTestBase;

public class SortedArrayTest extends IndexTestBase
{
    @Override
    protected Index<TestRecord> newIndex()
    {
        return
            new SortedArray<TestRecord>()
            {
                @Override
                public TestRecord newRecord()
                {
                    return new TestRecord();
                }
            };
    }
}
