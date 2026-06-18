import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

public class SecondBySecondVideoProcessor {
    private static final Coordinate NO_CENTROID = new Coordinate(-1, -1);
    private final LargestCentroidFinder centroidFinder;

    public SecondBySecondVideoProcessor(LargestCentroidFinder centroidFinder) {
        this.centroidFinder = centroidFinder;
    }

    public void process(VideoProcessingConfig config) throws Exception {
        validatePaths(config);

        try (
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(config.inputPath());
            Java2DFrameConverter converter = new Java2DFrameConverter();
            PrintWriter writer = openWriter(config.outputCsvPath())
        ) {
            FrameSectionTracker sectionTracker = new FrameSectionTracker();
            grabber.start();
            processFrames(grabber, converter, writer, sectionTracker);
            if (writer.checkError()) {
                throw new IOException("Could not finish writing CSV: " + config.outputCsvPath());
            }
            sectionTracker.writeSummary(sectionSummaryPath(config.outputCsvPath()));
        }
    }

    private void validatePaths(VideoProcessingConfig config) throws IOException {
        Path inputPath = Path.of(config.inputPath());
        if (!Files.isRegularFile(inputPath) || !Files.isReadable(inputPath)) {
            throw new FileNotFoundException("Input video is missing or unreadable: " + config.inputPath());
        }

        Path outputPath = Path.of(config.outputCsvPath());
        Path parent = outputPath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }

    private PrintWriter openWriter(String outputCsvPath) throws FileNotFoundException {
        return new PrintWriter(outputCsvPath);
    }

    private void processFrames(
        FFmpegFrameGrabber grabber,
        Java2DFrameConverter converter,
        PrintWriter writer,
        FrameSectionTracker sectionTracker
    ) throws Exception {
        long nextSecondToWrite = 0;
        boolean wroteFrame = false;
        Frame frame;

        while ((frame = grabber.grabImage()) != null) {
            long currentSecond = grabber.getTimestamp() / 1_000_000L;
            if (currentSecond < nextSecondToWrite) {
                continue;
            }

            while (nextSecondToWrite < currentSecond) {
                writeRow(writer, nextSecondToWrite, NO_CENTROID);
                nextSecondToWrite++;
            }

            BufferedImage image = converter.convert(frame);
            Coordinate centroid = NO_CENTROID;
            if (image != null) {
                centroid = centroidFinder.findLargestCentroidOrDefault(image, NO_CENTROID);
                sectionTracker.record(centroid, image.getWidth(), image.getHeight());
            }

            writeRow(writer, nextSecondToWrite, centroid);
            nextSecondToWrite++;
            wroteFrame = true;
        }

        if (!wroteFrame) {
            throw new IllegalArgumentException("Input video did not contain readable image frames.");
        }
    }

    private void writeRow(PrintWriter writer, long second, Coordinate centroid) {
        writer.printf("%d,%d,%d%n", second, centroid.x(), centroid.y());
    }

    public Path sectionSummaryPath(String outputCsvPath) {
        Path outputPath = Path.of(outputCsvPath);
        Path fileNamePath = outputPath.getFileName();
        String fileName = fileNamePath == null ? outputCsvPath : fileNamePath.toString();
        String sectionFileName;

        if (fileName.endsWith(".csv")) {
            sectionFileName = fileName.substring(0, fileName.length() - 4) + "-sections.csv";
        } else {
            sectionFileName = fileName + "-sections.csv";
        }

        Path parent = outputPath.getParent();
        return parent == null ? Path.of(sectionFileName) : parent.resolve(sectionFileName);
    }
}
