public class VideoArgumentsParser {
    private final CliInputParser cliInputParser;

    public VideoArgumentsParser() {
        this(new CliInputParser());
    }

    public VideoArgumentsParser(CliInputParser cliInputParser) {
        this.cliInputParser = cliInputParser;
    }

    public VideoProcessingConfig parse(String[] args) {
        if (args.length != 4) {
            throw new IllegalArgumentException("Expected exactly 4 arguments.");
        }

        String inputPath = args[0];
        String outputCsvPath = args[1];
        int targetColor = cliInputParser.parseRgbHexColor(args[2]);
        int threshold = cliInputParser.parseNonNegativeThreshold(args[3]);

        return new VideoProcessingConfig(inputPath, outputCsvPath, targetColor, threshold);
    }

    public String usage() {
        return "Usage: java -jar videoprocessor.jar <inputPath> <outputCsv> <targetColor> <threshold>";
    }
}
