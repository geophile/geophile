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

package com.geophile.z;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class SerializedRecord extends TestRecord
{
    // Object interface


    @Override
    public String toString()
    {
        ensureFieldsUsable();
        return super.toString();
    }

    @Override
    public int hashCode()
    {
        ensureFieldsUsable();
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        ensureFieldsUsable();
        return super.equals(obj);
    }

    // Record interface

    @Override
    public long z()
    {
        ensureFieldsUsable();
        return super.z();
    }

    @Override
    public void z(long newZ)
    {
        super.z(newZ);
        markSerializedNotOK();
    }

    @Override
    public void copyTo(Record record)
    {
        SerializedRecord target = (SerializedRecord) record;
        assert target.serializer == serializer;
        if (this.bufferUsable) {
            target.buffer = ByteBuffer.allocate(buffer.capacity());
            target.buffer.put(buffer.array(), 0, buffer.capacity());
            target.buffer.position(buffer.position());
            target.buffer.limit(buffer.limit());
            target.bufferUsable = true;
        }
        if (this.fieldsUsable) {
            super.copyTo(record);
            target.fieldsUsable = true;
        }
    }

    // RecordWithSpatialObject interface

    @Override
    public SpatialObject spatialObject()
    {
        ensureFieldsUsable();
        return super.spatialObject();
    }

    @Override
    public void spatialObject(SpatialObject spatialObject)
    {
        super.spatialObject(spatialObject);
        markSerializedNotOK();
    }

    // TestRecord interface

    @Override
    public int soid()
    {
        ensureFieldsUsable();
        return super.soid();
    }

    @Override
    public void soid(int newId)
    {
        super.soid(newId);
        markSerializedNotOK();
    }

    // SerializedRecord interface

    public void serialize()
    {
        ensureBufferUsable();
        markRecordNotOK();
    }

    public SerializedRecord(SpatialObjectSerializer serializer, long z, SpatialObject spatialObject, int soid)
    {
        super(spatialObject, soid);
        this.serializer = serializer;
        z(z);
        fieldsUsable = true;
        bufferUsable = false;
    }

    public SerializedRecord(SpatialObjectSerializer serializer)
    {
        this.serializer = serializer;
        this.fieldsUsable = true;
        this.bufferUsable = false;
    }

    // For use by this class

    private void markSerializedNotOK()
    {
        bufferUsable = false;
    }

    private void markRecordNotOK()
    {
        fieldsUsable = false;
    }

    private void ensureFieldsUsable()
    {
        if (!fieldsUsable) {
            z(buffer.getLong());
            spatialObject(serializer.deserialize(buffer));
            soid(buffer.getInt());
            fieldsUsable = true;
        }
    }

    private void ensureBufferUsable()
    {
        if (!bufferUsable) {
            if (buffer == null) {
                buffer = ByteBuffer.allocate(INITIAL_BUFFER_SIZE);
            }
            while (!bufferUsable) {
                try {
                    buffer.putLong(z());
                    serializer.serialize(spatialObject(), buffer);
                    buffer.putInt(soid());
                    bufferUsable = true;
                    buffer.flip();
                } catch (BufferOverflowException e) {
                    buffer = ByteBuffer.allocate(buffer.capacity() * 2);
                }
            }
        }
    }

    private static final int INITIAL_BUFFER_SIZE = 100;

    private final SpatialObjectSerializer serializer;
    private ByteBuffer buffer = null;
    private boolean fieldsUsable = false;
    private boolean bufferUsable = false;
}
