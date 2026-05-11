import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class DfsBinaryGroupFinderTest {
    @Test
    public void findsSingleConnectedBlockInFiveByFiveImage() {
        int[][] image = {
            {0, 0, 0, 0, 0},
            {0, 1, 1, 0, 0},
            {0, 1, 1, 0, 0},
            {0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0}
        };

        List<Group> groups = new DfsBinaryGroupFinder().findConnectedGroups(image);

        assertEquals(1, groups.size());
        assertEquals(new Group(4, new Coordinate(1, 1)), groups.get(0));
    }

    @Test
    public void doesNotConnectDiagonalPixelsInFiveByFiveImage() {
        int[][] image = {
            {0, 0, 0, 0, 0},
            {0, 1, 0, 0, 0},
            {0, 0, 1, 0, 0},
            {0, 0, 0, 1, 0},
            {0, 0, 0, 0, 0}
        };

        List<Group> groups = new DfsBinaryGroupFinder().findConnectedGroups(image);

        assertEquals(3, groups.size());
        assertEquals(new Group(1, new Coordinate(3, 3)), groups.get(0));
        assertEquals(new Group(1, new Coordinate(2, 2)), groups.get(1));
        assertEquals(new Group(1, new Coordinate(1, 1)), groups.get(2));
    }

    @Test
    public void returnsMultipleGroupsInDescendingOrderFromSixBySixImage() {
        int[][] image = {
            {1, 1, 0, 0, 0, 0},
            {1, 1, 0, 0, 0, 0},
            {1, 0, 0, 0, 0, 1},
            {0, 0, 0, 0, 1, 1},
            {0, 0, 0, 0, 0, 0},
            {0, 0, 1, 1, 0, 0}
        };

        List<Group> groups = new DfsBinaryGroupFinder().findConnectedGroups(image);

        assertEquals(3, groups.size());
        assertEquals(new Group(5, new Coordinate(0, 0)), groups.get(0));
        assertEquals(new Group(3, new Coordinate(4, 2)), groups.get(1));
        assertEquals(new Group(2, new Coordinate(2, 5)), groups.get(2));
    }
}
