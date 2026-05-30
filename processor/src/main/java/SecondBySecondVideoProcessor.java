import java.awt.image.BufferedImage;
import java.io.PrintWriter;

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
        try (
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(config.inputPath());
            Java2DFrameConverter converter = new Java2DFrameConverter();
            PrintWriter writer = new PrintWriter(config.outputCsvPath())
        ) {
            grabber.start();

            long nextSecondToWrite = 0;
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
                }

                writeRow(writer, nextSecondToWrite, centroid);
                nextSecondToWrite++;
            }
        }
    }

    private void writeRow(PrintWriter writer, long second, Coordinate centroid) {
        writer.printf("%d,%d,%d%n", second, centroid.x(), centroid.y());
    }
}
