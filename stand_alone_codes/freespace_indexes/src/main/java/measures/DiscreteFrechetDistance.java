package measures;// This class will compute the Discrete Frechet Distance given two sets of time
// series by using dynamic programming to increase performance.
//
// How to run: Run the file, and the console will ask for the first and second
// time series which should be supplied as a String. Each time series is given
// as pairs of values separated by semicolons and the values for each pair are
// separated by a comma.
//
// Pseudocode of computing DFD from page 5 of
// http://www.kr.tuwien.ac.at/staff/eiter/et-archive/cdtr9464.pdf
//
// @author - Stephen Bahr (sbahr@bu.edu)


import entity.Point;
import entity.Trajectory;

import java.util.ArrayList;

public class DiscreteFrechetDistance implements CalDistance {
    @Override
    public double GetDistance(Trajectory T1, Trajectory T2) {
        ArrayList<Point> p1=T1.points;
        ArrayList<Point> p2=T2.points;
        int m=p1.size();
        int n=p2.size();
        double[][] cost=new double[m][n];
        cost[0][0]=Point.getLength(p1.get(0),p2.get(0));
        for(int i=1;i<m;i++){
            cost[i][0]=Math.max(Point.getLength(p1.get(i),p2.get(0)),cost[i-1][0]);
        }
        for(int j=1;j<n;j++){
            cost[0][j]=Math.max(Point.getLength(p1.get(0),p2.get(j)),cost[0][j-1]);
        }
        for(int i=1;i<m;i++){
            for(int j=1;j<n;j++){
                cost[i][j]=Math.max(Point.getLength(p1.get(i),p2.get(j)),Math.min(cost[i-1][j-1],Math.min(cost[i][j-1],cost[i-1][j])));
            }
        }
        return cost[m-1][n-1];
    }

//    /** Dimensions of the time series */
//    private int DIM;
//    /** Dynamic programming memory array */
//    private double[][] mem;
//    /** First time series */
//    private ArrayList<Point> timeSeriesP;
//    /** Second time series */
//    private ArrayList<Point> timeSeriesQ;
//
//    /**
//     * Wrapper that makes a call to computeDFD. Initializes mem array with all
//     * -1 values.
//     *
//     * @param P - the first time series
//     * @param Q - the second time series
//     *
//     * @return The length of the shortest distance that can traverse both time
//     *         series.
//     */
//    private double computeDiscreteFrechet(ArrayList<Point> P, ArrayList<Point> Q) {
//        timeSeriesP=P;
//        timeSeriesQ=Q;
//
//        mem = new double[P.size()][Q.size()];
//
//        // initialize all values to -1
//        for (int i = 0; i < mem.length; i++) {
//            for (int j = 0; j < mem[i].length; j++) {
//                mem[i][j] = -1.0;
//            }
//        }
//
//        return computeDFD(P.size() - 1, Q.size() - 1);
//    }
//
//    /**
//     * Compute the Discrete Frechet Distance (DFD) given the index locations of
//     * i and j. In this case, the bottom right hand corner of the mem two-d
//     * array. This method uses dynamic programming to improve performance.
//     *
//     * Pseudocode of computing DFD from page 5 of
//     * http://www.kr.tuwien.ac.at/staff/eiter/et-archive/cdtr9464.pdf
//     *
//     * @param i - the row
//     * @param j - the column
//     *
//     * @return The length of the shortest distance that can traverse both time
//     *         series.
//     */
//    private double computeDFD(int i, int j) {
//
//        // if the value has already been solved
//        if (mem[i][j] > -1)
//            return mem[i][j];
//            // if top left column, just compute the distance
//        else if (i == 0 && j == 0)
//            mem[i][j] = euclideanDistance(timeSeriesP.get(i), timeSeriesQ.get(j));
//            // can either be the actual distance or distance pulled from above
//        else if (i > 0 && j == 0)
//            mem[i][j] = max(computeDFD(i - 1, 0), euclideanDistance(timeSeriesP.get(i), timeSeriesQ.get(j)));
//            // can either be the distance pulled from the left or the actual
//            // distance
//        else if (i == 0 && j > 0)
//            mem[i][j] = max(computeDFD(0, j - 1), euclideanDistance(timeSeriesP.get(i), timeSeriesQ.get(j)));
//            // can be the actual distance, or distance from above or from the left
//        else if (i > 0 && j > 0) {
//            mem[i][j] = max(min(computeDFD(i - 1, j), computeDFD(i - 1, j - 1), computeDFD(i, j - 1)), euclideanDistance(timeSeriesP.get(i), timeSeriesQ.get(j)));
//        }
//        // infinite
//        else
//            mem[i][j] = Integer.MAX_VALUE;
//
//        // printMemory();
//        // return the DFD
//        return mem[i][j];
//    }
//
//    /**
//     * Get the max value of all the values.
//     *
//     * @param values - the values being compared
//     *
//     * @return The max value of all the values.
//     */
//    private static double max(double... values) {
//        double max = Integer.MIN_VALUE;
//        for (double i : values) {
//            if (i >= max)
//                max = i;
//        }
//        return max;
//    }
//
//    /**
//     * Get the minimum value of all the values.
//     *
//     * @param values - the values being compared
//     *
//     * @return The minimum value of all the values.
//     */
//    private static double min(double... values) {
//        double min = Integer.MAX_VALUE;
//        for (double i : values) {
//            if (i <= min)
//                min = i;
//        }
//        return min;
//    }
//
//    /**
//     * Given two points, calculate the Euclidean distance between them, where
//     * the Euclidean distance: sum from 1 to n dimensions of ((x - y)^2)^1/2
//     *
//     * @param i - the first point
//     * @param j - the second point
//     *
//     * @return The total Euclidean distance between two points.
//     */
//    private static double euclideanDistance(Point i, Point j) {
//
//
//        return Point.getLength(i,j);
//    }
//
//    @Override
//    public double GetDistance(Trajectory T1, Trajectory T2) {
//        return computeDiscreteFrechet(T1.points,T2.points);
//    }
}