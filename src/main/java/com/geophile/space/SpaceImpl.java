/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.space;

import com.geophile.Space;
import com.geophile.spatialobject.SpatialObject;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

public class SpaceImpl extends Space
{
    // SpaceImpl interface

    public int dimensions()
    {
        return dimensions;
    }

    public long shuffle(long x[])
    {
        return shuffle(x, zBits);
    }

    public void decompose(SpatialObject spatialObject, long[] zs)
    {
        int maxRegions = zs.length;
        int zCount = 0;
        long[] x = spatialObject.arbitraryPoint();
        Region region = new Region(this, x, x, zBits);
        while (!spatialObject.containedBy(region)) {
            region.up();
        }
        Queue<Region> queue = new ArrayDeque<>(maxRegions);
        queue.add(region);
        while (!queue.isEmpty()) {
            region = queue.poll();
            if (region.isPoint()) {
                zs[zCount++] = region.z();
            } else {
                region.downLeft();
                RegionComparison leftComparison = spatialObject.compare(region);
                region.up();
                region.downRight();
                RegionComparison rightComparison = spatialObject.compare(region);
                switch (leftComparison) {
                    case REGION_OUTSIDE_OBJECT:
                        switch (rightComparison) {
                            case REGION_OUTSIDE_OBJECT:
                                assert false;
                                break;
                            case REGION_INSIDE_OBJECT:
                                zs[zCount++] = region.z();
                                break;
                            case REGION_OVERLAPS_OBJECT:
                                queue.add(region);
                                break;
                        }
                        break;
                    case REGION_INSIDE_OBJECT:
                        switch (rightComparison) {
                            case REGION_OUTSIDE_OBJECT:
                                region.up();
                                region.downLeft();
                                zs[zCount++] = region.z();
                                break;
                            case REGION_INSIDE_OBJECT:
                                region.up();
                                zs[zCount++] = region.z();
                                break;
                            case REGION_OVERLAPS_OBJECT:
                                if (queue.size() + 1  + zCount < maxRegions) {
                                    queue.add(region.copy());
                                    region.up();
                                    region.downLeft();
                                    zs[zCount++] = region.z();
                                } else {
                                    region.up();
                                    zs[zCount++] = region.z();
                                }
                                break;
                        }
                        break;
                    case REGION_OVERLAPS_OBJECT:
                        switch (rightComparison) {
                            case REGION_OUTSIDE_OBJECT:
                                region.up();
                                region.downLeft();
                                queue.add(region);
                                break;
                            case REGION_INSIDE_OBJECT:
                                if (queue.size() + 1  + zCount < maxRegions) {
                                    zs[zCount++] = region.z();
                                    region.up();
                                    region.downLeft();
                                    queue.add(region);
                                } else {
                                    region.up();
                                    zs[zCount++] = region.z();
                                }
                                break;
                            case REGION_OVERLAPS_OBJECT:
                                if (queue.size() + 1 + zCount < maxRegions) {
                                    queue.add(region.copy());
                                    region.up();
                                    region.downLeft();
                                    queue.add(region);
                                } else {
                                    region.up();
                                    zs[zCount++] = region.z();
                                }
                                break;
                        }
                        break;
                }
            }
        }
        while (!queue.isEmpty()) {
            region = queue.poll();
            zs[zCount++] = region.z();
        }
        for (int i = zCount; i < maxRegions; i++) {
            zs[i] = -1L;
        }
        Arrays.sort(zs, 0, zCount);
        boolean merge;
        do {
            merge = false;
            for (int i = 1; i < zCount; i++) {
                long a = zs[i - 1];
                long b = zs[i];
                if ((merge = siblings(a, b))) {
                    zs[i - 1] = parent(a);
                    System.arraycopy(zs, i + 1, zs, i, zCount - i - 1);
                    zs[--zCount] = -1L;
                }
            }
        } while (merge);
    }

