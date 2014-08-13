package com.geophile.z;

import com.geophile.z.spatialobject.SerializerImpl;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

/**
 * A Serializer implements serialization of {@link com.geophile.z.SpatialObject}s.
 */

public abstract class SpatialObjectSerializer
{
    /**
     * Serializes the given SpatialObject into the given ByteBuffer.
     * @param spatialObject The SpatialObject to be serialized.
     * @param buffer The buffer to store the serialized SpatialObject.
     * @throws BufferOverflowException buffer is not large enough to accomodate the serialized SpatialObject.
     */
    public abstract void serialize(SpatialObject spatialObject, ByteBuffer buffer) throws BufferOverflowException;

    /**
     * Deserialize the contents of the given ByteBuffer, returning the resulting SpatialObject.
     * @param buffer ByteBuffer containing the serialized SpatialObject.
     * @return The deserialized SpatialObject.
     */
    public abstract SpatialObject deserialize(ByteBuffer buffer);

    /**
     * Associate a SpatialObject subclass with a type id.
     * @param typeId Type id of the given SpatialObject subclass.
     * @param spatialObjectClass A SpatialObject subclass.
     */
    public abstract void register(int typeId, Class<? extends SpatialObject> spatialObjectClass);

    /**
     * Create a new Serializer.
     * @return A new Serializer.
     */
    public static SpatialObjectSerializer newSerializer()
    {
        return new SerializerImpl();
    }
}
