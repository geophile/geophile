package com.geophile.z;

import com.geophile.z.spatialobject.SerializerImpl;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

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
