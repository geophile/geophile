/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialjoin;

import com.geophile.z.SpatialObject;
import com.geophile.z.index.Record;
import com.geophile.z.index.SpatialObjectKey;
import com.geophile.z.space.SpaceImpl;
import com.geophile.z.space.SpatialIndexImpl;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.logging.Level;

import static com.geophile.z.space.SpaceImpl.formatZ;

class SpatialJoinInputMultiCell<THIS_SPATIAL_OBJECT extends SpatialObject, THAT_SPATIAL_OBJECT extends SpatialObject>
    extends SpatialJoinInput<THIS_SPATIAL_OBJECT, THAT_SPATIAL_OBJECT>
{
    // SpatialJoinInput interface

    @Override
    public long nextEntry()
    {
        return current.eof() ? EOF : SpaceImpl.zLo(current.key().z());
    }

    @Override
    public long nextExit()
    {
        return nest.isEmpty() ? EOF : SpaceImpl.zHi(nest.peek().key().z());
    }

    @Override
    public void enterZ() throws IOException, InterruptedException
    {
        assert !current.eof();
        if (currentOverlapsOtherNest() ||
            !that.current.eof() && overlap(current.key().z(), that.current.key().z())) {
            // Enter current
            if (!nest.isEmpty()) {
                long topZ = nest.peek().key().z();
                assert SpaceImpl.contains(topZ, current.key().z());
            }
            Record<THIS_SPATIAL_OBJECT> currentCopy = new Record<>();
            current.copyTo(currentCopy);
            nest.push(currentCopy);
            cursor.next().copyTo(current);
        } else {
            advanceCursor();
        }
        counters.countEnterZ();
        log("enter");
    }

    @Override
    public void exitZ()
    {
        assert !nest.isEmpty();
        Record<THIS_SPATIAL_OBJECT> top = nest.pop();
        that.generateSpatialJoinOutput(top.spatialObject());
        log("exit");
    }

    @Override
    public Record<THIS_SPATIAL_OBJECT> nestTop()
    {
        return nest.peek();
    }

    @Override
    public Record<THIS_SPATIAL_OBJECT> nestBottom()
    {
        return nest.peekLast();
    }

    @Override
    public void generateSpatialJoinOutput(THAT_SPATIAL_OBJECT thatSpatialObject)
    {
        for (Record<THIS_SPATIAL_OBJECT> thisRecord : nest) {
            spatialJoinOutput.add(thisRecord.spatialObject(), thatSpatialObject);
        }
    }

    @Override
    protected void advanceCursor() throws IOException, InterruptedException
    {
        // Use that.current to skip ahead
        if (that.current.eof()) {
            // If that.current is EOF, then we can skip to the end on this side too.
            current.setEOF();
        } else {
            assert !current.eof(); // Should have been checked in caller, but just to be sure.
            long thisCurrentZ = this.current.key().z();
            long thatCurrentZ = that.current.key().z();
            assert thatCurrentZ >= thisCurrentZ; // otherwise, we would have entered that.current
            if (thatCurrentZ > thisCurrentZ) {
                cursor.goTo(SpatialObjectKey.keyLowerBound(thatCurrentZ));
                cursor.next().copyTo(current);
                if (!singleCellOptimization || !singleCell) {
                    if (!current.eof() && SpaceImpl.contains(thatCurrentZ, current.key().z())) {
                        // that.current contains this.current. Find the largest ancestor.
                        findAncestorToResumeWithoutCache(current.key().z(), thisCurrentZ);
                    } else {
                        // that.current does not contain this.current. Look for a z-value in this containing
                        // that.current.
                        findAncestorToResumeWithoutCache(thatCurrentZ, thisCurrentZ);
                    }
                }
                // else: No ancestor z-values in a single-cell index
            }
        }
    }

    @Override
    protected void log(String label)
    {
        if (LOG.isLoggable(Level.FINE)) {
            StringBuilder buffer = new StringBuilder();
            Iterator<Record<THIS_SPATIAL_OBJECT>> nestScan = nest.descendingIterator();
            while (nestScan.hasNext()) {
                Record<THIS_SPATIAL_OBJECT> record = nestScan.next();
                buffer.append(' ');
                buffer.append(formatZ(record.key().z()));
            }
            String nextZ =
                current.eof()
                ? "eof"
                : formatZ(current.key().z());
            LOG.log(Level.FINE,
                    "{0} {1}: nest:{2}, current: {3}",
                    new Object[]{this, label, buffer.toString(), nextZ});
        }
    }

    // For use by this package

    SpatialJoinInputMultiCell(SpatialIndexImpl<THIS_SPATIAL_OBJECT> spatialIndex,
                              SpatialJoinOutput<THIS_SPATIAL_OBJECT, THAT_SPATIAL_OBJECT> spatialJoinOutput)
        throws IOException, InterruptedException
    {
        super(spatialIndex, spatialJoinOutput);
        this.singleCell = spatialIndex.singleCell();
        this.singleCellOptimization = SpatialJoinImpl.singleCellOptimization();
    }

    // For use by this class

    private void findAncestorToResumeWithoutCache(long zStart, long zLowerBound)
        throws IOException, InterruptedException
    {
        // Find the largest ancestor of current that exists and that is past zLowerBound.
        long zCandidate = SpaceImpl.parent(zStart);
        while (zCandidate > zLowerBound) {
            SpatialObjectKey key = SpatialObjectKey.keyLowerBound(zCandidate);
            cursor.goTo(key);
            Record<THIS_SPATIAL_OBJECT> ancestor = cursor.next();
            if (!ancestor.eof() && ancestor.key().z() == zCandidate) {
                ancestor.copyTo(current);
            }
            zCandidate = SpaceImpl.parent(zCandidate);
        }
        // Resume at current
        if (current.eof()) {
            cursor.close();
        } else {
            assert current.key().z() >= zLowerBound;
            cursor.goTo(current.key());
            cursor.next().copyTo(current);
        }
    }

    private boolean currentOverlapsOtherNest()
    {
        boolean overlap = false;
        Record<THAT_SPATIAL_OBJECT> thatNestTop = that.nestTop();
        if (thatNestTop != null) {
            long thisCurrentZ = current.key().z();
            overlap =
                SpaceImpl.contains(thisCurrentZ, thatNestTop.key().z()) ||
                SpaceImpl.contains(that.nestBottom().key().z(), thisCurrentZ);
        }
        return overlap;
    }

    // Object state

    private final boolean singleCell;
    // nest contains z-values that have been entered but not exited. current is the next z-value to enter,
    // and cursor contains later z-values.
    private final Deque<Record<THIS_SPATIAL_OBJECT>> nest = new ArrayDeque<>();
    private final Counters counters = Counters.forThread();
    private final boolean singleCellOptimization;
}
