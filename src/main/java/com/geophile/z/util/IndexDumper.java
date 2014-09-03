/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.util;

import com.geophile.z.Cursor;
import com.geophile.z.Index;
import com.geophile.z.Record;

import java.io.IOException;
import java.io.PrintStream;

public class IndexDumper<RECORD extends Record>
{
    public void dump(PrintStream output) throws IOException, InterruptedException
    {
        Cursor<RECORD> cursor = index.cursor();
        RECORD start = index.newKeyRecord();
        start.z(0);
        cursor.goTo(start);
        RECORD record;
        while ((record = cursor.next()) != null) {
            output.println(describe(record));
        }
    }

    public String describe(RECORD record)
    {
        return record.toString();
    }

    public IndexDumper(Index<RECORD> index)
    {
        this.index = index;
    }

    private final Index<RECORD> index;
}
