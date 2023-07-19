import entity.Point;
import entity.Segment;
import entity.Trajectory;
import measures.CalculateDistance;
import measures.*;
import java.util.ArrayList;
import java.util.HashMap;

public class test {
    public static void main(String[] args) throws Exception {

        String T1 = "[(1,3,1s),(3,6,3s),(4,2,4s),(6,4,6s),(7,3,9s)]";
        String T2 = "[(1,7,2s),(1,4,4s),(3,4,6s),(5,5,7s),(8,5,9s),(8,7,11s)]";

        Trajectory traj = get_string_traj(T1);
        Trajectory query = get_string_traj(T2);

        //*********ground truth*********************
        String[] methods = {"LCRS",
                "LORS",
                "NetEDR",
                "NetERP",
                "NetLCSS",
                "TP",
                "NetDTW"};
//        String[] methods = {"TP"};
        for(String item: methods){
            CalculateDistance calculator;
            switch (item){
                case "LCRS" : calculator = new LCRS();break;
                case "LORS" : calculator = new LORS();break;
                case "NetEDR" : calculator = new NetEDR(1.0);break;
                case "NetERP" : calculator = new NetERP(new Point(3.0,4.0));break;
                case "NetLCSS" : calculator = new NetLCSS(1.0);break;
                case "TP" : calculator = new TP(0.2);break;
                case "NetDTW" : calculator = new NetDTW();break;
                default:calculator = null;
            }
            double dist = calculator.GetDistance(query,traj,null);
            System.out.println(item+" : "+Double.toString(dist));
        }
    };
    static Trajectory get_string_traj(String _T){
        _T = _T.replace("[(","").replace(")]","");
        String[] s_points = _T.split("\\),\\(");
        //input the trajectories
        ArrayList<Point> points = new ArrayList<>();
        ArrayList<Segment> segments = new ArrayList<>();
        ArrayList<Double> times = new ArrayList<>();
        for(String item : s_points){
            String[] ss = item.split(",");
            int x = Integer.valueOf(ss[0]);
            int y = Integer.valueOf(ss[1]);
            double time = Double.valueOf(ss[2].replace("s",""));
            points.add(new Point((double)x,(double) y));
            times.add(time);
            if(points.size() > 1){
                Point p1 = points.get(points.size() - 2);
                Point p2 = points.get(points.size() - 1);
                double dist = Point.getLength(p1,p2,null);
                Integer seg_id = get_seg_id(x,y);
                segments.add(new Segment(p1,p2,dist,seg_id));
            }
        }
        return new Trajectory(points,segments,times);
    }
    static class Key{
        int x, y;
        Key(int a,int b){
            x = a;
            y = b;
        }

        @Override
        public boolean equals(Object obj) {
            return ((Key) obj).x == this.x && ((Key) obj).y == this.y;
        }
    }
    static HashMap<Key, Integer> segments = new HashMap<Key, Integer>();
    static int count = 0;

    static Integer get_seg_id(int x,int y){
        Key key = new Key(x,y);
        if(segments.containsKey(key))
            return segments.get(key);
        else{
            segments.put(key,count++);
            return count - 1;
        }

    }
}
