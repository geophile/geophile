package com.geophile.z;

import com.geophile.z.examples.OverlappingPairs;
import com.geophile.z.examples.PointsInBox;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ExamplesTest {
    @Test
    public void testPointsInBox() throws IOException, InterruptedException {
        PrintStream ps = PointsInBox.out;
        PointsInBox.out = DUMMY_PRINTSTREAM;
        try {
            // Let's make sure at least PointsInBox doesn't throw an exception.
            PointsInBox.main(new String[]{});
        } finally {
            PointsInBox.out = ps;
        }
    }

    @Test
    public void testOverlappingPairs() throws IOException, InterruptedException {
        PrintStream ps = PointsInBox.out;
        OverlappingPairs.out = DUMMY_PRINTSTREAM;
        try {
            // Let's make sure at least PointsInBox doesn't throw an exception.
            OverlappingPairs.main(new String[]{});
        } finally {
            OverlappingPairs.out = ps;
        }
    }

    private static PrintStream createDummyPrintStream() {
        return new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        });
    }

    private static final PrintStream DUMMY_PRINTSTREAM = createDummyPrintStream();
}
