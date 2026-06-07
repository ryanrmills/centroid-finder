public class CliInputParser {
    private static final String HEX_COLOR_PATTERN = "[0-9a-fA-F]{6}";

    public int parseRgbHexColor(String value) {
        if (value == null || !value.matches(HEX_COLOR_PATTERN)) {
            throw new IllegalArgumentException("targetColor must be a hex color in RRGGBB format.");
        }
        return Integer.parseInt(value, 16);
    }

    public int parseNonNegativeThreshold(String value) {
        int threshold;
        try {
            threshold = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("threshold must be an integer.");
        }

        if (threshold < 0) {
            throw new IllegalArgumentException("threshold must be zero or greater.");
        }

        return threshold;
    }
}
