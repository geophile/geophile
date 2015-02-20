package com.geophile.z.spatialjoin;

import com.geophile.z.Cursor;
import com.geophile.z.Index;
import com.geophile.z.Record;
import com.geophile.z.SpatialJoin;
import com.geophile.z.space.SpaceImpl;
import com.geophile.z.space.SpatialIndexImpl;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.geophile.z.space.SpaceImpl.formatZ;

/*

This class implements the spatial join algorithm similar to Orenstein & Manola,
IEEE Transactions on Software Engineering 14 (5) 1988. That algorithm has some bugs, which
are corrected here:

- Not describing how the spatial join ends

- Figure 3.9a, b don't consider cases in which Y.nest is contained by X.current.

- Figure 3.9c doesn't consider the possibility that the next relevant X z-value may be a container of Y.current,
  as in figure 3.9d.

- Figure 3.9d doesn't allow for a container of the rightmost X z-value, (the one labeled "but random access
  locates this one") containing Y.current.

The following writeup is intended to be a more complete and correct description:

Spatial Join Optimization

Spatial join is implemented as a 4-way merge.  There are two inputs to
the join, X and Y, and each input provides z-values to be merged from
two data structures:

- nest: a stack of z-values that have been entered but not exited.

- cursor: supplies z-values in z-order. The current item of the cursor
  is visible.

A z-value, z, represents a range of integers, [z.lo, z.hi]. In each
step of the merge, four such integers are considered. The minimum of
these values corresponds to an event: entering or leaving a z-value
from X or Y:

- The z.lo value from the X.cursor.current: Enter X.cursor.current

- The z.lo value from the Y.cursor.current: Enter Y.cursor.current

- The z.hi value from the X.nest: Exit X.nest.top

- The z.hi value from the Y.nest: Exit Y.nest.top

(In the event of a tie between a z.lo value and a z.hi values, the
z.lo value is preferred. Otherwise, overlap involving z.lo's z-value
will be missed.)

During the merge, we want to locate all the cases in which a z-value
from X overlaps a z-value from Y. This will be done by maintaining
X.nest and Y.nest. Part of processing an exit event is to generate
output, combining the z-value being exited with all of the z-values in
the opposite nest.

Example:

          +--------+
X         |   ++   |       ++  ++  ++                ++     ++
          |   ||   |       ||  ||  ||                ||     ||
      ----+---++---+-------++--++--++----------------++-----++---------
          a  bcd e f       gh  ij  kl    mn      o   pq     rs   t
      -------+---+-----------------------++------+---------------+-----
Y            |   |                       ||      |               |
             +---+                       ++      +---------------+

The merge (without optimization) proceeds as follows:

1. Initially, both X.nest and Y.nest are empty. X.cursor.current is
the z-value af, which covers the range [A, F]. Y.current is be.

        X:    nest: []        current: af
        Y:    next: []        current: be
        output:

2. The nests are empty, and comparing the z.lo values of X.current and
Y.current, a < b, so the first thing we do is to enter the af z-value,
placing af on the X.nest stack. The X cursor is advanced.

        X:    nest: [af]      current: cd
        Y:    next: []        current: be
        output:

3. The candidate events are exiting X.nest at position f, entering
X.current at position c, and entering Y.current at position
b. The minimum value of these is b, so the next event is to enter
be. be is placed on Y.nest, and Y.cursor is advanced.

        X:    nest: [af]      current: cd
        Y:    next: [be]      current: mn
        output:

4. There are now candidates from all four parts of the merge:
X.nest.hi = f, X.current.lo = c, Y.nest.hi = e,
Y.current.lo = m. The next event is to enter
X.current = cd. cd is contained by Y.nest, so we enter the
z-value, pushing it onto X.nest.

        X:    nest: [af, cd]  current: gh
        Y:    next: [be]      current: mn
        output:

5. The next event is to exit the cd z-value in X.nest, the one just
entered. We generate output by combining this z-value with each
z-value in Y.nest input's nest, so the output is (cd, be), indicating
that the associated spatial objects overlap. cd is popped from its
stack.

        X:    nest: [af]      current: gh
        Y:    next: [be]      current: mn
        output:  (cd, be)

6. The next event is to exit be. We generate output (af, be), and pop
Y.nest.

        X:    nest: [af]      current: gh
        Y:    next: []        current: mn
        output:  (cd, be), (af, be)

7. The next event is to exit af. No output is generated because the
opposite nest is empty.

        X:    nest: []        current: gh
        Y:    next: []        current: mn
        output:  (cd, be), (af, be)

8. Next we enter gh.

        X:    nest: [gh]      current: ij
        Y:    next: []        current: mn
        output:  (cd, be), (af, be)

9. And exit gh, generating no output.

        X:    nest: []        current: ij
        Y:    next: []        current: mn
        output:  (cd, be), (af, be)

10. The next events are all from the X input, and obviously
contribute no output. We enter and exit ij, and kl.

        X:    nest: []        current: pq
        Y:    next: []        current: mn
        output:  (cd, be), (af, be)

11. From Y, we enter and exit mn, generating no output.

        X:    nest: []        current: pq
        Y:    next: []        current: ot
        output:  (cd, be), (af, be)

12. Enter ot.

        X:    nest: []        current: pq
        Y:    next: [ot]      current: --
        output:  (cd, be), (af, be)

13. Enter pq, and then exit pq, noting the overlap with ot.

        X:    nest: []      current: rs
        Y:    next: [ot]      current: --
        output:  (cd, be), (af, be), (pq, ot)

14. Enter rs, and then exit rs, noting the overlap with ot.

        X:    nest: []        current: --
        Y:    next: [ot]      current: --
        output:  (cd, be), (af, be), (pq, ot), (rs, ot)

15. Exit ot, and we're done.

        X:    nest: []        current: --
        Y:    next: []        current: --
        output:  (cd, be), (af, be), (pq, ot), (rs, ot)

Optimization

On entry to step 8 above, the state of the merge is as follows:

        X:    nest: []        current: gh
        Y:    next: []        current: mn
        output:  (cd, be), (af, be)

The next event is to enter gh, but that is obviously going to produce
no output. This leads to an optimization.

We know that gh isn't going to produce output because it doesn't
overlap any z-value in Y.nest, and because it doesn't overlap
with Y.cursor.current (mn). (And there can't be any z-values in Y
between Y.nest.top and Y.cursor.current due to the way that these data
structures are maintained, with cursor.current being pushed to the
nest on entry to the z-value.) The absence of overlap can be checked
cheaply, without incurring any additional IO.

(To determine whether a z-value, X.current, overlaps any element in Y.nest, it is
sufficient to test:
  - a) whether X.current contains Y.nest.top, and
  - b) whether Y.nest.bottom contains X.current
If either of these is true, then there is overlap, otherwise there isn't. Obviously,
condition a is sufficient. If it doesn't hold, then if any z-value in Y.nest contains X.current,
the bottom-most one will, because z-values are nested, bottom to top.)

With the z-values visible (gh and mn, both nests are empty), all we
know is that the next overlapping pair might involve mn, or if it
doesn't, it involves z-values later in the Y input sequence.

To make use of this observation, we can do a random access in
X.cursor, at m, locating pq, the first z-value past m. It is premature
to enter pq, but we can move the cursor ahead, resulting in this
state:

        X:    nest: []        current: pq
        Y:    next: []        current: mn
        output:  (cd, be), (af, be)

In general, the random access (that located pq) is not sufficient by
itself, because it only searches forward. There could be a z-value
containing both mn and pq, denoted as yz (not present in the original
data set), with y < m:

          +--------+                   +-----------------+
X:        |   ++   |       ++  ++  ++  |             ++  |  ++
          |   ||   |       ||  ||  ||  |             ||  |  ||
      ----+---++---+-------++--++--++--+-------------++--+--++---------
          a  bcd e f       gh  ij  kl  y mn      o   pq  z  rs   t
      -------+---+-----------------------++------+---------------+-----
Y:           |   |                       ||      |               |
             +---+                       ++      +---------------+

After skipping gh, ij and kl, the correct thing to do would be to
resume at yz. But if we do a random access at m, and locate the first
z-value >= m, we will miss yz. To locate yz we have to do random
accesses for elements containing pq. Due to the construction of
z-values, there are only a few possible containers, which can be
obtained by removing trailing bits from pq. E.g., if pq is the 16-bit
z-value 0x50af, then the containers are:

        bits    z-value
        15      0x50ae
        14      0x50ac
        13      0x50a8
        12      0x50a0
        11      0x50a0
        10      0x5080
            ...
         3      0x4
         2      0x4
         1      0x0

And we only have to consider a container c with c.zlo >= g, (because
we already determined that gh doesn't overlap with mn, and c.zlo can't
be < g or else we would have already processed it).

Going back to the original data set (without yz), and this state:

        X:    nest: []        current: pq
        Y:    next: []        current: mn
        output:  (cd, be), (af, be)

we now have a similar situation on entry to mn -- the z-value about to
be entered doesn't overlap the other nest.top or cursor.current. We
can use p to skip ahead. The random access locates nothing (because
the Y input sequence has no z-value >= pq), but checking containers as
above, we locate ot.

        X:    nest: []        current: pq
        Y:    next: []        current: ot
        output:  (cd, be), (af, be)

We next enter ot:

        X:    nest: []        current: pq
        Y:    next: [ot]      current: --
        output:  (cd, be), (af, be)

enter pq:

        X:    nest: [pq]      current: rs
        Y:    next: [ot]      current: --
        output:  (cd, be), (af, be)

exit pq, generating output:

        X:    nest: []        current: rs
        Y:    next: [ot]      current: --
        output:  (cd, be), (af, be), (pq, ot)

enter rs:

        X:    nest: [rs]      current: --
        Y:    next: [ot]      current: --
        output:  (cd, be), (af, be), (pq, ot)

exit rs, generating output:

        X:    nest: []        current: --
        Y:    next: [ot]      current: --
        output:  (cd, be), (af, be), (pq, ot), (rs, ot)

exit ot, and we're done:

        X:    nest: []        current: --
        Y:    next: []        current: --
        output:  (cd, be), (af, be), (pq, ot), (rs, ot)

*/


