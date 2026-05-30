import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.util.List;

import org.junit.jupiter.api.Test;

public class VideoProcessorComponentsTest {
    @Test
    public void parserAcceptsValidCliArguments() {
        VideoArgumentsParser parser = new VideoArgumentsParser();

        VideoProcessingConfig config =
            parser.parse(new String[] {"input.mp4", "out.csv", "FFA200", "164"});

        assertEquals("input.mp4", config.inputPath());
        assertEquals("out.csv", config.outputCsvPath());
        assertEquals(0xFFA200, config.targetColor());
        assertEquals(164, config.threshold());
    }

    @Test
    public void parserRejectsWrongNumberOfArguments() {
        VideoArgumentsParser parser = new VideoArgumentsParser();

        assertThrows(IllegalArgumentException.class, () ->
            parser.parse(new String[] {"input.mp4", "out.csv", "FFA200"})
        );
    }

    @Test
    public void parserRejectsNonHexColor() {
        VideoArgumentsParser parser = new VideoArgumentsParser();

        assertThrows(IllegalArgumentException.class, () ->
            parser.parse(new String[] {"input.mp4", "out.csv", "not-a-color", "164"})
        );
    }

    @Test
    public void parserRejectsNonIntegerThreshold() {
        VideoArgumentsParser parser = new VideoArgumentsParser();

        assertThrows(IllegalArgumentException.class, () ->
            parser.parse(new String[] {"input.mp4", "out.csv", "FFA200", "high"})
        );
    }

    @Test
    public void largestCentroidFinderReturnsDefaultWhenNoGroupsExist() {
        ImageGroupFinder groupFinder = image -> List.of();
        LargestCentroidFinder finder = new LargestCentroidFinder(groupFinder);
        Coordinate fallback = new Coordinate(-1, -1);

        Coordinate result = finder.findLargestCentroidOrDefault(
            new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB),
            fallback
        );

        assertEquals(fallback, result);
    }

    @Test
    public void largestCentroidFinderReturnsFirstGroupCentroid() {
        ImageGroupFinder groupFinder = image -> List.of(
            new Group(10, new Coordinate(7, 4)),
            new Group(5, new Coordinate(1, 1))
        );
        LargestCentroidFinder finder = new LargestCentroidFinder(groupFinder);

        Coordinate result = finder.findLargestCentroidOrDefault(
            new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB),
            new Coordinate(-1, -1)
        );

        assertEquals(new Coordinate(7, 4), result);
    }
}
