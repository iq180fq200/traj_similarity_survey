package measures;

import entity.RoadMap;
import entity.Segment;
import entity.Trajectory;

/**
 * @author hanxi
 * @date 4/29/2022 16 41
 * discription
 */
public class LCRS implements CalculateDistance{
    @Override
    public double GetDistance(Trajectory T1, Trajectory T2, RoadMap roadMap) {
        double LORSScore=-(new LORS().GetDistance(T1,T2,roadMap));
        double T1length=0,T2length=0;
        for(Segment seg:T1.segments)
            T1length+=seg.length;
        for(Segment seg:T2.segments)
            T2length+=seg.length;
        return -(LORSScore/(T1length+T2length-LORSScore));
    }

//    public double GetDistance(Trajectory T1, Trajectory T2, RoadMap roadMap) {
//        return 5.0;
//
//    }
}
