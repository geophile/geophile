package com.geophile.z.spatialobject.jts;

import com.geophile.z.Space;
import com.geophile.z.SpatialObject;
import com.geophile.z.SpatialObjectException;
import com.geophile.z.space.Region;
import com.geophile.z.space.RegionComparison;
import com.geophile.z.space.SpaceImpl;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

import java.nio.ByteBuffer;

public abstract class JTSBase implements SpatialObject
{
    // Object interface

    @Override
    public final String toString()
    {
        return geometry.toString();
    }

    @Override
    public final int hashCode()
    {
        if (!hashCodeKnown) {
            hashCode = geometry.getEnvelopeInternal().hashCode();
            hashCodeKnown = true;
        }
        return hashCode;
    }

    @Override
    public final boolean equals(Object obj)
    {
        // Relies on Geometry.equals(Geometry), which does not override Object.equals(Object).
        return this == obj || obj != null && obj instanceof JTSBase && geometry.equals(((JTSBase)obj).geometry);
    }

    // SpatialObject interface

    @Override
    public final void id(long id)
    {
        this.id = id;
    }

    @Override
    public final long id()
    {
        return id;
    }

    @Override
    public abstract double[] arbitraryPoint();

    @Override
    public int maxZ()
    {
        return MAX_Z;
    }

    @Override
    public abstract boolean equalTo(SpatialObject that);

    @Override
    public abstract boolean containedBy(Region region);

    @Override
    public abstract RegionComparison compare(Region region);

    @Override
    public final void readFrom(ByteBuffer buffer)
    {
        read(buffer);
    }

    @Override
    public void writeTo(ByteBuffer buffer)
    {
        write(buffer);
    }

    // JTSBase interface

    public final Geometry geometry()
    {
        return geometry;
    }

    // For use by subclasses

    protected void ensureGeometry()
    {
        if (geometry == null) {
            assert wkb != null;
            try {
                geometry = io().reader().read(wkb);
            } catch (ParseException e) {
                throw new SpatialObjectException(e);
            }
        }
    }

    protected JTSBase(Space space, Geometry geometry)
    {
        this.space = (SpaceImpl) space;
        this.geometry = geometry;
    }

    protected void read(ByteBuffer input)
    {
        // WKB
        int size = input.getInt();
        wkb = new byte[size];
        input.get(wkb);
        // geometry
        geometry = null;
    }

    protected void write(ByteBuffer output)
    {
        // WKB
        ensureWKB();
        output.putInt(wkb.length);
        output.put(wkb);
        // geometry: nothing to do
    }

    // For use by this class

    private IO io()
    {
        return THREAD_IO.get();
    }

    private void ensureWKB()
    {
        if (wkb == null) {
            assert geometry != null;
            wkb = io().writer().write(geometry);
        }
    }

    // Class state

    private static final int MAX_Z = 8;
    private static final ThreadLocal<IO> THREAD_IO =
        new ThreadLocal<IO>()
        {
            @Override
            protected IO initialValue()
            {
                return new IO();
            }
        };

    // Object state

    private long id;
    // Caching of hashCode
    private boolean hashCodeKnown = false;
    private int hashCode;
    //
    protected SpaceImpl space;
    protected Geometry geometry;
    // Well Known Binary representation, (i.e., serialized)
    private byte[] wkb;
    // Derivation of state (bounding boxes handled by subclass JTSBaseWithBoundingBox):
    // - New spatial object:
    //    - Start with geometry
    //    - Generate bounding box on addition to index.
    //    - Generate wkb on durability.
    // - Read spatial object from index:
    //    - Start with bounding box and wkb.
    //    - Generate geometry lazily.

    // Inner classes
    
    private static class IO
    {
        WKBReader reader()
        {
            if (reader == null) {
                reader = new WKBReader();
            }
            return reader;
        }

        WKBWriter writer()
        {
            if (writer == null) {
                writer = new WKBWriter();
            }
            return writer;
        }

        private WKBReader reader;
        private WKBWriter writer;
    }
}
