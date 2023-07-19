package measures;

import entity.Point;
import entity.Trajectory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static java.lang.Math.max;

/**
 * @author hanxi
 * @date 4/21/2022 17 30
 * discription
 */
public class Hausdorff implements CalDistance{
    private static double getMax(double[]array){
        double max = -Double.MAX_VALUE;

        for (double num : array) {
            if (num > max) {
                max = num;
            }
        }
        return max;
    }

    private static void printArray(double[] array) {
        System.out.println("=======================");
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i]);
            System.out.print(",");
        }
        System.out.println();
        System.out.println("=======================");
    }

    private static void printArray(ArrayList<Double> array) {
        System.out.println("=======================");
        for (int i = 0; i < array.size(); i++) {
            System.out.print(array.get(i));
            System.out.print(",");
        }
        System.out.println();
        System.out.println("=======================");
    }
    public static double hausdorffDistance(ArrayList<Point> series1, ArrayList<Point> series2){



        ArrayList<Double> distances2 = new ArrayList<>(); //An arraylist that will contain all of the minimum distances
        ArrayList<Double> distances3 = new ArrayList<>();

        double minDis0 = 0.0;
        double minDis1 = 0.0;


        for (int i = 0; i <series1.size(); i++) {
            ArrayList<Double> distances = new ArrayList<>(); //Contains all of the distances for a particular set of calculations
            for(int j = 0; j < series2.size(); j++) {
                double dist = Point.getLength(series1.get(i),series2.get(j)); //Using Euclidean distance (C^2 = A^2 + B^2)
                distances.add(dist);
            }
            minDis0 = Collections.min(distances);
            distances2.add(minDis0);
        }

        double hausdorffDistance_0 = Collections.max(distances2);

        for (int i = 0; i <series2.size(); i++) {
            ArrayList<Double> distances = new ArrayList<>(); //Contains all of the distances for a particular set of calculations
            for(int j = 0; j < series1.size(); j++) {
                double dist = Point.getLength(series2.get(i),series1.get(j)); //Using Euclidean distance (C^2 = A^2 + B^2)
                distances.add(dist);
            }
            minDis1 = Collections.min(distances);
            distances3.add(minDis1);
        }
        double hausdorffDistance_1 = Collections.max(distances3);
//        printArray(distances2);
//        printArray(distances3);
        return Double.max(hausdorffDistance_0,hausdorffDistance_1);

    }

    @Override
    public double GetDistance(Trajectory T1, Trajectory T2) {
        int m = T1.points.size(), n = T2.points.size();
        double distanceIJ;
        double[]minDisT12T2 = new double[m];
        double[]minDisT22T1 = new double[n];
        double maxT12T2 = - Double.MAX_VALUE;
        double maxT22T1 = - Double.MAX_VALUE;
        for(int i = 0; i < m; i++){
            minDisT12T2[i] = Double.MAX_VALUE;
        }
        for(int j = 0; j < n; j++){
            minDisT22T1[j] = Double.MAX_VALUE;
        }
        for(int i = 0; i < m; i++){
            for(int j = 0; j < n;j++){
                distanceIJ = Point.getLength(T1.points.get(i),T2.points.get(j));
                if(distanceIJ < minDisT12T2[i]){
                    minDisT12T2[i] = distanceIJ;
                }
                if(distanceIJ < minDisT22T1[j]){
                    minDisT22T1[j] = distanceIJ;
                }
            }
        }
//        printArray(minDisT12T2);
//        printArray(minDisT22T1);
//        System.out.println(max(getMax(minDisT12T2),getMax(minDisT12T2)));
//        System.out.println(hausdorffDistance(T1.points,T2.points));
        return max(getMax(minDisT12T2),getMax(minDisT12T2));
    }

}
