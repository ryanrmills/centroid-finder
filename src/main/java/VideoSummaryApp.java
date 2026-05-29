import java.io.PrintWriter;
import java.awt.image.BufferedImage;
import java.util.List;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.Frame;

public class VideoSummaryApp {
    public static void main(String[] args){
        if (args.length < 4){
            //ending the whole function if there's not enough arguments
            System.out.println("Usage: java -jar videoprocessor.jar <inputPath> <outputCsv> <hex_target_color> <threshold>");
            return;
        }

        String inputVideoPath = args[0]; //grabbing the first argument
        String outputCsv = args[1];

        int targetColor;
        int threshold;

        try {
            targetColor = Integer.parseInt(args[2], 16); //turn it into a hexadecimal
            threshold = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            System.err.println("Color must be RRGGBB hex and threshold must be integers.");
            return;
        }

        // if (frameStep <= 0){
        //     System.err.println("frame_step must be > 0");
        //     return;
        // }

        ColorDistanceFinder distanceFinder = new EuclideanColorDistance();
        ImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, targetColor, threshold);
        ImageGroupFinder groupFinder = new BinarizingImageGroupFinder(binarizer, new DfsBinaryGroupFinder());

        try (
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputVideoPath);
            Java2DFrameConverter converter = new Java2DFrameConverter();
            PrintWriter writer = new PrintWriter(outputCsv);
        ) {
            grabber.start();
            writer.println("frame_index,timestamp_s,group_count,largest_group_size,centroid_x,centroid_y");
            int frameIndex = 0;
            Frame frame;
            
            long lastSavedMs = -1000;
            
            while ((frame = grabber.grabImage()) != null){
                long timestampMs = grabber.getTimestamp() / 1000L;
                double timestampS = timestampMs / 1000.0;

                if (timestampMs - lastSavedMs < 1000){
                    continue;
                }
                lastSavedMs = timestampMs;

                BufferedImage image = converter.convert(frame);
                if (image == null){
                    continue;
                }

                List<Group> groups = groupFinder.findConnectedGroups(image);
                if (groups.isEmpty()){
                    writer.printf("%d,%.3f,0,0,-1,-1%n", frameIndex, timestampS);
                } else {
                    Group largest = groups.get(0);
                    writer.printf(
                        "%d,%.3f,%d,%d,%d,%d%n",
                        frameIndex,
                        timestampS,
                        groups.size(),
                        largest.size(),
                        largest.centroid().x(),
                        largest.centroid().y()
                    );
                }
                frameIndex++;
            }

            System.out.println("Done. CSV written to: " + outputCsv);
        } catch (Exception e){
            System.err.println("Error processing video.");
            e.printStackTrace();
        }
    }
}
