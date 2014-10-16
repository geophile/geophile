package com.geophile.z.spatialobject.jts;

import com.geophile.z.Space;
import com.geophile.z.SpatialObject;
import com.geophile.z.SpatialObjectException;
import com.geophile.z.space.Region;
import com.geophile.z.space.RegionComparison;
import com.geophile.z.space.SpaceImpl;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

import java.nio.ByteBuffer;

public abstract class JTSSpatialObject implements SpatialObject
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
        boolean eq = this == obj;
        if (!eq && obj != null && obj instanceof JTSSpatialObject) {
            JTSSpatialObject that = (JTSSpatialObject) obj;
            this.ensureGeometry();
            that.ensureGeometry();
            eq = this.geometry.equals(that.geometry);
        }
        return eq;
    }

    // SpatialObject interface

    @Override
    public final double[] arbitraryPoint()
    {
        double[] point = new double[2];
        Coordinate coordinate = geometry.getCoordinate();
        point[0] = coordinate.x;
        point[1] = coordinate.y;
        return point;
    }


    @Override
    public int maxZ()
    {
        return MAX_Z;
    }

    @Override
    public abstract boolean containedBy(Space space);

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

    protected JTSSpatialObject(Space space, Geometry geometry)
    {
        this.space = (SpaceImpl) space;
        this.geometry = geometry;
    }

    // Used during deserialization
    protected JTSSpatialObject()
    {
        this.space = null;
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

    protected final SpaceImpl space;
    // Caching of hashCode
    private boolean hashCodeKnown = false;
    private int hashCode;
    //
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
