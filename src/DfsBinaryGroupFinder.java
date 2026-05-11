import java.util.ArrayList;
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
        //have a set of visited
        Set<String> visited = new HashSet<>();
        List<Group> groupList = new ArrayList<>();
        
        for (int r = 0; r < image.length; r++){
            for (int c = 0; c < image[r].length; c++){
                String coordString = "" + c + " " + r;
                if (image[r][c] == 1 && !visited.contains(coordString)){
                    List<Integer> stats = new ArrayList<>();
                    stats.add(0);
                    stats.add(0);
                    stats.add(0);

                    stats = findGroupsHelper(
                        stats,
                        image, 
                        visited,
                        coordString, 
                        new Coordinate(c, r)
                    );
                    int groupSize = stats.get(0);
                    int xAvg = stats.get(1) / groupSize;
                    int yAvg = stats.get(2) / groupSize;
                    groupList.add(new Group(groupSize, new Coordinate(xAvg, yAvg)));
                }
            }
        }
        
        groupList.sort(null);
        Collections.reverse(groupList);
        return groupList;
    }

    //helper method
    public List<Integer> findGroupsHelper(
        List<Integer> stats,
        int[][] image, 
        Set<String> visited,
        String coordString,
        Coordinate coordinate
    ){
        if (visited.contains(coordString)){
            return stats;
        }

        visited.add(coordString);
        stats.set(0, stats.get(0) + 1);
        stats.set(1, stats.get(1) + coordinate.x());
        stats.set(2, stats.get(2) + coordinate.y());

        //coords
        int[][] coords = {
            {-1, 0},
            {1, 0},
            {0, -1},
            {0, 1}
        };

        for (int[] coord : coords){
            //not less than image length && not greater than or equal to image length
            //not less than image row length && not greater than or equal to image row length
            int xCoord = coordinate.x() + coord[1];
            int yCoord = coordinate.y() + coord[0];
            String newCoordString = "" + xCoord + " " + yCoord;
            if (
                yCoord >= 0 && yCoord < image.length &&
                xCoord >= 0 && xCoord < image[coordinate.y()].length &&
                image[yCoord][xCoord] == 1 &&
                !visited.contains(newCoordString)
            ){
                stats = findGroupsHelper(stats, image, visited, newCoordString, new Coordinate(xCoord, yCoord));
            }
        }

        return stats;
    }
}
