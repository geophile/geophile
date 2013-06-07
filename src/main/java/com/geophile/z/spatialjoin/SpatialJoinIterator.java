package com.geophile.z.spatialjoin;

import com.geophile.z.Pair;
import com.geophile.z.SpatialJoinRuntimeException;
import com.geophile.z.SpatialObject;
import com.geophile.z.space.SpatialIndexImpl;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

class SpatialJoinIterator<LEFT extends SpatialObject, RIGHT extends SpatialObject>
    implements Iterator<Pair<LEFT, RIGHT>>
{
    // Object interface

    @Override
    public String toString()
    {
        return name;
    }


    // Iterator interface

    @Override
    public boolean hasNext()
    {
        ensurePending();
        return !pending.isEmpty();
    }

    @Override
    public Pair<LEFT, RIGHT> next()
    {
        Pair<LEFT, RIGHT> next;
        ensurePending();
        if (pending.isEmpty()) {
            throw new NoSuchElementException();
        }
        next = pending.poll();
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "{0} -> {1}", new Object[]{this, next});
        }
        return next;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }


    // SpatialJoinIterator interface

    public SpatialJoinIterator(SpatialIndexImpl<LEFT> leftSpatialIndex,
                               SpatialIndexImpl<RIGHT> rightSpatialIndex,
                               final SpatialJoinFilter<LEFT, RIGHT> filter) throws IOException, InterruptedException
    {
        SpatialJoinOutput<LEFT, RIGHT> pendingLeftRight =
            new SpatialJoinOutput<LEFT, RIGHT>()
            {
                @Override
                public void add(LEFT left, RIGHT right)
                {
                    if (filter.overlap(left, right)) {
                        pending.add(new Pair<>(left, right));
                    }
                }
            };
        left = SpatialJoinInput.newSpatialJoinInput(leftSpatialIndex, pendingLeftRight);
        SpatialJoinOutput<RIGHT, LEFT> pendingRightLeft =
            new SpatialJoinOutput<RIGHT, LEFT>()
            {
                @Override
                public void add(RIGHT right, LEFT left)
                {
                    if (filter.overlap(left, right)) {
                        pending.add(new Pair<>(left, right));
                    }
                }
            };
        right = SpatialJoinInput.newSpatialJoinInput(rightSpatialIndex, pendingRightLeft);
        left.otherInput(right);
        right.otherInput(left);
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO,
                    "SpatialJoinIterator {0}: {1} x {2}",
                    new Object[]{this, left, right});
        }
        findPairs();
    }

    // For use by this class

    private void ensurePending()
    {
        if (pending.isEmpty()) {
            try {
                findPairs();
            } catch (IOException | InterruptedException e) {
                throw new SpatialJoinRuntimeException(e);
            }
        }
    }

    private void findPairs() throws IOException, InterruptedException
    {
        assert pending.isEmpty();
        long zMin;
        do {
            long zLeftEnter = left.nextEntry();
            long zLeftExit = left.nextExit();
            long zRightEnter = right.nextEntry();
            long zRightExit = right.nextExit();
            zMin = min(zLeftEnter, zLeftExit, zRightEnter, zRightExit);
            if (zMin < SpatialJoinInput.EOF) {
                // Prefer entry to exit to avoid missing join output
                if (zMin == zLeftEnter) {
                    left.enterZ();
                } else if (zMin == zRightEnter) {
                    right.enterZ();
                } else if (zMin == zLeftExit) {
                    left.exitZ();
                } else {
                    right.exitZ();
                }
            }
        } while (pending.isEmpty() && zMin < SpatialJoinInput.EOF);
    }

    private long min(long a, long b, long c, long d)
    {
        long minAB = a < b ? a : b;
        long minCD = c < d ? c : d;
        return minAB < minCD ? minAB : minCD;
    }

    // Class state

    private static final Logger LOG = Logger.getLogger(SpatialJoinIterator.class.getName());
    private static final AtomicInteger idGenerator = new AtomicInteger(0);

    // Object state

    private final String name = String.format("sj(%s)", idGenerator.getAndIncrement());
    private final SpatialJoinInput<LEFT, RIGHT> left;
    private final SpatialJoinInput<RIGHT, LEFT> right;
    private final Queue<Pair<LEFT, RIGHT>> pending = new ArrayDeque<>();
}