class SpatialJoinInput
{
    // Object interface

    @Override
    public final String toString()
    {
        return name();
    }

    // SpatialJoinInput interface

    public long nextEntry()
    {
        return eof ? EOF : SpaceImpl.zLo(current.z());
    }

    public long nextExit()
    {
        return nest.isEmpty() ? EOF : SpaceImpl.zHi(nest.peek().z());
    }

    public void enterZ() throws IOException, InterruptedException
    {
        assert !eof;
        observer.enter(current.z());
        if (currentOverlapsOtherNest() ||
            !that.eof && overlap(current.z(), that.current.z())) {
            // Enter current
            if (!nest.isEmpty()) {
                long topZ = nest.peek().z();
                assert SpaceImpl.contains(topZ, current.z());
            }
            push(current);
            copyToCurrent(cursorNext(cursor));
        } else {
            advanceCursor();
        }
        log("enter");
    }

    public void exitZ()
    {
        assert !nest.isEmpty();
        Record top = nest.pop();
        observer.exit(top.z());
        that.generateSpatialJoinOutput(top);
        log("exit");
    }

    public final void otherInput(SpatialJoinInput that)
    {
        this.that = that;
    }

    public static SpatialJoinInput newSpatialJoinInput(SpatialIndexImpl spatialIndex,
                                                       SpatialJoinOutput spatialJoinOutput,
                                                       SpatialJoin.InputObserver observer)
        throws IOException, InterruptedException
    {
        return new SpatialJoinInput(spatialIndex, spatialJoinOutput, observer);
    }

