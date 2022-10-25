package measures;

import entity.Trajectory;

/**
 * @author hanxi
 * @date 4/1/2022 17 25
 * description
 */
public class EDWP_Recursive implements CalDistance{
    public double GetDistance(Trajectory T1, Trajectory T2){
        if (T1 == null || T2 == null)
            return Double.POSITIVE_INFINITY;
        else if (T1.points.size() == 1 && T2.points.size() == 1) {
            return 0;
        }
        else if (T1.points.size() == 1 || T2.points.size() == 1){
            return Double.POSITIVE_INFINITY;
        }

        assert (T1.points.size() > 0 && T2.points.size() > 0);

        double case1 = GetDistance(T1.rest(), T2.rest());
        case1 += Trajectory.replace(T1.points.get(0),
                T1.points.get(1),
                T2.points.get(0),
                T2.points.get(1))
                *
                Trajectory.coverage(T1.points.get(0),
                        T1.points.get(1),
                        T2.points.get(0),
                        T2.points.get(1));

        double case2 = getInsertDist(Trajectory.insert(T1, T2), T2);
        if (case2 == 0) return 0;

        double case3 = getInsertDist(T1, Trajectory.insert(T2, T1));
        if (case3 == 0) return 0;

        return Math.min(case1, Math.min(case2, case3));
        //        return dp_EDwP(T1, T2);
    }

    public double getInsertDist(Trajectory t1, Trajectory t2){
        if (t1 == null || t2 == null){
            return Double.POSITIVE_INFINITY;
        }
        else if (t1.points.size() == 1 && t2.points.size() == 1){
            return 0;
        }
        else if (t1.points.size() == 1 || t2.points.size() == 1){
            return Double.POSITIVE_INFINITY;
        }
        // Only calculate case1.
        double case1 =  GetDistance(t1.rest(), t2.rest());
        case1 += Trajectory.replace(t1.points.get(0),
                t1.points.get(1),
                t2.points.get(0),
                t2.points.get(1))
                *
                Trajectory.coverage(t1.points.get(0),
                        t1.points.get(1),
                        t2.points.get(0),
                        t2.points.get(1));

        return case1;
    }
}
