package measures;//package measures;

import entity.Point;
import entity.Segment;
import entity.Trajectory;

import java.util.ArrayList;
import java.util.List;

public class OWD implements CalDistance {

    @Override
    public double GetDistance(Trajectory T1, Trajectory T2) {
        double t1OWD=getOWD(T1.points,T2.points,T2.GetSegments());
//        System.out.println();
        double t2OWD=getOWD(T2.points,T1.points,T1.GetSegments());


        return (t1OWD+t2OWD)/2;
    }

    private double getOWD(ArrayList<Point> t, ArrayList<Point> tt,ArrayList<Segment> ttl){
        double owd=0;
        for (int i=0;i<t.size();i++) {
            owd += minDistance(t.get(i),tt,ttl);
        }

        return owd / t.size();
    }

    private double minDistance(Point p, List<Point> t, List<Segment> l) {
        double min = Point.getLength(p,t.get(0));
        for (int i=0; i<t.size(); i++) {
            double temp = Point.getLength(p,t.get(i));
            if (temp < min) {
                min = temp;
            }
        }
        for (int i=0;i<l.size();i++) {
            double temp = l.get(i).pointToSegmentDistance(p);
            if (temp < min) {
                min = temp;
            }
        }
//        System.out.print(min+"+");
        return min;
    }

}