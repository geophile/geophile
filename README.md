# Geophile

Geophile is a Java implementation of *spatial join*. Given two sets of
spatial objects, *R* and *S*, the spatial join is the set of pairs, ( _x_, _y_), 
such that *x* belongs to *R*, *y* belongs to *S*, and *x*
and *y* overlap.

This implementation is built on two abstractions:

* **Index:** Any key/value data structure supporting both random and
sequential access. Binary trees, skiplists, and b-trees are examples
of indexes that are compatible with Geophile. Hash tables are not,
because they do not provide for sequential access in key order.

* **Spatial Object:** Any kind of spatial object, in any number of
dimensions. The implementation must provide a method that determines the
relationship of the object to a box: the box is outside the spatial
object; the box is contained by the spatial object; or the box
overlaps the spatial object.

These implementations can be mixed arbitrarily, as long as all the
spatial objects involved are located in the same space.  For example,
you could compute the spatial join of a b-tree containing millions of
polygons with an in-memory sorted array containing a small number of
points. The output would contain the (polygon, point) pairs for which
the point is inside the polygon.

## Installation

Geophile can be built from source using [maven](http://maven.apache.org):

        mvn install

This creates `target/geophile-1.1.2.jar`.

To create Javadoc files:

        mvn javadoc:javadoc

These files are located in `target/site/apidocs`.

## Concepts

In addition to Index and Spatial Object, described above, Geophile
relies on the following concepts.

### Space 

In order to use Geophile, you must describe the *space* in which spatial
objects reside by giving the number of cells in each dimension, e.g.

        Space space = Space.newSpace(new double[]{0.0, 1_000_000.0},
                                     new double[]{0.0, 1_000_000.0}, 
                                     new int[]{10, 10});

A `Space` describes the space from the application's point of view.
In this case, the space has two dimensions, whose coordinates go from 0 to 1,000,000 in each
dimension. The last argument says that Geophile represents the space internally
using a grid with 10 bits of resolution in each dimension.

The total resolution must never exceed 57. In this case, the total resolution is 20 (10 + 10).

### Spatial Index

A *spatial index* is layered over a `Space` and an `Index`. This is the
interface for adding and removing spatial objects. 

Geophile works by mapping spatial objects into *z-values*, 64-bit
integers which encode spatial information. This encoding of spatial
objects as z-values, and the use of z-values as index keys is managed
by the `SpatialIndex` implementation.

### Spatial Join

*Spatial join* is computed by the `SpatialJoin` class, given two
spatial indexes. Output from spatial join comprises a set of
overlapping pairs of spatial objects, which can be accessed through an
`java.util.Iterator`.

## Examples

The source code for these examples can be found in
`src/test/java/com/geophile/z/examples`. Scripts running the examples
can be found in `src/test/examples`.

Both examples rely on the following `Index` and `SpatialObject` classes,
included with the distribution:

* `com.geophile.z.index.treeindex.TreeIndex`: An adapter
between `com.geophile.z.Index` and `java.util.TreeSet`.

* `com.geophile.z.spatialobject.d2.Point`: A 2d point.

* `com.geophile.z.spatialobject.d2.Box`: A 2d box.

To run the examples, set the `CLASSPATH` to contain
`target/geophile-1.1.2.jar` and `target/test-classes`.

### Find all overalpping pairs of boxes

`OverlappingPairs` loads two spatial indexes, each with 100,000 tiny
boxes, and then finds all the overlapping pairs.

The space is created as follows:

    private static final int X = 1_000_000;
    private static final int Y = 1_000_000;
    private static final int X_BITS = 20;
    private static final int Y_BITS = 20;
    private static final int N_BOXES = 100_000;
    ...
    private static final Space SPACE = Space.newSpace(new double[]{0, 0}, 
                                                      new double[]{X, Y}, 
                                                      new int[]{X_BITS, Y_BITS});

The spatial indexes are created and loaded as follows:

        ExampleRecord.Factory recordFactory = 
            new ExampleRecord.Factory();
        SpatialIndex<ExampleRecord> left = 
            SpatialIndex.newSpatialIndex(SPACE, new ExampleIndex());
        SpatialIndex<ExampleRecord> right = 
            SpatialIndex.newSpatialIndex(SPACE, new ExampleIndex());
        Box box;
        for (int i = 0; i < N_BOXES; i++) {
            Box leftBox = randomBox();
            Box rightBox = randomBox();
            left.add(leftBox, recordFactory.setup(leftBox, i));
            right.add(rightBox, recordFactory.setup(rightBox, i));
        }
        ...
        private static final int N_BOXES = 1_000_000;

* `ExampleRecord` is a simple class encapsulating a `SpatialObject` and an integer identifier.

* `ExampleRecord.Factory` creates `ExampleRecord` objects. 
`ExampleRecord.Factory.setup(SpatialObject, int)` modifies the `Factory` so that
`Factory.newRecord()` calls will create an `ExampleRecord` with the desired `SpatialObject` and
identifier. It also returns the `Factory` object itself.

* `ExampleIndex` is a wrapper around `TreeIndex` (a `TreeSet`-based
index) which specifies that it contains records of type
`ExampleRecord`, and a Comparator for these records.

The spatial join output is created and scanned as follows:

        Iterator<Pair<ExampleRecord, ExampleRecord>> iterator =
            SpatialJoin.iterator(left, 
                                 right, 
                                 BOX_OVERLAP, 
                                 SpatialJoin.Duplicates.EXCLUDE);
        while (iterator.hasNext()) {
            Pair<ExampleRecord, ExampleRecord> overlappingPair = iterator.next();
            ...
        }

To run the example (exact results will differ):

        $ src/test/examples/overlapping_pairs 
        Overlapping pairs
            (808041.0:808050.0, 309510.0:309519.0)	(808038.0:808047.0, 309505.0:309514.0)
            (912678.0:912687.0, 766340.0:766349.0)	(912673.0:912682.0, 766348.0:766357.0)

### Find all the points in a given box

`PointsInBox` loads 100,000 points in a spatial index, and then does
a spatial join to locate all the points within a query box. There is a
simplified API for cases like this, in which one of the spatial join
inputs is a single spatial object.

The spatial index with 1,000,000 points is created and loaded as follows:

        ExampleRecord.Factory recordFactory = 
            new ExampleRecord.Factory();
        SpatialIndex<ExampleRecord> points = 
            SpatialIndex.newSpatialIndex(SPACE, new ExampleIndex());
        for (int i = 0; i < N_POINTS; i++) {
            Point point = randomPoint();
            points.add(point, recordFactory.setup(point, i));
        }
        ...
        private static final int N_POINTS = 100_000;

The query box is created:

            Box box = randomBox();

The spatial join is done as follows:

            Iterator<ExampleRecord> iterator =
                SpatialJoin.iterator(box, 
                                     points, 
                                     BOX_CONTAINS_POINT, 
                                     SpatialJoin.Duplicates.EXCLUDE);
            // Print points contained in box
            System.out.println(String.format("Points inside %s", box));
            while (iterator.hasNext()) {
                ExampleRecord record = iterator.next();
                System.out.println(String.format("    %s", record.spatialObject()));
            }

The iterator returns spatial objects from `points`, the second argument to the
spatial join. (There is no reason to return `Pair` objects, because the left
part of every pair would be the same query object.)

The example runs 5 queries for the same data set. To run the example
(exact results will differ):

        $ src/test/examples/points_in_box 
        Points inside (913802.0:923801.0, 454256.0:464255.0)
            (914856.0, 457038.0)
            (914733.0, 460286.0)
            (917114.0, 458769.0)
            (917084.0, 460012.0)
            (919723.0, 455706.0)
            (920704.0, 462415.0)
            (920952.0, 461800.0)
            (922320.0, 455902.0)
            (923100.0, 459578.0)
            (922532.0, 461437.0)
            (922380.0, 463585.0)
        Points inside (113254.0:123253.0, 832710.0:842709.0)
            (115443.0, 839890.0)
            (119119.0, 840991.0)
            (118426.0, 842531.0)
            (121833.0, 841634.0)
            (122308.0, 841260.0)
        Points inside (322956.0:332955.0, 898825.0:908824.0)
            (324159.0, 900132.0)
            (326677.0, 901713.0)
            (329265.0, 901457.0)
            (330675.0, 904247.0)
            (332428.0, 901001.0)
            (332606.0, 900930.0)
            (332891.0, 904158.0)
            (328246.0, 906392.0)
            (330033.0, 907402.0)
            (330479.0, 908126.0)
            (331725.0, 906492.0)
            (331660.0, 908755.0)
        Points inside (543685.0:553684.0, 619044.0:629043.0)
            (545674.0, 624489.0)
            (548309.0, 622728.0)
            (543778.0, 626348.0)
            (548836.0, 627232.0)
            (549439.0, 628060.0)
            (551537.0, 625792.0)
            (551633.0, 626626.0)
            (552856.0, 628017.0)
        Points inside (449049.0:459048.0, 238101.0:248100.0)
            (452099.0, 240735.0)
            (451890.0, 244515.0)
            (455139.0, 239030.0)
            (455306.0, 241785.0)
            (456368.0, 242123.0)
            (457867.0, 240053.0)
            (457873.0, 245254.0)