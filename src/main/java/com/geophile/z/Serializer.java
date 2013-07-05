package com.geophile.z;

import com.geophile.z.spatialobject.SerializerImpl;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

// Serializer is not type-safe. Each SpatialIndex has a serializer, and it is expected that type registration
// will be done in a single thread.

public abstract class Serializer
{
    public abstract void serialize(SpatialObject spatialObject, ByteBuffer buffer) throws BufferOverflowException;

    public abstract SpatialObject deserialize(ByteBuffer buffer);

    public abstract void register(int typeId, Class<? extends SpatialObject> spatialObjectClass);

    public static Serializer newSerializer()
    {
        return new SerializerImpl();
    }
}
