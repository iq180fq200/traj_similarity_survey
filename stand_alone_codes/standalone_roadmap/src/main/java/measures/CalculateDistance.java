package measures;

import entity.RoadMap;
import entity.Trajectory;

/**
 * @author hanxi
 * @date 4/29/2022 16 22
 * discription
 */
public interface CalculateDistance {
    double GetDistance(Trajectory T1, Trajectory T2, RoadMap roadMap) throws Exception;
}
