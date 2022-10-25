package measures;

import entity.Point;
import entity.RoadMap;
import entity.Trajectory;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author hanxi
 * @date 5/1/2022 12 28
 * discription
 */
public class NetEDR implements CalculateDistance{
    double threshold=1000;


    @Override
    public double GetDistance(Trajectory T1, Trajectory T2, RoadMap roadMap) {
        ArrayList<Point> ps1 = T1.points;
        ArrayList<Point> ps2 = T2.points;
        int m = T1.points.size(), n = T2.points.size();
        int[][] dp = new int[m + 1][n + 1];
        dp[0][0] = 0;
        for (int i = 1; i <= m; i++) dp[i][0] = 0;
        for (int j = 1; j <= n; j++) dp[0][j] = 0;

        Iterator<Point> it1 = ps1.iterator();
        for (int i = 1; i <= m; i++) {
            Point p1 = it1.next();
            Iterator<Point> it2 = ps2.iterator();
            for (int j = 1; j <= n; j++) {
                Point p2 = it2.next();
                int subcost = (Point.getLength(p1,p2,roadMap) <= threshold?0:1);
                dp[i][j] = Math.min(dp[i - 1][j - 1] + subcost, Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
            }
        }
        return dp[m][n];
    }
}
