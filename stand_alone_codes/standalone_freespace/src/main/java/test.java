import entity.Point;
import entity.Trajectory;
import measures.*;

import java.util.ArrayList;

public class test {
    public static void main(String[] args){

        String T1 = "[(1,3,1s),(3,6,3s),(4,2,4s),(6,4,6s),(7,3,9s)]";
        String T2 = "[(1,7,2s),(1,4,4s),(3,4,6s),(5,5,7s),(8,5,9s),(8,7,11s)]";

        Trajectory traj = get_string_traj(T1);
        Trajectory query = get_string_traj(T2);

        //*********ground truth*********************
        String[] methods={"DTW",
                "LCSS",
                "EDR",
                "ERP",
                "Frechet",
                "Hausdorff",
                "OWD",
                "LIP",
                "EDwP",
                "Seg-Frechet"};
        for(String item: methods){
            CalDistance calculator;
            switch (item){
                case "LCSS" : calculator = new LCSS(1.0);break;
                case "DTW" : calculator = new DTW();break;
                case "EDR" : calculator = new EDR(1.0);break;
                case "ERP" : calculator = new ERP(new Point(4.0,4.0));break;
                case "Frechet" : calculator = new DiscreteFrechetDistance();break;
                case "Hausdorff" : calculator = new Hausdorff();break;
                case "OWD" : calculator = new OWD();break;
                case "LIP" : calculator = new LIP();break;
                case "EDwP" : calculator = new EDWP_DP();break;
                case "Seg-Frechet":calculator = new NetFrechet();break;
                default:calculator = null;
            }
            double dist = calculator.GetDistance(query,traj);
            System.out.println(item+" : "+Double.toString(dist));
        }
    };
    static Trajectory get_string_traj(String _T){
        _T = _T.replace("[(","").replace(")]","");
        String[] s_points = _T.split("\\),\\(");
        //input the trajectories
        ArrayList<Point> points = new ArrayList<>();
        for(String item : s_points){
            String[] ss = item.split(",");
            double x = Double.valueOf(ss[0]);
            double y = Double.valueOf(ss[1]);
            points.add(new Point(x,y));
        }
        return new Trajectory(points);

    }
}