    // For use by this class

    private void advanceCursor() throws IOException, InterruptedException
    {
        // Use that.current to skip ahead
        if (that.eof) {
            // If that.current is EOF, then we can skip to the end on this side too.
            this.eof = true;
        } else {
            assert !eof; // Should have been checked in caller, but just to be sure.
            long thisCurrentZ = this.current.z();
            long thatCurrentZ = that.current.z();
            assert thatCurrentZ >= thisCurrentZ; // otherwise, we would have entered that.current
            if (thatCurrentZ > thisCurrentZ) {
                if (singleCellOptimization && singleCell) {
                    randomAccessKey.z(thatCurrentZ);
                    cursorGoTo(cursor, randomAccessKey);
                    copyToCurrent(cursorNext(cursor));
                } else {
                    // Why this works: There are two cases to consider.
                    // 1) thatCurrentZ contains thisCurrentZ: thisCurrentZ might be the correct place to
                    //    resume. But it is also possible that there is a larger, containing ancestor z-value,
                    //    a, such that a.lo() < thatCurrentZ. This call finds that ancestor.
                    // 2) thatCurrentZ does not contain thisCurrentZ, which means that thatCurrentZ.hi() <=
                    //    thisCurrentZ.lo(). We need to find an ancestor of thisCurrentZ, a. If a.lo() >
                    //    thatCurrentZ.hi(), then the random access would have discovered it, so this case can't happen.
                    //    We must therefore look for a container of thisCurrentZ that ALSO contains thatCurrentZ.
                    //    So again, we can start the search for an ancestor at thatCurrentZ.
                    advanceToNextOrAncestor(thatCurrentZ, thisCurrentZ);
                }
            }
        }
    }

