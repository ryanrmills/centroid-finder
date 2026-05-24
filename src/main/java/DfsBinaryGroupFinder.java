import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DfsBinaryGroupFinder implements BinaryGroupFinder {
   /**
    * Finds connected pixel groups of 1s in an integer array representing a binary image.
    * 
    * The input is a non-empty rectangular 2D array containing only 1s and 0s.
    * If the array or any of its subarrays are null, a NullPointerException
    * is thrown. If the array is otherwise invalid, an IllegalArgumentException
    * is thrown.
    *
    * Pixels are considered connected vertically and horizontally, NOT diagonally.
    * The top-left cell of the array (row:0, column:0) is considered to be coordinate
    * (x:0, y:0). Y increases downward and X increases to the right. For example,
    * (row:4, column:7) corresponds to (x:7, y:4).
    *
    * The method returns a list of sorted groups. The group's size is the number 
    * of pixels in the group. The centroid of the group
    * is computed as the average of each of the pixel locations across each dimension.
    * For example, the x coordinate of the centroid is the sum of all the x
    * coordinates of the pixels in the group divided by the number of pixels in that group.
    * Similarly, the y coordinate of the centroid is the sum of all the y
    * coordinates of the pixels in the group divided by the number of pixels in that group.
    * The division should be done as INTEGER DIVISION.
    *
    * The groups are sorted in DESCENDING order according to Group's compareTo method.
    * 
    * @param image a rectangular 2D array containing only 1s and 0s
    * @return the found groups of connected pixels in descending order
    */
    @Override
    public List<Group> findConnectedGroups(int[][] image) {
        Set<String> visited = new HashSet<>();
        List<Group> groupList = new ArrayList<>();

        int[][] directions = {
            {-1, 0},
            {1, 0},
            {0, -1},
            {0, 1}
        };

        for (int r = 0; r < image.length; r++) {
            for (int c = 0; c < image[r].length; c++) {
                String startKey = c + " " + r;
                if (image[r][c] == 1 && !visited.contains(startKey)) {
                    int groupSize = 0;
                    int xSum = 0;
                    int ySum = 0;

                    ArrayDeque<Coordinate> queue = new ArrayDeque<>();
                    queue.add(new Coordinate(c, r));
                    visited.add(startKey);

                    while (!queue.isEmpty()) {
                        Coordinate current = queue.removeFirst();
                        int x = current.x();
                        int y = current.y();

                        groupSize++;
                        xSum += x;
                        ySum += y;

                        for (int[] direction : directions) {
                            int nextX = x + direction[0];
                            int nextY = y + direction[1];
                            String nextKey = nextX + " " + nextY;

                            if (
                                nextY >= 0 && nextY < image.length &&
                                nextX >= 0 && nextX < image[nextY].length &&
                                image[nextY][nextX] == 1 &&
                                !visited.contains(nextKey)
                            ) {
                                visited.add(nextKey);
                                queue.add(new Coordinate(nextX, nextY));
                            }
                        }
                    }

                    int xAvg = xSum / groupSize;
                    int yAvg = ySum / groupSize;
                    groupList.add(new Group(groupSize, new Coordinate(xAvg, yAvg)));
                }
            }
        }

        groupList.sort(null);
        Collections.reverse(groupList);
        return groupList;
    }
}
