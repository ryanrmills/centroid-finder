import java.awt.image.BufferedImage;
import java.util.List;

public class LargestCentroidFinder {
    private final ImageGroupFinder groupFinder;

    public LargestCentroidFinder(ImageGroupFinder groupFinder) {
        this.groupFinder = groupFinder;
    }

    public Coordinate findLargestCentroidOrDefault(BufferedImage image, Coordinate defaultValue) {
        List<Group> groups = groupFinder.findConnectedGroups(image);
        if (groups.isEmpty()) {
            return defaultValue;
        }
        return groups.get(0).centroid();
    }
}