    // Advance to an ancestor of zStart, or if there is none, to the z-value after zStart.
    private void advanceToNextOrAncestor(long zStart, long zLowerBound)
        throws IOException, InterruptedException
    {
        // Generate all the ancestors that need to be considered.
        int nCandidates = 0;
        long zCandidate = zStart;
        while (zCandidate > zLowerBound) {
            zCandidates[nCandidates++] = zCandidate;
            zCandidate = SpaceImpl.parent(zCandidate);
        }
        // In the caller, thatCurrentZ > thisCurrentZ, so zStart > zLowerBound, and zCandidate is initialized to
        // zStart. So zCandidate > zLowerBound has to be true at least once, and nCandidates > 0.
        assert nCandidates > 0;
        // Find the largest ancestor among the candidates that exists.
        int c = nCandidates;
        boolean foundAncestor = false;
        while (!foundAncestor && --c >= 0) {
            zCandidate = zCandidates[c];
            randomAccessKey.z(zCandidate);
            cursorGoTo(cursor, randomAccessKey);
            Record record = cursorNext(cursor);
            if (c == 0) {
                // No ancestors were found. Go to the record following zStart.
                assert zCandidate == zStart;
                copyToCurrent(record);
            } else if (record != null && record.z() == zCandidate) {
                copyToCurrent(record);
                foundAncestor = true;
            }
        }
        if (eof) {
            cursor.close();
        } else {
            assert current.z() >= zLowerBound;
        }
        observer.ancestorSearch(cursor,
                                zStart,
                                foundAncestor ? current.z() : SpaceImpl.Z_NULL);
    }

    private boolean currentOverlapsOtherNest()
    {
        boolean overlap = false;
        Record thatNestTop = that.nestTop();
        if (thatNestTop != null) {
            long thisCurrentZ = current.z();
            overlap =
                SpaceImpl.contains(thisCurrentZ, thatNestTop.z()) ||
                SpaceImpl.contains(that.nestBottom().z(), thisCurrentZ);
        }
        return overlap;
    }

    private Record nestTop()
    {
        return nest.peek();
    }

    private Record nestBottom()
    {
        return nest.peekLast();
    }

    private void generateSpatialJoinOutput(Record thatRecord)
    {
        for (Record thisRecord : nest) {
            spatialJoinOutput.add(thisRecord, thatRecord);
        }
    }

    private boolean overlap(long z1, long z2)
    {
        return SpaceImpl.contains(z1, z2) || SpaceImpl.contains(z2, z1);
    }

    private String name()
    {
        return String.format("sjinput(%s)", id);
    }

