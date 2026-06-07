import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.util.List;

import org.junit.jupiter.api.Test;

public class CentroidFinderAllTest {
    private static final double DELTA = 1e-9;

    @Test
    public void dfsFindsSingleConnectedBlockInFiveByFiveImage() {
        int[][] image = {
            {0, 0, 0, 0, 0},
            {0, 1, 1, 0, 0},
            {0, 1, 1, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0}
        };

        List<Group> groups = new DfsBinaryGroupFinder().findConnectedGroups(image);

        assertEquals(1, groups.size());
        assertEquals(new Group(4, new Coordinate(1, 1)), groups.get(0));
    }

    @Test
    public void dfsDoesNotConnectDiagonalPixelsInFiveByFiveImage() {
        int[][] image = {
            {0, 0, 0, 0, 0},
            {0, 1, 0, 0, 0},
            {0, 0, 1, 0, 0},
            {0, 0, 0, 1, 0},
            {0, 0, 0, 0, 0}
        };

        List<Group> groups = new DfsBinaryGroupFinder().findConnectedGroups(image);

        assertEquals(3, groups.size());
        assertEquals(new Group(1, new Coordinate(3, 3)), groups.get(0));
        assertEquals(new Group(1, new Coordinate(2, 2)), groups.get(1));
        assertEquals(new Group(1, new Coordinate(1, 1)), groups.get(2));
    }

    @Test
    public void dfsReturnsMultipleGroupsInDescendingOrderFromSixBySixImage() {
        int[][] image = {
            {1, 1, 0, 0, 0, 0},
            {1, 1, 0, 0, 0, 0},
            {1, 0, 0, 0, 0, 1},
            {0, 0, 0, 0, 1, 1},
            {0, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 0, 0}
        };

        List<Group> groups = new DfsBinaryGroupFinder().findConnectedGroups(image);

        assertEquals(3, groups.size());
        assertEquals(new Group(5, new Coordinate(0, 0)), groups.get(0));
        assertEquals(new Group(3, new Coordinate(4, 2)), groups.get(1));
        assertEquals(new Group(2, new Coordinate(2, 5)), groups.get(2));
    }

    @Test
    public void dfsFindsLargeConnectedShape() {
        int[][] image = {
            {1, 1, 1, 0, 0},
            {0, 1, 0, 0, 0},
            {0, 1, 1, 1, 0},
            {0, 0, 0, 1, 0},
            {0, 0, 0, 1, 1}
        };

        List<Group> groups = new DfsBinaryGroupFinder().findConnectedGroups(image);

        assertEquals(1, groups.size());
        assertEquals(new Group(10, new Coordinate(2, 1)), groups.get(0));
    }

    @Test
    public void dfsRejectsEmptyImage() {
        assertThrows(IllegalArgumentException.class, () ->
            new DfsBinaryGroupFinder().findConnectedGroups(new int[][] {})
        );
    }

    @Test
    public void dfsRejectsEmptyRows() {
        assertThrows(IllegalArgumentException.class, () ->
            new DfsBinaryGroupFinder().findConnectedGroups(new int[][] {{}}
        ));
    }

    @Test
    public void dfsRejectsJaggedImage() {
        int[][] image = {
            {1, 0},
            {1}
        };

        assertThrows(IllegalArgumentException.class, () ->
            new DfsBinaryGroupFinder().findConnectedGroups(image)
        );
    }

    @Test
    public void dfsRejectsValuesOtherThanZeroAndOne() {
        int[][] image = {
            {1, 2},
            {0, 1}
        };

        assertThrows(IllegalArgumentException.class, () ->
            new DfsBinaryGroupFinder().findConnectedGroups(image)
        );
    }

    @Test
    public void euclideanDistanceIsZeroForIdenticalColors() {
        EuclideanColorDistance finder = new EuclideanColorDistance();

        assertEquals(0.0, finder.distance(0x123456, 0x123456), DELTA);
    }

    @Test
    public void euclideanDistanceMatchesKnownPrimaryColors() {
        EuclideanColorDistance finder = new EuclideanColorDistance();
        double expected = Math.sqrt((255 * 255) + (255 * 255));

        assertEquals(expected, finder.distance(0xFF0000, 0x00FF00), DELTA);
    }

    @Test
    public void euclideanDistanceUsesAllThreeColorChannels() {
        EuclideanColorDistance finder = new EuclideanColorDistance();
        double expected = Math.sqrt(1 + 4 + 9);

        assertEquals(expected, finder.distance(0x000000, 0x010203), DELTA);
    }

    @Test
    public void toBinaryArrayMarksPixelsByDistanceThreshold() {
        RecordingDistanceFinder finder = new RecordingDistanceFinder();
        finder.register(0x000000, 0.0);
        finder.register(0x010101, 5.0);
        finder.register(0x020202, 10.0);
        finder.register(0x030303, 15.0);

        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(finder, 0xABCDEF, 10);
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0x000000);
        image.setRGB(1, 0, 0x010101);
        image.setRGB(0, 1, 0x020202);
        image.setRGB(1, 1, 0x030303);

        int[][] actual = binarizer.toBinaryArray(image);
        int[][] expected = {
            {1, 1},
            {0, 0}
        };

