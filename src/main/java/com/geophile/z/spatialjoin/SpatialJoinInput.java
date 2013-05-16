package com.geophile.z.spatialjoin;

import com.geophile.z.SpatialIndex;
import com.geophile.z.index.Cursor;
import com.geophile.z.index.Record;
import com.geophile.z.index.SpatialObjectKey;
import com.geophile.z.space.SpaceImpl;
import com.geophile.z.space.SpatialIndexImpl;
import com.geophile.z.spatialobject.SpatialObject;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.geophile.z.space.SpaceImpl.formatZ;

// This class implements the spatial join algorithm described in Orenstein & Manola,
// IEEE Transactions on Software Engineering 14 (5) 1988.

class SpatialJoinInput<THIS_SPATIAL_OBJECT extends SpatialObject, THAT_SPATIAL_OBJECT extends SpatialObject>
{
    @Override
    public String toString()
    {
        return name;
    }

    public long nextEntry()
    {
        return nextToEnter.eof() ? EOF : space.zLo(nextToEnter.key().z());
    }

    public long nextExit()
    {
        return nest.isEmpty() ? EOF : space.zHi(nest.peek().key().z());
    }

    public void enterRegion()
    {
        assert !nextToEnter.eof();
        fixNest();
        advanceCursor();
        log("enter");
    }

    public void exitRegion()
    {
        assert !nest.isEmpty();
        THIS_SPATIAL_OBJECT thisSpatialObject = nest.pop().spatialObject();
        for (Record<THAT_SPATIAL_OBJECT> thatRecord : that.nest) {
            spatialJoinOutput.add(thisSpatialObject, thatRecord.spatialObject());
        }
        log("exit");
    }

    public void otherInput(SpatialJoinInput<THAT_SPATIAL_OBJECT, THIS_SPATIAL_OBJECT> that)
    {
        this.that = that;
    }

    public SpatialJoinInput(SpatialIndex<THIS_SPATIAL_OBJECT> spatialIndex,
                            SpatialJoinOutput<THIS_SPATIAL_OBJECT, THAT_SPATIAL_OBJECT> spatialJoinOutput)
    {
        this.space = (SpaceImpl) spatialIndex.space();
        this.cursor = ((SpatialIndexImpl<THIS_SPATIAL_OBJECT>)spatialIndex).index().cursor(Long.MIN_VALUE);
        this.cursor.next().copyTo(this.nextToEnter);
        this.spatialJoinOutput = spatialJoinOutput;
        log("initialize");
    }

    private void advanceCursor()
    {
        // The referenced figures 3.9a-e are from the paper cited above.
        cursor.next().copyTo(nextToEnter);
        if (nextToEnter.eof()) {
            // Can't go any farther
        } else if (!that.nest.isEmpty() && space.contains(that.nest.getLast().key().z(), nextToEnter.key().z())) {
            // Figure 3.9 a, b: nextToEnter's z-value is contained by the largest z-value in that's nest.
            // nextToEnter is correct -- can't skip ahead because it will generate join output.
        } else {
            if (that.nextToEnter.eof()) {
                nextToEnter.setEOF();
            } else {
                // Use that's nextToEnter to skip ahead, but only if that z-value > nextToEnter. Otherwise, we could end
                // up at nextToEnter or even behind it.
                long zThat = that.nextToEnter.key().z();
                if (zThat > nextToEnter.key().z()) {
                    cursor.goTo(SpatialObjectKey.keyLowerBound(space.zLo(zThat)));
                    cursor.next().copyTo(nextToEnter);
                    if (!nextToEnter.eof() && space.contains(zThat, nextToEnter.key().z())) {
                        // Figure 3.9c. The published algorithm seems to have a bug! In 3.9c, the z-value in this
                        // located by random access of zThat may have other z-values containing it, as in 3.9d.
                        findAncestorToResume();
                    } else {
                        // zThat may be contained by the z-value in this immediately preceding nextToEnter,
                        // or some ancestor thereof. If such a z-value exists, resume there, otherwise resume
                        // at nextToEnter. (The preceding z-value cannot be contained by zThat, or the random
                        // access would have placed us there, and we'd have 3.9c).
                        if (nextToEnter.eof()) {
                            // The z-value immediately preceding nextToEnter is the last z-value in this.
                            cursor.goTo(SpatialObjectKey.keyLowerBound(Long.MAX_VALUE));
                        }
                        cursor.previous().copyTo(nextToEnter);
                        if (space.contains(nextToEnter.key().z(), zThat)) {
                            // Figure 3.9d.
                            findAncestorToResume();
                        } else {
                            // Figure 3.9e. Stepping back was wrong.
                            cursor.next().copyTo(nextToEnter);
                        }
                    }
                }
            }
        }
    }

    private void findAncestorToResume()
    {
        // Find the largest ancestor of nextToEnter that exists and that is past the nest top's z-value.
        long zLowerBound = space.zHi(nest.peek().key().z());
        long zCandidate = space.parent(nextToEnter.key().z());
        while (zCandidate > zLowerBound) {
            SpatialObjectKey key = SpatialObjectKey.keyLowerBound(zCandidate);
            cursor.goTo(key);
            Record<THIS_SPATIAL_OBJECT> ancestor = cursor.next();
            if (!ancestor.eof() && ancestor.key().z() == zCandidate) {
                ancestor.copyTo(nextToEnter);
            }
            zCandidate = space.parent(zCandidate);
        }
        // Resume at nextToEnter
        cursor.goTo(nextToEnter.key());
        cursor.next().copyTo(nextToEnter);
    }

    private void fixNest()
    {
        if (!nextToEnter.eof()) {
            long z = nextToEnter.key().z();
            Record<THIS_SPATIAL_OBJECT> top;
            while ((top = nest.peek()) != null && !space.contains(top.key().z(), z)) {
                nest.pop();
            }
            Record<THIS_SPATIAL_OBJECT> copy = new Record<>();
            nextToEnter.copyTo(copy);
            nest.push(copy);
        }
    }

    private void log(String label)
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
                nextToEnter.eof()
                ? "eof"
                : formatZ(nextToEnter.key().z());
            LOG.log(Level.FINE,
                    "{0} {1}: nest:{2}, nextToEnter: {3}",
                    new Object[]{this, label, buffer.toString(), nextZ});
        }
    }

    // Class state

    private static final Logger LOG = Logger.getLogger(SpatialJoinInput.class.getName());
    private static final AtomicInteger idGenerator = new AtomicInteger(0);

    // Object state

    // A 64-bit z-value is definitely less than Long.MAX_VALUE. The maxium z-value length is 57, which is recorded
    // in a 6-bit length field. So the length field will always have some zeros, while Long.MAX_VALUE won't (in those
    // last six bits).
    public static long EOF = Long.MAX_VALUE;

    private final String name = String.format("sjinput(%s)",idGenerator.getAndIncrement());
    private final SpaceImpl space;
    private SpatialJoinInput<THAT_SPATIAL_OBJECT, THIS_SPATIAL_OBJECT> that;
    private final SpatialJoinOutput<THIS_SPATIAL_OBJECT, THAT_SPATIAL_OBJECT> spatialJoinOutput;
    // nest contains z-values that have been entered but not exited. nextToEnter is the next z-value to enter,
    // and cursor contains later z-values.
    private final Deque<Record<THIS_SPATIAL_OBJECT>> nest = new ArrayDeque<>();
    private final Cursor<THIS_SPATIAL_OBJECT> cursor;
    private final Record<THIS_SPATIAL_OBJECT> nextToEnter = new Record<>();
}