    private SpatialJoinInput(SpatialIndexImpl spatialIndex,
                             SpatialJoinOutput spatialJoinOutput,
                             SpatialJoin.InputObserver observer)
        throws IOException, InterruptedException
    {
        Index index = spatialIndex.index();
        this.stableRecords = index.stableRecords();
        this.spatialIndex = spatialIndex;
        this.observer = observer == null ? DEFAULT_OBSERVER : observer;
        // Initialize cursor
        this.cursor = index.cursor();
        Record zMinKey = index.newKeyRecord();
        zMinKey.z(SpaceImpl.Z_MIN);
        cursorGoTo(cursor, zMinKey);
        //
        this.current = stableRecords ? null : index.newRecord();
        this.randomAccessKey = index.newKeyRecord();
        copyToCurrent(cursorNext(this.cursor));
        this.spatialJoinOutput = spatialJoinOutput;
        this.singleCell = spatialIndex.singleCell();
        this.singleCellOptimization = SpatialJoinImpl.singleCellOptimization();
        log("initialize");
    }

    private void copyToCurrent(Record record)
    {
        if (record == null) {
            eof = true;
        } else {
            if (stableRecords) {
                current = record;
            } else {
                record.copyTo(current);
            }
            eof = false;
        }
    }

    private void push(Record record)
    {
        if (stableRecords) {
            nest.push(record);
        } else {
            Record copy = spatialIndex.index().newRecord();
            record.copyTo(copy);
            nest.push(copy);
        }
    }

    private void cursorGoTo(Cursor cursor, Record key) throws IOException, InterruptedException
    {
        cursor.goTo(key);
        lastZRandomAccess = key.z();
        observer.randomAccess(cursor, lastZRandomAccess);
    }

    private Record cursorNext(Cursor cursor) throws IOException, InterruptedException
    {
        Record record = cursor.next();
        observer.sequentialAccess(cursor, lastZRandomAccess, record);
        return record;
    }

    private void log(String label)
    {
        if (LOG.isLoggable(Level.FINE)) {
            StringBuilder buffer = new StringBuilder();
            Iterator<Record> nestScan = nest.descendingIterator();
            long[] zs = new long[64];
            int[] counts = new int[64];
            int n = 0;
            while (nestScan.hasNext()) {
                Record record = nestScan.next();
                long z = record.z();
                if (n > 0 && zs[n - 1] == z) {
                    counts[n - 1]++;
                } else {
                    zs[n] = z;
                    counts[n] = 1;
                    n++;
                }
            }
            for (int i = 0; i < n; i++) {
                long z = zs[i];
                buffer.append(' ');
                buffer.append(formatZ(z));
                if (counts[i] > 1) {
                    buffer.append('[');
                    buffer.append(counts[i]);
                    buffer.append(']');
                }
            }
            String nextZ = eof ? "eof" : formatZ(current.z());
            LOG.log(Level.FINE,
                    "{0} {1}: nest:{2}, current: {3}",
                    new Object[]{this, label, buffer.toString(), nextZ});
        }
    }

    // Class state

    // A 64-bit z-value is definitely less than Long.MAX_VALUE. The maxium z-value length is 57, which is recorded
    // in a 6-bit length field. So the length field will always have some zeros in the last 6 bits,
    // while Long.MAX_VALUE doesn't.
    public static long EOF = Long.MAX_VALUE;
    private static final Logger LOG = Logger.getLogger(SpatialJoinInput.class.getName());
    private static final AtomicInteger idGenerator = new AtomicInteger(0);
    private static final SpatialJoin.InputObserver DEFAULT_OBSERVER = new SpatialJoin.InputObserver();

    // Object state

    private final int id = idGenerator.getAndIncrement();
    private final SpatialIndexImpl spatialIndex;
    private final boolean stableRecords;
    private final boolean singleCell;
    private SpatialJoinInput that;
    private final SpatialJoinOutput spatialJoinOutput;
    // nest contains z-values that have been entered but not exited. current is the next z-value to enter,
    // and cursor contains later z-values.
    private final Deque<Record> nest = new ArrayDeque<>();
    private final Cursor cursor;
    private Record current;
    private final Record randomAccessKey;
    // For use in finding ancestors
    private final long[] zCandidates = new long[SpaceImpl.MAX_Z_BITS];
    private long lastZRandomAccess; // For observing access pattern
    private boolean eof = false;
    private final boolean singleCellOptimization;
    private final SpatialJoin.InputObserver observer;
}
