package com.geophile.z.jcunit;

import com.geophile.z.*;
import com.geophile.z.examples.ExampleIndex;
import com.geophile.z.examples.ExampleRecord;
import com.geophile.z.spatialobject.d2.Box;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.factor.MethodLevelsFactory;
import com.github.dakusui.jcunit.core.rules.JCUnitRecorder;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.generators.RecordedTuplePlayer;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import static com.github.dakusui.jcunit.core.TestCaseUtils.factor;
import static com.github.dakusui.jcunit.core.TestCaseUtils.newTestCase;
import static org.junit.Assert.assertNotNull;

/**
 * Performs geophile tests using models in 'd2' package.
 * Currently 2 tests are failing for the issue #1 https://github.com/geophile/geophile/issues/1 .
 *
 * In order to replay the tests previously recorded and custom test cases only,
 * set system property 'jcunit.replayer=true'.
 * This requires recording test cases in advance, and to record test cases you can set
 * system property 'jcunit.recorder=true'. See the static block of this class.
 */
@RunWith(JCUnit.class)
@TupleGeneration(
        generator = @Generator(
                value = RecordedTuplePlayer.class,
                params = {
                        @Param("FailedOnly"), // Possible values are "All" and "FailedOnly"
                        @Param("src/test/resources/jcunit"),
                        @Param("com.github.dakusui.jcunit.generators.IPO2TupleGenerator"),
                        @Param("2")
                }
        )
)
public class Dimension2Test {
    static {
        ////
        // Setting system property in an '@BeforeClass' annotated method doesn't take effect because generator
        // instantiation and initialization happens even before it.
        System.setProperty(SystemProperties.KEY.RECORDER.key(), "false");
        System.setProperty(SystemProperties.KEY.REPLAYER.key(), "true");
    }

    @SuppressWarnings("unused")
    private static final String TEST_DIR = "src/test/resources/jcunit";

    private static final SpatialJoinFilter BOX_OVERLAP =
            new SpatialJoinFilter() {
                @Override
                public boolean overlap(Object o1, Object o2) {
                    Box a = (Box) ((ExampleRecord) o1).spatialObject();
                    Box b = (Box) ((ExampleRecord) o2).spatialObject();
                    return
                            a.xLo() <= b.xHi() && b.xLo() <= a.xHi() &&
                                    a.yLo() <= b.yHi() && b.yLo() <= a.yHi();
                }
            };

    /**
     * In order to make tests reproducible, give a fixed number as a seed of
     * a random object.
     */
    private final Random random = new Random(726);
    @Rule
    public JCUnitRecorder recorder = new JCUnitRecorder(TEST_DIR);
    @FactorField
    public SpatialJoin.Duplicates duplicates;
    @FactorField(doubleLevels = {1, 64, 1024, 1_000_000})
    public double X;
    @FactorField(doubleLevels = {1, 64, 1024, 1_000_000})
    public double Y;
    @FactorField(intLevels = {0, 1, 20, 30})
    public int X_BITS;
    @FactorField(intLevels = {0, 1, 20, 27})
    public int Y_BITS;
    @FactorField(levelsFactory = MethodLevelsFactory.class)
    public LevelCreator<Index> indexForLeft;
    @FactorField(levelsFactory = MethodLevelsFactory.class)
    public LevelCreator<Index> indexForRight;
    @FactorField(intLevels = {1000, 1024 /* To keep the test id's the same and reduce the test execution time, reduced the number to */})
    public int numBoxes;
    @FactorField(doubleLevels = {0, 0.00001, 1, 2, 21})
    public double boxWidth;
    @FactorField(doubleLevels = {0, 0.00001, 1, 2, 21})
    public double boxHeight;
    SpatialIndex left;
    SpatialIndex right;
    Space space;

    @SuppressWarnings("unused")
    public static LevelCreator[] indexForLeft() { // called by JCUnit
        final String levelName = "indexForLeft";
        return new LevelCreator[]{
                new LevelCreator<Index>() {
                    @Override
                    public Index create() {
                        return new ExampleIndex();
                    }

                    @Override
                    public int levelId() {
                        return 0;
                    }

                    @Override
                    public String factorName() {
                        return levelName;
                    }
                }
        };
    }

    @SuppressWarnings("unused")
    public static LevelCreator[] indexForRight() { // called by JCUnit
        final String levelName = "indexForRight";
        return new LevelCreator[]{
                new LevelCreator<Index>() {
                    @Override
                    public Index create() {
                        return new ExampleIndex();
                    }

                    @Override
                    public int levelId() {
                        return 0;
                    }

                    @Override
                    public String factorName() {
                        return levelName;
                    }

                }
        };
    }

