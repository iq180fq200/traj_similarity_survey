package measures;

import entity.Point;
import entity.RoadMap;
import entity.Trajectory;

import java.util.*;

/**
 * @author hanxi
 * @date 4/29/2022 16 41
 * discription
 */
//class Djstra{
//    double current_min;//current determined minimum distance
//    PriorityQueue<DjstraPoint> candidatePoints=new PriorityQueue<>();
//    HashMap<Point,DjstraPoint> djstraPointHashMap;
//
//    private class DjstraPoint implements Comparable<DjstraPoint>{
//        Point p;
//        boolean determined;
//        double current_distance;//current distance
//        DjstraPoint(Point p){
//            this.p=p;
//            determined=false;
//            current_distance=-1;
//        }
//
//
//        @Override
//        public int compareTo(DjstraPoint o) {
//            if (this.current_distance>o.current_distance)
//               return 1;
//            else if(this.current_distance<o.current_distance)
//                return -1;
//            else
//                return 0;
//        }
//    }
//
//    //get the min network distance from the point to a set of other points using Djstra algorithm
//    public double minDJstraDistance(Point sPoint, ArrayList<Point> points) throws Exception {
//        if(points.contains(sPoint))
//            return 0;
//
//        //change all the points into djstra points hashtable
//        djstraPointHashMap=new HashMap<>();
//        for(Point p:RoadMap.pointTable.values()){
//            djstraPointHashMap.put(p,new DjstraPoint(p));
//        }
//        //initialize parameters
//        boolean getResult=false;
//        DjstraPoint current=djstraPointHashMap.get(sPoint);
//        current.determined=true;
//        current.current_distance=0;
//        current_min=0;
//        AddCandidatePoints(current);
//        while(candidatePoints.size()!=0){
//            //get the next determined point
////            int minIndex=0;
////            double maxd=Double.MAX_VALUE;
////            for(int j=0;j<candidatePoints.size();j++)
////                if(candidatePoints.get(j).current_distance<maxd)
////                    minIndex=j;
////            current=candidatePoints.get(minIndex);
//            current=candidatePoints.poll();
//            current.determined=true;
//            current_min=current.current_distance;
//            //judge if current is the point in points
//            if(points.contains(current.p)){
//                getResult=true;
//                break;
//            }
//            //renew the distances and add new candidate point
//            AddCandidatePoints(current);
//        }
//        if(getResult)
//            return current_min;
//        else{
//            throw new Exception("could not get");
//        }
//    }
//    //add adjacent points or renew the distances of the adjacent points of the given point p
//    private void AddCandidatePoints(DjstraPoint current){
////        int size=current.p.connectPoints.size();
////        for(int i=0;i<size;i++){
////            DjstraPoint q=djstraPointHashMap.get(current.p.connectPoints.get(i));
////            if(q.current_distance==-1 && !q.determined){
////                q.current_distance=current.p.connectDistances.get(i)+current_min;
////                candidatePoints.add(q);
////            }
////            else if(!q.determined){
////                q.current_distance=Math.min((current_min+current.p.connectDistances.get(i)),q.current_distance);
////            }
////        }
////    }
//}



public class TP implements CalculateDistance {
    double lamda;
    public TP(double _lamda){
        lamda = _lamda;
    }

    @Override
    public double GetDistance(Trajectory T1, Trajectory T2, RoadMap roadMap) throws Exception {
        double totalExponent1 = 0, totalExponent2 = 0;
        for (Point p : T1.points) {
            double minDistance = Double.MAX_VALUE;
            for(Point q:T2.points){
                double d=Point.getLength(p,q,roadMap)/1000;
                if(d<minDistance)
                    minDistance=d;
            }
            totalExponent1 += Math.exp(-1 * minDistance);
        }
        for (Point p : T2.points){
            double minDistance = Double.MAX_VALUE;
            for(Point q:T1.points){
                double d=Point.getLength(p,q,roadMap)/1000;
                if(d<minDistance)
                    minDistance=d;
            }
            totalExponent2 += Math.exp(-1 * minDistance);
        }
        double totalTimeExponent1=0,totalTimeExponent2=0;
        double t1_min = T1.timestamps.get(0);
        double t2_min = T2.timestamps.get(0);
        for (double t1:T1.timestamps){
            Double minTimeDiatance = Double.MAX_VALUE;
            for (double t2:T2.timestamps){
                double d = Math.abs((t1-t1_min) - (t2 - t2_min));
                if (d < minTimeDiatance)
                    minTimeDiatance = d;
            }
            totalTimeExponent1 += Math.exp(-1*minTimeDiatance);
        }


        for (double t2:T2.timestamps){
            Double minTimeDiatance = Double.MAX_VALUE;
            for (double t1:T1.timestamps){
                double d = Math.abs((t2-t2_min) - (t1 - t1_min));
                if (d < minTimeDiatance)
                    minTimeDiatance = d;
            }
            totalTimeExponent2 += Math.exp(-1*minTimeDiatance);
        }


        return -(lamda*(totalExponent1 / T1.points.size() + totalExponent2 / T2.points.size())+(1-lamda)*
                (totalTimeExponent1/T1.points.size()+totalTimeExponent2/T2.points.size()));
    }
}
