package measures;

import entity.Point;
import entity.Trajectory;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author hanxi
 * @date 4/20/2022 11 27
 * discription
 */
public class DTW implements CalDistance{
    @Override
    public double GetDistance(Trajectory T1, Trajectory T2) {
        ArrayList<Point> ps1 = T1.points;
        ArrayList<Point> ps2 = T2.points;
        int m = T1.points.size(), n = T2.points.size();
        double[][] dp = new double[m + 1][n + 1];
        dp[0][0] = 0;
        for (int i = 1; i <= m; i++) dp[i][0] = Double.MAX_VALUE;
        for (int j = 1; j <= n; j++) dp[0][j] = Double.MAX_VALUE;

        Iterator<Point> it1 = ps1.iterator();
        for (int i = 1; i <= m; i++) {
            Point p1 = it1.next();
            Iterator<Point> it2 = ps2.iterator();
            for (int j = 1; j <= n; j++) {
                Point p2 = it2.next();
                dp[i][j] = Point.getLength(p1,p2) +
                        Math.min(Math.min(dp[i][j - 1], dp[i - 1][j]), dp[i - 1][j - 1]);
            }
        }
        return dp[m][n];
    }
}