    /**
     * These test cases will be executed even if you specify '@FailedOnly' to RecordedTupleReplayer.
     * Because these are not recorded/replayed.
     * To suppress them, remove '@CustomTestCases' annotation from this method.
     */
    @SuppressWarnings("unchecked")
    @CustomTestCases
    public static Iterable<Tuple> assertionErrorTestCases() {
        return Arrays.asList(
                newTestCase(
                        factor("duplicates", SpatialJoin.Duplicates.EXCLUDE),
                        factor("X", 1.0), factor("Y", 2.0),
                        factor("X_BITS", 30), factor("Y_BITS", 27),
                        factor("indexForLeft", indexForLeft()[0]),
                        factor("indexForRight", indexForRight()[0]),
                        factor("numBoxes", 1000),
                        factor("boxWidth", 0.5), factor("boxHeight", 1.5)
                ),
                newTestCase(
                        factor("duplicates", SpatialJoin.Duplicates.EXCLUDE),
                        factor("X", 2.0), factor("Y", 1.0),
                        factor("X_BITS", 30), factor("Y_BITS", 27),
                        factor("indexForLeft", indexForLeft()[0]),
                        factor("indexForRight", indexForRight()[0]),
                        factor("numBoxes", 1000),
                        factor("boxWidth", 1.5), factor("boxHeight", 0.5)
                )
        );
    }

    public static boolean areObjectsInsideSpace(Dimension2Test test) {
        return test.X > test.boxWidth && test.Y > test.boxHeight;
    }

    //    @Precondition
    public static boolean isObjectHeightBiggerThanX(Dimension2Test test) {
        return test.X <= test.boxHeight && test.boxHeight <= test.Y;
    }

    //    @Precondition
    public static boolean isObjectWidthBiggerThanY(Dimension2Test test) {
        return test.Y <= test.boxWidth && test.boxWidth <= test.X;
    }

    public static boolean isBoxSizeValid(Dimension2Test test) {
        return test.boxWidth > 0 && test.boxHeight > 0;
    }

    @BeforeClass
    public static void beforeClass() {
        JCUnitRecorder.initializeDir(TEST_DIR, Dimension2Test.class);
    }

    @Test
    @Given("areObjectsInsideSpace&&isBoxSizeValid&&!isObjectHeightBiggerThanX&&!isObjectWidthBiggerThanY")
    public void whenPerformSpatialJoin$thenNoExceptionThrown()
            throws IOException, InterruptedException {
        prepareSpatialJoin();
        performSpatialJoin();
    }

    @Test(expected = SpatialObjectException.class)
    @Given("!areObjectsInsideSpace&&isBoxSizeValid")
    public void whenPrepareSpatialJoin$thenSpatialObjectExceptionWillBeThrown()
            throws IOException, InterruptedException {
        prepareSpatialJoin();
    }

    @Test(expected = IllegalArgumentException.class)
    @Given("!isBoxSizeValid")
    public void whenPrepareSpatialJoin$thenIllegalArgumentExceptionWillBeThrown()
            throws IOException, InterruptedException {
        prepareSpatialJoin();
    }

    /**
     * @see "https://github.com/geophile/geophile/issues/1"
     */
    @Test
    @Given({"isObjectHeightBiggerThanX&&isBoxSizeValid", "isObjectWidthBiggerThanY&&isBoxSizeValid"})
    public void whenPrepareSpatialJoin$thenOnlySpatialObjectExceptionCanBeThrown() throws IOException, InterruptedException {
        try {
            prepareSpatialJoin();
        } catch (SpatialObjectException e) {
            if (isObjectHeightBiggerThanX(this) || isObjectWidthBiggerThanY(this)) {
                ////
                // In case X > box width > Y or Y > box height > X, spatial object exception can be thrown.
                // (Sometimes it's not thrown by chance)
                // In future, this might be fixed but for now, we have to ignore these exception.
                System.out.println("Ignoring " + e);
            } else {
                ////
                // Except the issue above, spatial object exception should be considered (potentially) a problem.
                // So throw it to make JUnit report it.
                throw e;
            }
        }
    }

    private void prepareSpatialJoin() throws IOException, InterruptedException {
        System.out.println(this.recorder.getTestName());
        System.out.println(
                String.format(
                        "    %s", TupleUtils.toString(this.recorder.getTestCase())
                ));
        this.space = Space
                .newSpace(new double[]{0, 0}, new double[]{X, Y},
                        new int[]{X_BITS, Y_BITS});
        this.left = SpatialIndex
                .newSpatialIndex(this.space, this.indexForLeft.create());
        this.right = SpatialIndex
                .newSpatialIndex(this.space, this.indexForRight.create());

        for (int i = 0; i < numBoxes; i++) {
            Box leftBox = randomBox();
            left.add(leftBox, new ExampleRecord(leftBox, i));
            Box rightBox = randomBox();
            right.add(rightBox, new ExampleRecord(rightBox, i));
        }
    }

    private void performSpatialJoin() throws IOException, InterruptedException {
        Iterator<Pair> iterator =
                SpatialJoin.iterator(left, right, BOX_OVERLAP, this.duplicates);
        long before = System.nanoTime();
        long numFound = 0;
        while (iterator.hasNext()) {
            Pair overlappingPair = iterator.next();
            assertNotNull(overlappingPair);
            assertNotNull(overlappingPair.left());
            assertNotNull(overlappingPair.right());
            numFound++;
        }
        long timeElapsedInNano = System.nanoTime() - before;

        System.out.println(String.format(
                "    result: (%8s[nsec],%6d,%6d)",
                timeElapsedInNano,
                numFound,
                this.numBoxes
        ));
    }

    private Box randomBox() {
        double xLo = random.nextDouble() * (X - boxWidth);
        double xHi = xLo + boxWidth;
        double yLo = random.nextDouble() * (Y - boxHeight);
        double yHi = yLo + boxHeight;
        return new Box(xLo, xHi, yLo, yHi);
    }
}
