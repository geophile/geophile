/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index;

import com.geophile.z.Cursor;
import com.geophile.z.DuplicateRecordException;
import com.geophile.z.Index;
import com.geophile.z.Record;
import com.geophile.z.RecordFilter;

import java.io.IOException;

public class KeyTemplateIndex<RECORD extends Record> extends Index<RECORD>
{
    // Index interface

    @Override
    public void add(RECORD record)
        throws IOException, InterruptedException, DuplicateRecordException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(long z, RecordFilter<RECORD> recordFilter) throws IOException, InterruptedException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Cursor<RECORD> cursor() throws IOException, InterruptedException
    {
        return index.cursor();
    }

    @Override
    public RECORD newRecord()
    {
        return index.newRecord();
    }

    @Override
    public RECORD newKeyRecord()
    {
        RECORD keyRecord = index.newRecord();
        keyTemplate.copyTo(keyRecord);
        return keyRecord;
    }

    @Override
    public boolean blindUpdates()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Index<RECORD> restrict(RECORD keyTemplate)
    {
        throw new UnsupportedOperationException();
    }

    // KeyTemplateIndex interface

    public KeyTemplateIndex(Index<RECORD> index, RECORD keyTemplate)
    {
        this.index = index;
        this.keyTemplate = keyTemplate;
    }

    // Object state

    private final Index<RECORD> index;
    private final RECORD keyTemplate;
}
