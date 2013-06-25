/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialobject.jts;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class JTSBaseWithBoundingBox extends JTSBase
{
    // For use by subclasses

    @Override
    protected void read(ByteBuffer input)
    {
        xLo = input.getDouble();
        xHi = input.getDouble();
        yLo = input.getDouble();
        yHi = input.getDouble();
        super.read(input);
    }

    @Override
    protected void write(ByteBuffer output) throws IOException
    {
        ensureBoundingBox();
        output.putDouble(xLo);
        output.putDouble(xHi);
        output.putDouble(yLo);
        output.putDouble(yHi);
        super.write(output);
    }

    protected JTSBaseWithBoundingBox(Geometry geometry)
    {
        super(geometry);
    }

    // For use by this class

    private boolean boundingBoxAvailable()
    {
        return xLo <= xHi;
    }

    protected void ensureBoundingBox()
    {
        if (!boundingBoxAvailable()) {
            assert geometry != null;
            Envelope envelope = geometry.getEnvelopeInternal();
            xLo = envelope.getMinX();
            xHi = envelope.getMaxX();
            yLo = envelope.getMinY();
            yHi = envelope.getMaxY();
            assert boundingBoxAvailable();
        }
    }

    // Object state

    protected double xLo = 0;
    protected double xHi = -1;
    protected double yLo;
    protected double yHi;
}
