public class VideoSummaryApp {
    public static void main(String[] args) {
        VideoArgumentsParser parser = new VideoArgumentsParser();

        VideoProcessingConfig config;
        try {
            config = parser.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println(parser.usage());
            return;
        }

        ColorDistanceFinder distanceFinder = new EuclideanColorDistance();
        ImageBinarizer binarizer =
            new DistanceImageBinarizer(distanceFinder, config.targetColor(), config.threshold());
        ImageGroupFinder groupFinder = new BinarizingImageGroupFinder(binarizer, new DfsBinaryGroupFinder());
        LargestCentroidFinder centroidFinder = new LargestCentroidFinder(groupFinder);
        SecondBySecondVideoProcessor processor = new SecondBySecondVideoProcessor(centroidFinder);

        try {
            processor.process(config);
            System.out.println("Done. CSV written to: " + config.outputCsvPath());
            System.out.println("Section summary written to: " + processor.sectionSummaryPath(config.outputCsvPath()));
        } catch (Exception e) {
            System.err.println("Error processing video: " + e.getMessage());
        }
    }
}
