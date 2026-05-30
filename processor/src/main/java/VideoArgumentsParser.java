public class VideoArgumentsParser {
    public VideoProcessingConfig parse(String[] args) {
        if (args.length != 4) {
            throw new IllegalArgumentException("Expected exactly 4 arguments.");
        }

        String inputPath = args[0];
        String outputCsvPath = args[1];

        int targetColor;
        try {
            targetColor = Integer.parseInt(args[2], 16);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("targetColor must be a hex color in RRGGBB format.");
        }

        int threshold;
        try {
            threshold = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("threshold must be an integer.");
        }

        return new VideoProcessingConfig(inputPath, outputCsvPath, targetColor, threshold);
    }

    public String usage() {
        return "Usage: java -jar videoprocessor.jar <inputPath> <outputCsv> <targetColor> <threshold>";
    }
}
