package measures;

import entity.RoadMap;
import entity.Trajectory;

/**
 * @author hanxi
 * @date 4/29/2022 16 40
 * discription
 */
public class LORS implements CalculateDistance{
    @Override
    public double GetDistance(Trajectory T1, Trajectory T2, RoadMap roadMap) {
        int m=T1.segments.size();
        int n=T2.segments.size();
        if(m==0 || n==0)
            return 0;
        double[][] dpLORS=new double[m][n];
        if(T1.segments.get(0).sID==T2.segments.get(0).sID)
            dpLORS[0][0]=T1.segments.get(0).length;
        else
            dpLORS[0][0]=0;
        for(int i=1;i<m;i++){
            if(T1.segments.get(i).sID==T2.segments.get(0).sID)
                dpLORS[i][0]=T2.segments.get(0).length;
            else
                dpLORS[i][0]=dpLORS[i-1][0];
        }
        for(int i=1;i<n;i++){
            if(T2.segments.get(i).sID==T1.segments.get(0).sID)
                dpLORS[0][i]=T1.segments.get(0).length;
            else
                dpLORS[0][i]=dpLORS[0][i-1];
        }
        for(int i=1;i<m;i++){
            for(int j=1;j<n;j++){
                if(T1.segments.get(i).sID==T2.segments.get(j).sID)
                    dpLORS[i][j]=T1.segments.get(i).length+dpLORS[i-1][j-1];
                else
                    dpLORS[i][j]=Math.max(dpLORS[i-1][j],dpLORS[i][j-1]);//the larger LORS, the larger similarity
            }
        }
//        System.out.println(dpLORS[m-1][n-1]);
        return -dpLORS[m-1][n-1];
    }

//    public double GetDistance(Trajectory T1, Trajectory T2, RoadMap roadMap) {
//        return 5.0;
//    }

}