    public long shuffle(long x[], int length)
    {
        long z = 0;
        for (int d = 0; d < dimensions; d++) {
            long xd = x[d];
            switch (xBytes[d]) {
                case 8: z |= shuffle7[d][(int)(xd >>> 56) & 0xff];
                case 7: z |= shuffle6[d][(int)(xd >>> 48) & 0xff];
                case 6: z |= shuffle5[d][(int)(xd >>> 40) & 0xff];
                case 5: z |= shuffle4[d][(int)(xd >>> 32) & 0xff];
                case 4: z |= shuffle3[d][(int)(xd >>> 24) & 0xff];
                case 3: z |= shuffle2[d][(int)(xd >>> 18) & 0xff];
                case 2: z |= shuffle1[d][(int)(xd >>>  8) & 0xff];
                case 1: z |= shuffle0[d][(int)(xd       ) & 0xff];
            }
        }
        return z | length;
    }

    public boolean siblings(long a, long b)
    {
        boolean siblings = false;
        int length = length(a);
        if (length > 0 && length == length(b)) {
            long matchMask = ((1L << (length - 1)) - 1) << (64 - length);
            long differMask = 1L << (63 - length);
            siblings = (a & matchMask) == (b & matchMask) && (a & differMask) != (b & differMask);
        }
        return siblings;
    }

    public long parent(long z)
    {
        int length = length(z);
        check(length > 0, Long.toString(z));
        length--;
        long mask = ((1L << length) - 1) << (63 - length);
        return (z & mask) | length;
    }

    public boolean contains(long a, long b)
    {
        int aLength = length(a);
        int bLength = length(b);
        boolean prefix = aLength <= bLength;
        if (prefix) {
            long mask = ((1L << aLength) - 1) << (63 - aLength);
            prefix = (a & mask) == (b & mask);
        }
        return prefix;
    }

    public long zLo(long z)
    {
        return z;
    }

    public long zHi(long z)
    {
        long mask = ((1L << (MAX_Z_BITS - length(z))) - 1) << LENGTH_BITS;
        return z | mask;
    }

    public int zBits()
    {
        return zBits;
    }

    public int[] interleave()
    {
        return interleave;
    }

    public long z(long bits, int length)
    {
        assert (bits & LENGTH_MASK) == 0 : bits;
        assert length <= MAX_Z_BITS : length;
        return (bits >>> 1) | length;
    }

    public SpaceImpl(int[] xBits, int[] interleave)
    {
        this.dimensions = xBits.length;
        check(dimensions >= 1 && dimensions <= MAX_DIMENSIONS,
              "dimensions (%s) must be between 1 and %s", dimensions, MAX_DIMENSIONS);
        this.xBits = Arrays.copyOf(xBits, dimensions);
        this.xBytes = new int[dimensions];
        int zBits = 0;
        for (int d = 0; d < dimensions; d++) {
            zBits += xBits[d];
            xBytes[d] = (xBits[d] + 7) / 8;
        }
        this.zBits = zBits;
        interleave =
            interleave == null
            ? defaultInterleaving()
            : Arrays.copyOf(interleave, interleave.length);
        this.interleave = interleave;
        check(this.interleave.length == zBits,
              "this.interleave.length (%s) != zBits (%s)", this.interleave.length, zBits);
        // Check interleave
        {
            int[] count = new int[dimensions];
            for (int zBitPosition = 0; zBitPosition < zBits; zBitPosition++) {
                count[this.interleave[zBitPosition]]++;
            }
            check(Arrays.equals(xBits, count), "interleave inconsistent with xBits");
        }
        long[][][] shuffle = computeShuffleMasks();
        shuffle0 = shuffle[0];
        shuffle1 = shuffle[1];
        shuffle2 = shuffle[2];
        shuffle3 = shuffle[3];
        shuffle4 = shuffle[4];
        shuffle5 = shuffle[5];
        shuffle6 = shuffle[6];
        shuffle7 = shuffle[7];
    }

    // For use by this class

