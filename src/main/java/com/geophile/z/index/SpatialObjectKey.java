/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.index;

public class SpatialObjectKey implements Comparable<SpatialObjectKey>
{
    // Object interface

    @Override
    public String toString()
    {
        return String.format("(z: 0x%016x, soid: %s)", z, soid);
    }

    @Override
    public int hashCode()
    {
        return (int) ((z * 9987001) ^ soid);
    }

    @Override
    public boolean equals(Object spatialObject)
    {
        boolean eq;
        if (spatialObject == null || spatialObject.getClass() != SpatialObjectKey.class) {
            eq = false;
        } else {
            SpatialObjectKey that = (SpatialObjectKey) spatialObject;
            eq = this.z == that.z && this.soid == that.soid;
        }
        return eq;
    }

    // Comparable interface

    @Override
    public int compareTo(SpatialObjectKey that)
    {
        return
            this.z < that.z
            ? -1
            : this.z > that.z
              ? 1
              : this.soid < that.soid
                ? -1
                : this.soid > that.soid
                  ? 1
                  : 0;
    }

    // SpatialObjectKey interface

    public long z()
    {
        return z;
    }

    public long soid()
    {
        return soid;
    }

    public static SpatialObjectKey key(long z, long soid)
    {
        if (soid < 0) {
            throw new IllegalArgumentException(String.format("SpatialObject id must be non-negative: %s", soid));
        }
        return new SpatialObjectKey(z, soid);
    }

    public static SpatialObjectKey keyLowerBound(long z)
    {
        return new SpatialObjectKey(z, Long.MIN_VALUE);
    }

    // For use by this class

    private SpatialObjectKey(long z, long soid)
    {
        this.z = z;
        this.soid = soid;
    }

    // Object state

    private final long z;
    private final long soid; // spatial object id
}