        assertBinaryImageEquals(expected, actual);
    }

    @Test
    public void toBinaryArrayMasksOutAlphaChannelBeforeDistanceCheck() {
        RecordingDistanceFinder finder = new RecordingDistanceFinder();
        finder.register(0x112233, 0.0);

        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(finder, 0x445566, 1);
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0x7F112233);

        int[][] binary = binarizer.toBinaryArray(image);

        assertEquals(0x112233, finder.lastColorA);
        assertEquals(1, binary[0][0]);
    }

    @Test
    public void toBinaryArrayTreatsDistanceEqualToThresholdAsBlack() {
        RecordingDistanceFinder finder = new RecordingDistanceFinder();
        finder.register(0xABCDEF, 5.0);

        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(finder, 0x010203, 5);
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0xABCDEF);

        int[][] binary = binarizer.toBinaryArray(image);

        assertEquals(0, binary[0][0]);
    }

    @Test
    public void toBufferedImageUsesInputDimensions() {
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new EuclideanColorDistance(), 0x000000, 1);
        int[][] binary = {
            {1, 0, 1},
            {0, 1, 0}
        };

        BufferedImage output = binarizer.toBufferedImage(binary);

        assertEquals(3, output.getWidth());
        assertEquals(2, output.getHeight());
    }

    @Test
    public void toBufferedImageMapsOneToWhiteAndZeroToBlack() {
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new EuclideanColorDistance(), 0x000000, 1);
        int[][] binary = {
            {1, 0},
            {0, 1}
        };

        BufferedImage output = binarizer.toBufferedImage(binary);

        assertEquals(0xFFFFFF, output.getRGB(0, 0) & 0xFFFFFF);
        assertEquals(0x000000, output.getRGB(1, 0) & 0xFFFFFF);
        assertEquals(0x000000, output.getRGB(0, 1) & 0xFFFFFF);
        assertEquals(0xFFFFFF, output.getRGB(1, 1) & 0xFFFFFF);
    }

    @Test
    public void toBufferedImageTreatsNonOneValuesAsBlack() {
        DistanceImageBinarizer binarizer =
            new DistanceImageBinarizer(new EuclideanColorDistance(), 0x000000, 1);
        int[][] binary = {
            {2}
        };

        BufferedImage output = binarizer.toBufferedImage(binary);

        assertEquals(0x000000, output.getRGB(0, 0) & 0xFFFFFF);
    }

    @Test
    public void imageGroupFinderPassesInputImageToBinarizer() {
        FakeImageBinarizer binarizer = new FakeImageBinarizer();
        FakeBinaryGroupFinder groupFinder = new FakeBinaryGroupFinder();
        binarizer.binaryToReturn = new int[][] {{1}};
        groupFinder.groupsToReturn = List.of(new Group(1, new Coordinate(0, 0)));
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);
        BufferedImage input = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        finder.findConnectedGroups(input);

        assertSame(input, binarizer.receivedImage);
    }

    @Test
    public void imageGroupFinderPassesBinarizedArrayToGroupFinder() {
        FakeImageBinarizer binarizer = new FakeImageBinarizer();
        FakeBinaryGroupFinder groupFinder = new FakeBinaryGroupFinder();
        int[][] expectedBinary = {
            {1, 0},
            {0, 1}
        };
        binarizer.binaryToReturn = expectedBinary;
        groupFinder.groupsToReturn = List.of();
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        finder.findConnectedGroups(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB));

        assertSame(expectedBinary, groupFinder.receivedBinaryImage);
    }

    @Test
    public void imageGroupFinderReturnsExactListFromGroupFinder() {
        FakeImageBinarizer binarizer = new FakeImageBinarizer();
        FakeBinaryGroupFinder groupFinder = new FakeBinaryGroupFinder();
        binarizer.binaryToReturn = new int[][] {{1}};
        List<Group> expectedGroups = List.of(new Group(3, new Coordinate(4, 5)));
        groupFinder.groupsToReturn = expectedGroups;
        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        List<Group> actualGroups =
            finder.findConnectedGroups(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));

        assertSame(expectedGroups, actualGroups);
    }

    private static void assertBinaryImageEquals(int[][] expected, int[][] actual) {
        assertEquals(expected.length, actual.length);
        for (int row = 0; row < expected.length; row++) {
            assertArrayEquals(expected[row], actual[row]);
        }
    }

    private static class RecordingDistanceFinder implements ColorDistanceFinder {
        private final java.util.Map<Integer, Double> distances = new java.util.HashMap<>();
        private double defaultDistance = 1000.0;
        int lastColorA = -1;

        @Override
        public double distance(int colorA, int colorB) {
            this.lastColorA = colorA;
            return distances.getOrDefault(colorA, defaultDistance);
        }

        void register(int color, double distance) {
            distances.put(color, distance);
        }
    }

    private static class FakeImageBinarizer implements ImageBinarizer {
        BufferedImage receivedImage;
        int[][] binaryToReturn;

        @Override
        public int[][] toBinaryArray(BufferedImage image) {
            this.receivedImage = image;
            return binaryToReturn;
        }

        @Override
        public BufferedImage toBufferedImage(int[][] image) {
            return null;
        }
    }

    private static class FakeBinaryGroupFinder implements BinaryGroupFinder {
        int[][] receivedBinaryImage;
        List<Group> groupsToReturn;

        @Override
        public List<Group> findConnectedGroups(int[][] image) {
            this.receivedBinaryImage = image;
            return groupsToReturn;
        }
    }
}
