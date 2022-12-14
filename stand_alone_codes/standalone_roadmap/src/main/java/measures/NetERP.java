package measures;

import entity.Point;
import entity.RoadMap;
import entity.Trajectory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author hanxi
 * @date 4/29/2022 16 23
 * discription
 */
public class NetERP implements CalculateDistance{
    private Long reference_id;
    private Point reference_point = null;
    public NetERP(Long reference_id){
        this.reference_id = reference_id;
    }
    public NetERP(Point point){
        this.reference_point = point;
    }
    @Override
    public double GetDistance(Trajectory T1, Trajectory T2, RoadMap roadMap) {
        ArrayList<Point> ps1 = T1.points;
        ArrayList<Point> ps2 = T2.points;
        int m = T1.points.size(), n = T2.points.size();

//get the gap point
        Point gap;
        if(reference_point != null){
            gap = reference_point;
        }
        else{
            gap = roadMap.pointTable.get(reference_id);
        }



        Map<Point, Double> mapGapDist = new HashMap<>();
        ps1.forEach(point -> {
            mapGapDist.put(point, Point.getLength(point,gap,roadMap));
        });
        ps2.forEach(point -> {
            mapGapDist.put(point, Point.getLength(point,gap,roadMap));
        });
        double[][] dp = new double[m + 1][n + 1];
        dp[0][0] = 0;
        double accu = 0;
        Iterator<Point> it1 = ps1.iterator();
        for (int i = 1; i <= m; i++) {
            Point p = it1.next();
            accu += mapGapDist.get(p);
            dp[i][0] = accu;
        }
        accu = 0;
        Iterator<Point> it2 = ps2.iterator();
        for (int j = 1; j <= n; j++) {
            Point p = it2.next();
            accu += mapGapDist.get(p);
            dp[0][j] = accu;
        }

        it1 = ps1.iterator();
        for (int i = 1; i <= m; i++) {
            Point p1 = it1.next();
            it2 = ps2.iterator();
            for (int j = 1; j <= n; j++) {
                Point p2 = it2.next();
                dp[i][j] = Math.min(dp[i - 1][j - 1] + Point.getLength(p1,p2,roadMap),
                        Math.min(dp[i - 1][j] + mapGapDist.get(p1), dp[i][j - 1] + mapGapDist.get(p2))
                );
            }
        }
        return dp[m][n];
    }
}
