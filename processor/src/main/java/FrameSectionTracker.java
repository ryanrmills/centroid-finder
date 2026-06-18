import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

public class FrameSectionTracker {
    private static final Coordinate NO_CENTROID = new Coordinate(-1, -1);
    private final Map<Section, Integer> counts = new EnumMap<>(Section.class);

    public FrameSectionTracker() {
        for (Section section : Section.values()) {
            counts.put(section, 0);
        }
    }

    public void record(Coordinate centroid, int width, int height) {
        if (centroid.equals(NO_CENTROID)) {
            return;
        }

        Section section = sectionFor(centroid, width, height);
        counts.put(section, counts.get(section) + 1);
    }

    public Section sectionFor(Coordinate centroid, int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
        if (centroid.x() < 0 || centroid.y() < 0 || centroid.x() >= width || centroid.y() >= height) {
            throw new IllegalArgumentException("centroid must be inside the image");
        }

        int col = Math.min((centroid.x() * 3) / width, 2);
        int row = Math.min((centroid.y() * 3) / height, 2);
        return Section.fromGrid(row, col);
    }

    public int secondsIn(Section section) {
        return counts.get(section);
    }

    public Section mostVisitedSection() {
        Section bestSection = Section.TOP_LEFT;
        int bestCount = counts.get(bestSection);

        for (Section section : Section.values()) {
            if (section == Section.NONE) {
                continue;
            }
            int currentCount = counts.get(section);
            if (currentCount > bestCount) {
                bestSection = section;
                bestCount = currentCount;
            }
        }

        return bestCount == 0 ? Section.NONE : bestSection;
    }

    public void writeSummary(Path outputPath) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(outputPath.toFile())) {
            writer.println("section,seconds");
            for (Section section : Section.values()) {
                if (section == Section.NONE) {
                    continue;
                }
                writer.printf("%s,%d%n", section.label(), counts.get(section));
            }

            Section winner = mostVisitedSection();
            int seconds = winner == Section.NONE ? 0 : counts.get(winner);
            writer.printf("most-time-section,%s,%d%n", winner.label(), seconds);
        }
    }

    public enum Section {
        TOP_LEFT("top-left"),
        TOP_CENTER("top-center"),
        TOP_RIGHT("top-right"),
        MIDDLE_LEFT("middle-left"),
        CENTER("center"),
        MIDDLE_RIGHT("middle-right"),
        BOTTOM_LEFT("bottom-left"),
        BOTTOM_CENTER("bottom-center"),
        BOTTOM_RIGHT("bottom-right"),
        NONE("none");

        private final String label;

        Section(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public static Section fromGrid(int row, int col) {
            return switch (row) {
                case 0 -> switch (col) {
                    case 0 -> TOP_LEFT;
                    case 1 -> TOP_CENTER;
                    case 2 -> TOP_RIGHT;
                    default -> throw new IllegalArgumentException("invalid column");
                };
                case 1 -> switch (col) {
                    case 0 -> MIDDLE_LEFT;
                    case 1 -> CENTER;
                    case 2 -> MIDDLE_RIGHT;
                    default -> throw new IllegalArgumentException("invalid column");
                };
                case 2 -> switch (col) {
                    case 0 -> BOTTOM_LEFT;
                    case 1 -> BOTTOM_CENTER;
                    case 2 -> BOTTOM_RIGHT;
                    default -> throw new IllegalArgumentException("invalid column");
                };
                default -> throw new IllegalArgumentException("invalid row");
            };
        }
    }
}
