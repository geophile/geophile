/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialobject;

import com.geophile.z.SpatialObject;
import com.geophile.z.SpatialObjectException;
import com.geophile.z.SpatialObjectSerializer;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

// Serializer is not type-safe. Each SpatialIndex has a serializer, and it is expected that type registration
// will be done in a single thread.

public class SerializerImpl extends SpatialObjectSerializer implements Serializable
{
    // Serializer interface

    @Override
    public void serialize(SpatialObject spatialObject, ByteBuffer buffer)
    {
        Class<? extends SpatialObject> klass = spatialObject.getClass();
        Integer typeId = classToTypeId.get(klass);
        if (typeId == null) {
            throw new SpatialObjectException(String.format("Unknown spatial object class %s", klass));
        }
        buffer.putInt(typeId);
        spatialObject.writeTo(buffer);
    }

    @Override
    public SpatialObject deserialize(ByteBuffer buffer)
    {
        int typeId = buffer.getInt();
        Class<? extends SpatialObject> klass = typeIdToClass.get(typeId);
        if (klass == null) {
            throw new SpatialObjectException(String.format("Unknown type id %s", typeId));
        }
        try {
            SpatialObject spatialObject = klass.newInstance();
            spatialObject.readFrom(buffer);
            return spatialObject;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new SpatialObjectException(e);
        }
    }

    @Override
    public void register(int typeId, Class<? extends SpatialObject> spatialObjectClass)
    {
        assert typeId >= 0;
        Class<? extends SpatialObject> klass = typeIdToClass.get(typeId);
        if (klass == null) {
            typeIdToClass.put(typeId, spatialObjectClass);
            Integer replaced = classToTypeId.put(spatialObjectClass, typeId);
            assert replaced == null;
        }
    }

    // SerializerImpl interface

    public SerializerImpl()
    {}

    // Object state

    private final Map<Integer, Class<? extends SpatialObject>> typeIdToClass = new HashMap<>();
    private final Map<Class<? extends SpatialObject>, Integer> classToTypeId = new HashMap<>();
}
