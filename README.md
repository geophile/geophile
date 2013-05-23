# Geophile

Geophile is a Java implementation of *spatial join*. Given two sets of
spatial objects, *R* and *S*, the spatial join is the set of pairs, \(*x*, *y*), 
such that *x* belongs to *R*, *y* belongs to *S*, and *x*
and *y* overlap.

This implementation is built on two abstractions:

* **Index:** Any key/value data structure supporting both random and
sequential access. Binary trees, skiplists, and btrees are examples of
indexes that are compatible with Geophile. Hash tables are not,
because they do not provide for sequential access in key
order. 

* **Spatial Object:** Any kind of spatial object, in any number of
dimensions. The implementation must provide a method that determines the
relationship of the object to a box: the box is outside the spatial
object; the box is contained by the spatial object; or the box
overlaps the spatial object.

These implementations can be mixed arbitrarily, (as long as all the
spatial objects involved in a spatial join must have the same number
of dimensions). For example, you could compute the spatial join of a
b-tree containing millions of polygons with an in-memory sorted array
containing a small number of points. The output would contain the
(point, polygon) pairs for which the point is inside the polygon.

## Installation

Geophile can be built from source using [maven](http://maven.apache.org):

        mvn install

This creates `target/geophile-1.0.jar`.

To create Javadoc files:

        mvn javadoc:javadoc

These files are located in `target/site/apidocs`.

## Concepts

In addition to Index and Spatial Object, described above, Geophile
relies on the following concepts.

### Space

In order to use Geophile, you must describe the *space* in which spatial
objects reside by giving the number of cells in each dimension, e.g.

        Space space = Space.newSpace(1_000_000, 1_000_000);

This creates a 2-dimensional space in which the lower-left corner is
(0, 0), and the upper-right corner is (999999, 999999).

The arguments to `Space.newSpace` are longs, so the maximum number of
cells in a dimension is `Long.MAX_VALUE`.

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
between `com.geophile.z.Index` and `java.util.TreeMap`.

* `com.geophile.z.spatialobject.d2.Point`: A 2d point.

* `com.geophile.z.spatialobject.d2.Box`: A 2d box.

To run the examples, set the `CLASSPATH` to contain
`target/geophile-1.0.jar` and `target/test-classes`.

### Find all the points in a given box

`PointsInBox` loads 1,000,000 points in one spatial index, a single
query box in another, and then does a spatial join to locate all the
points within the box.

The space is created as follows:

        Space space = Space.newSpace(SPACE_X, SPACE_Y);
        ...
        private static final int SPACE_X = 1_000_000;
        private static final int SPACE_Y = 1_000_000;

The spatial index with 1,000,000 points is created and loaded as follows:

        SpatialIndex<Point> points = 
            SpatialIndex.newSpatialIndex(space, new TreeIndex<Point>());
        for (int i = 0; i < N_POINTS; i++) {
            points.add(randomPoint());
        }
        ...
        private static final int N_POINTS = 1_000_000;

The spatial index with a query box is created and loaded as follows:

        SpatialIndex<Box> box = 
            SpatialIndex.newSpatialIndex(space, new TreeIndex<Box>());
        Box query = randomBox();
        box.add(query);

The spatial join output is created and scanned as follows:

            Iterator<Pair<Box, Point>> iterator =
                SpatialJoin.newSpatialJoin(BOX_CONTAINS_POINT, 
                                           SpatialJoin.Duplicates.EXCLUDE)
                           .iterator(box, points);
            while (iterator.hasNext()) {
                Pair<Box, Point> pointInBox = iterator.next();
                ...
            }

The example runs 5 queries for the same data set. To run the example:

        $ src/test/examples/points_in_box 
        Points inside (82078:84077, 740534:742533)
            (83230, 741182)
            (82783, 741386)
            (83198, 741478)
            (83377, 742178)
        Points inside (846457:848456, 156619:158618)
            (846473, 157085)
            (846665, 157147)
            (847076, 157252)
            (847979, 157426)
            (848306, 157305)
            (848156, 157784)
            (848269, 157807)
        Points inside (105162:107161, 922561:924560)
            (105993, 922817)
            (106286, 923679)
            (106641, 923434)
        Points inside (511381:513380, 932274:934273)
            (511527, 933712)
            (512707, 932378)
            (513057, 932959)
            (513160, 932908)
        Points inside (738161:740160, 280379:282378)
            (739058, 281866)
            (739995, 281111)
            (740117, 281418)

### Find all overalpping pairs of boxes

`OverlappingPairs` loads two spatial indexes, each with 1,000,000 tiny
boxes, and then finds all the overlapping pairs.

The spatial indexes are created and loaded as follows:

        SpatialIndex<Box> left = 
            SpatialIndex.newSpatialIndex(space, new TreeIndex<Box>());
        SpatialIndex<Box> right = 
            SpatialIndex.newSpatialIndex(space, new TreeIndex<Box>());
        for (int i = 0; i < N_BOXES; i++) {
            left.add(randomBox());
            right.add(randomBox());
        }
        ...
        private static final int N_BOXES = 1_000_000;

The spatial join output is created and scanned as follows:

        Iterator<Pair<Box, Box>> iterator =
            SpatialJoin.newSpatialJoin(BOX_OVERLAP, 
                                       SpatialJoin.Duplicates.EXCLUDE)
                       .iterator(left, right);
        while (iterator.hasNext()) {
            Pair<Box, Box> overlappingPair = iterator.next();
            ...
        }

To run the example:

        $ src/test/examples/overlapping_pairs 
        Overlapping pairs
            ((40847:40848, 247657:247658), (40846:40847, 247656:247657))
            ((358925:358926, 256877:256878), (358925:358926, 256877:256878))
            ((269488:269489, 517640:517641), (269488:269489, 517640:517641))
            ((218240:218241, 690284:690285), (218241:218242, 690283:690284))
            ((586603:586604, 78305:78306), (586604:586605, 78304:78305))
            ((577376:577377, 304065:304066), (577376:577377, 304064:304065))
            ((553172:553173, 482781:482782), (553172:553173, 482782:482783))
            ((654695:654696, 413812:413813), (654694:654695, 413812:413813))
            ((968053:968054, 24216:24217), (968054:968055, 24217:24218))
            ((977679:977680, 337249:337250), (977678:977679, 337250:337251))
            ((670178:670179, 643354:643355), (670178:670179, 643355:643356))