    private int[] defaultInterleaving()
    {
        int[] interleave = new int[zBits];
        int[] count = new int[dimensions];
        int d = -1;
        int zPosition = 0;
        while (zPosition < zBits) {
            do {
                d = (d + 1) % dimensions;
            } while (count[d] == xBits[d]);
            interleave[zPosition++] = d;
            count[d]++;
        }
        return interleave;
    }

    private long[][][] computeShuffleMasks()
    {
        // z-value format:
        // - Leading bit is zero.
        // - Last 6 bits is the bit count.
        // - Everything in between is a left-justified bitstring. Bits past the length are zero.
        // A bit count of 0 means a 0-length bitstring, covering the entire space. The maximum bit count is 57,
        // (the number of bits between the leading 0 and the bit count).
        //
        // Shuffling one bit at a time would be slow. The implementation used in shuffle(long[]) should be a lot
        // faster, relying on array subscripting to locate masks which are combined using bitwise OR. The masks
        // are computed here.
        //
        // First, xz, a mapping from x (coordinate) to z bit positions, is computed. xz[d][p] is the position
        // within the z-value of bit p of x[d], (the coordinate of dimension d). For both x and z, bit positions
        // are numbered left-to-right starting at 0. x values are right-justified, while z-values are right-justified.
        //
        // Then the shuffle masks are computed. shuffle[b][d][x] is a mask representing the bits of the bth byte
        // of x[d] that contribute to the z-value.
        int[][] xz;
        xz = new int[dimensions][];
        for (int d = 0; d < dimensions; d++) {
            xz[d] = new int[xBits[d]];
        }
        int[] xBitCount = new int[dimensions];
        for (int zBitPosition = 0; zBitPosition < zBits; zBitPosition++) {
            int d = interleave[zBitPosition];
            xz[d][xBitCount[d]] = zBitPosition;
            xBitCount[d]++;
        }
        long[][][] shuffle = new long[8][][];
        for (int xBytePosition = 0; xBytePosition < 8; xBytePosition++) {
            shuffle[xBytePosition] = new long[dimensions][];
            for (int d = 0; d < dimensions; d++) {
                shuffle[xBytePosition][d] = new long[256];
            }
        }
        for (int d = 0; d < dimensions; d++) {
            for (int xBitPosition = 0; xBitPosition < xBits[d]; xBitPosition++) {
                long xMask = 1L << xBits[d] - xBitPosition - 1;
                long zMask = 1L << (62 - xz[d][xBitPosition]);
                int xByteLeftShift = (xBits[d] - xBitPosition - 1) / 8;
                for (int xByte = 0; xByte <= 0xff; xByte++) {
                    // xPartial explores all 256 values of one byte of a coordinate. Outside this one byte,
                    // everything in xPartial is zero, which is fine for generating shuffle masks.
                    long xPartial = ((long) xByte) << (8 * xByteLeftShift);
                    if ((xPartial & xMask) != 0) {
                        shuffle[xByteLeftShift][d][xByte] |= zMask;
                    }
                }
            }
        }
        return shuffle;
    }

    private int length(long z)
    {
        return (int) (z & LENGTH_MASK);
    }

    private void check(boolean constraint, String template, Object... args)
    {
        if (!constraint) {
            throw new IllegalArgumentException(String.format(template, args));
        }
    }

    // Class state

    static final int MAX_Z_BITS = 57; // MSB is unused. 6 LSBs contain the number of z-value bits.
    static final long LENGTH_MASK = 0x3f;
    static final int LENGTH_BITS = 6;

    // Object state

    // The 'x' prefix refers to coordinates. The 'z' prefix refers to z values.
    final int dimensions;
    final int[] interleave;
    final int[] xBits;
    final int[] xBytes;
    final int zBits;
    // For shuffling
    private final long[][] shuffle0;
    private final long[][] shuffle1;
    private final long[][] shuffle2;
    private final long[][] shuffle3;
    private final long[][] shuffle4;
    private final long[][] shuffle5;
    private final long[][] shuffle6;
    private final long[][] shuffle7;
}
