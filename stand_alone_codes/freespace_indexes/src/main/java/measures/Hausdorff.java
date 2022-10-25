package measures;

import entity.Point;
import entity.Trajectory;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author hanxi
 * @date 4/21/2022 17 30
 * discription
 */
public class Hausdorff implements CalDistance{
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
        return Double.max(hausdorffDistance_0,hausdorffDistance_1);

    }

    @Override
    public double GetDistance(Trajectory T1, Trajectory T2) {
        return hausdorffDistance(T1.points,T2.points);
    }

}
