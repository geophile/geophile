package com.geophile.z.spatialobject.jts;

import com.geophile.z.SpatialObject;
import com.geophile.z.SpatialObjectException;
import com.geophile.z.space.Region;
import com.geophile.z.space.RegionComparison;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class JTSBase implements SpatialObject
{
    // SpatialObject interface

    @Override
    public void id(long id)
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
    public abstract int maxZ();

    @Override
    public abstract boolean equalTo(SpatialObject that);

    @Override
    public abstract boolean containedBy(Region region);

    @Override
    public abstract RegionComparison compare(Region region);

    @Override
    public final void readFrom(ByteBuffer buffer) throws IOException
    {
        read(buffer);
    }

    @Override
    public void writeTo(ByteBuffer buffer) throws IOException
    {
        write(buffer);
    }

    // JTSBase interface

    public final Geometry jtsObject()
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

    protected void ensureBoundingBox()
    {
        if (!boundingBoxAvailable) {
            assert geometry != null;
            Envelope envelope = geometry.getEnvelopeInternal();
            xLo = 0;
            xHi = 0;
            yLo = 0;
            yHi = 0;
/*
            xLo = envelope.getMinX();
            xHi = envelope.getMaxX();
            yLo = envelope.getMinY();
            yHi = envelope.getMaxY();
*/
            boundingBoxAvailable = true;
        }
    }

    protected JTSBase(Geometry geometry)
    {
        this.geometry = geometry;
    }

    // For use by this class

    private IO io()
    {
        return THREAD_IO.get();
    }

    private void read(ByteBuffer input)
    {
        // bounding box
        xLo = input.getLong();
        xHi = input.getLong();
        yLo = input.getLong();
        yHi = input.getLong();
        boundingBoxAvailable = true;
        // WKB
        int size = input.getInt();
        wkb = new byte[size];
        input.get(wkb);
        // geometry
        geometry = null;
    }

    private void write(ByteBuffer output) throws IOException
    {
        // bounding box
        ensureBoundingBox();
        output.putLong(xLo);
        output.putLong(xHi);
        output.putLong(yLo);
        output.putLong(yHi);
        // WKB
        ensureWKB();
        output.putInt(wkb.length);
        output.put(wkb);
        // geometry: nothing to do
    }

    private void ensureWKB()
    {
        if (wkb == null) {
            assert geometry != null;
            wkb = io().writer().write(geometry);
        }
    }

    // Class state

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
    protected Geometry geometry;
    // Bounding box (z space coordinates)
    protected long xLo;
    protected long xHi;
    protected long yLo;
    protected long yHi;
    // Well Known Binary representation, (i.e., serialized)
    private byte[] wkb;
    // Derivation of state:
    // - New spatial object:
    //    - Start with geometry
    //    - Generate bounding box on addition to index.
    //    - Generate wkb on durability.
    // - Read spatial object from index:
    //    - Start with bounding box and wkb.
    //    - Generate geometry lazily.
    // Absence of geometry, wkb indicated by null. For bounding-box, need a boolean variable.
    private boolean boundingBoxAvailable;
    
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
