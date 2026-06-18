import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
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
    public void parserRejectsShortHexColor() {
        VideoArgumentsParser parser = new VideoArgumentsParser();

        assertThrows(IllegalArgumentException.class, () ->
            parser.parse(new String[] {"input.mp4", "out.csv", "FFF", "164"})
        );
    }

    @Test
    public void parserRejectsLongHexColor() {
        VideoArgumentsParser parser = new VideoArgumentsParser();

        assertThrows(IllegalArgumentException.class, () ->
            parser.parse(new String[] {"input.mp4", "out.csv", "00FFA200", "164"})
        );
    }

    @Test
    public void parserRejectsEmptyHexColor() {
        VideoArgumentsParser parser = new VideoArgumentsParser();

        assertThrows(IllegalArgumentException.class, () ->
            parser.parse(new String[] {"input.mp4", "out.csv", "", "164"})
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
    public void parserRejectsNegativeThreshold() {
        VideoArgumentsParser parser = new VideoArgumentsParser();

        assertThrows(IllegalArgumentException.class, () ->
            parser.parse(new String[] {"input.mp4", "out.csv", "FFA200", "-1"})
        );
    }

    @Test
    public void parserRejectsEmptyThreshold() {
        VideoArgumentsParser parser = new VideoArgumentsParser();

        assertThrows(IllegalArgumentException.class, () ->
            parser.parse(new String[] {"input.mp4", "out.csv", "FFA200", ""})
        );
    }

    @Test
    public void parserRejectsTooLargeThreshold() {
        VideoArgumentsParser parser = new VideoArgumentsParser();

        assertThrows(IllegalArgumentException.class, () ->
            parser.parse(new String[] {"input.mp4", "out.csv", "FFA200", "999999999999"})
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

    @Test
    public void sectionTrackerMapsCentroidToNineSections() {
        FrameSectionTracker tracker = new FrameSectionTracker();

        assertEquals(
            FrameSectionTracker.Section.TOP_LEFT,
            tracker.sectionFor(new Coordinate(0, 0), 90, 90)
        );
        assertEquals(
            FrameSectionTracker.Section.CENTER,
            tracker.sectionFor(new Coordinate(45, 45), 90, 90)
        );
        assertEquals(
            FrameSectionTracker.Section.BOTTOM_RIGHT,
            tracker.sectionFor(new Coordinate(89, 89), 90, 90)
        );
    }

    @Test
    public void sectionTrackerCountsSecondsAndFindsMostVisitedSection() {
        FrameSectionTracker tracker = new FrameSectionTracker();

        tracker.record(new Coordinate(10, 10), 90, 90);
        tracker.record(new Coordinate(45, 45), 90, 90);
        tracker.record(new Coordinate(46, 46), 90, 90);

        assertEquals(1, tracker.secondsIn(FrameSectionTracker.Section.TOP_LEFT));
        assertEquals(2, tracker.secondsIn(FrameSectionTracker.Section.CENTER));
        assertEquals(FrameSectionTracker.Section.CENTER, tracker.mostVisitedSection());
    }

    @Test
    public void sectionTrackerIgnoresMissingCentroids() {
        FrameSectionTracker tracker = new FrameSectionTracker();

        tracker.record(new Coordinate(-1, -1), 90, 90);

        assertEquals(FrameSectionTracker.Section.NONE, tracker.mostVisitedSection());
    }

    @Test
    public void videoProcessorBuildsSectionSummaryPathBesideOutputCsv() {
        SecondBySecondVideoProcessor processor =
            new SecondBySecondVideoProcessor(new LargestCentroidFinder(image -> List.of()));

        Path summaryPath = processor.sectionSummaryPath("out/results.csv");

        assertEquals(Path.of("out/results-sections.csv"), summaryPath);
    }
}
