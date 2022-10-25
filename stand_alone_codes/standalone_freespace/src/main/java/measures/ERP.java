package measures;

import entity.Point;
import entity.Trajectory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author hanxi
 * @date 4/23/2022 10 06
 * discription
 */
public class ERP implements CalDistance {
    public Point gap=null;
    public ERP(Point gap){
        this.gap = gap;
    }
    @Override
    public double GetDistance(Trajectory T1, Trajectory T2) {
        ArrayList<Point> ps1 = T1.points;
        ArrayList<Point> ps2 = T2.points;
        int m = T1.points.size(), n = T2.points.size();

        //get the gap point
//        Point gap = new Point(-103.0,44.0,true);


        Map<Point, Double> mapGapDist = new HashMap<>();
        ps1.forEach(point -> {
            mapGapDist.put(point, Point.getLength(point,gap));
        });
        ps2.forEach(point -> {
            mapGapDist.put(point, Point.getLength(point,gap));
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
                dp[i][j] = Math.min(dp[i - 1][j - 1] + Point.getLength(p1,p2),
                        Math.min(dp[i - 1][j] + mapGapDist.get(p1), dp[i][j - 1] + mapGapDist.get(p2))
                );
            }
        }
        return dp[m][n];
    }
}
