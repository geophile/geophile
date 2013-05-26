/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.geophile.z.spatialjoin;

import com.geophile.z.SpatialJoin;
import com.geophile.z.spatialobject.d2.Box;

import java.io.IOException;
import java.util.EnumSet;

public class SpatialJoinIteratorProfile extends SpatialJoinIteratorTestBase
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        new SpatialJoinIteratorProfile().run();
    }

    private void run() throws IOException, InterruptedException
    {
        // enableLogging(Level.FINE);
        final int[] SIZES = { 1_000, 2_000, 4_000, 8_000, 16_000, 32_000, 64_000, 128_000 };
        final int TRIALS = 10;
        final int N_POINTS = 100_000;
        final int[] MAX_Z = new int[]{ 4, 8, 12, 16, 20, 24 };
        Counters counters = Counters.forThread();
        for (int size : SIZES) {
            for (int maxZ : MAX_Z) {
                System.setProperty("maxz", Integer.toString(maxZ));
                counters.reset();
                long start = System.currentTimeMillis();
                test(1, size, N_POINTS, 1, TRIALS, EnumSet.of(SpatialJoin.Duplicates.INCLUDE));
                long stop = System.currentTimeMillis();
                double enters = (double) counters.enterZ() / TRIALS;
                double msec = (stop - start) / TRIALS;
                print("size: %s\tmaxZ: %s\tenters: %s\tmsec: %s", size, maxZ, enters, msec);
            }
        }
    }

    @Override
    protected Box randomBox(int size)
    {
        long xLo = random.nextInt(NX - size + 1);
        long xHi = xLo + size - 1;
        long yLo = random.nextInt(NY - size + 1);
        long yHi = yLo + size - 1;
        return new Box(xLo, xHi, yLo, yHi);
    }

    @Override
    protected void checkEquals(Object expected, Object actual)
    {
        assert expected.equals(actual);
    }

    @Override
    protected boolean verify()
    {
        return false;
    }
}
