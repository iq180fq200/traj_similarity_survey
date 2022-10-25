package measures;

import entity.Segment;
import entity.Trajectory;

import java.util.ArrayList;

/**
 * @author hanxi
 * @date 5/3/2022 20 45
 * discription
 */
public class NetFrechet implements CalDistance {
    @Override
    public double GetDistance(Trajectory T1, Trajectory T2) {
       ArrayList<Segment> s1=T1.GetSegments();
       ArrayList<Segment> s2=T2.GetSegments();
       int m=s1.size();
       int n=s2.size();
       double[][] cost=new double[m][n];
       cost[0][0]=s1.get(0).getDistance(s2.get(0));
       for(int i=1;i<m;i++){
           cost[i][0]=Math.max(s1.get(i).getDistance(s2.get(0)),cost[i-1][0]);
       }
        for(int j=1;j<n;j++){
            cost[0][j]=Math.max(s1.get(0).getDistance(s2.get(j)),cost[0][j-1]);
        }
        for(int i=1;i<m;i++){
            for(int j=1;j<n;j++){
                cost[i][j]=Math.max(s1.get(i).getDistance(s2.get(j)),Math.min(cost[i-1][j-1],Math.min(cost[i][j-1],cost[i-1][j])));
            }
        }
        return cost[m-1][n-1];
    }
}
