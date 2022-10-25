package measures;

import entity.Point;
import entity.Trajectory;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author hanxi
 * @date 4/20/2022 13 52
 * discription
 */
public class EDR implements CalDistance{
    public double threshold=100;

    public EDR(Double _threshold){
        threshold = _threshold;
    }

    @Override
    public double GetDistance(Trajectory T1, Trajectory T2) {
        ArrayList<Point> ps1 = T1.points;
        ArrayList<Point> ps2 = T2.points;
        int m = T1.points.size(), n = T2.points.size();
        int[][] dp = new int[m + 1][n + 1];
        dp[0][0] = 0;
        for (int i = 1; i <= m; i++) dp[i][0] = i;
        for (int j = 1; j <= n; j++) dp[0][j] = j;


        for (int i = 1; i <= m; i++) {
            Point p1 = ps1.get(i-1);
            for (int j = 1; j <= n; j++) {
                Point p2 = ps2.get(j-1);
                int subcost;
                if(Point.getLength(p1,p2) <= threshold){
                    subcost=0;
                }
                else subcost=1;
                dp[i][j] = Math.min(dp[i - 1][j - 1] + subcost, Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
            }
        }
        return dp[m][n];
    }
}
