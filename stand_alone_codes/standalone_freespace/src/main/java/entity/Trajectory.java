package entity; /**
 * @author hanxi
 * @date 4/1/2022 19 16
 * discription
 */
import java.util.ArrayList;
import java.util.ListIterator;

public class Trajectory {
    public ArrayList<Point> points;


    @Override
    public String toString() {
        StringBuffer string = new StringBuffer();
        Point p;
        for(int i=0;i<points.size()-1;i++){
            p=points.get(i);
            string.append(p.x);
            string.append(',');
            string.append(p.y);
            string.append(';');
        }
        p=points.get(points.size()-1);
        string.append(p.x);
        string.append(',');
        string.append(p.y);
        return string.toString();
    }

    public Point getDataCenterPoint(){
        double[] boundary = {Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};
        for(Point p : this.points){
            if(p.x<boundary[0])
                boundary[0]=p.x;
            if(p.x>boundary[2])
                boundary[2] = p.x;
            if(p.y < boundary[1])
                boundary[1] = p.y;
            if (p.y >boundary[3])
                boundary[3] = p.y;
        }
        return new Point((boundary[0]+boundary[2])/2,(boundary[1]+boundary[3])/2);
    }


    public Trajectory(ArrayList<Point> points){
        this.points = points;
//        this.points = new ArrayList<Point>(points);
    }

    public Trajectory copy(){
        Trajectory trajectory2;
        ArrayList<Point> points2=new ArrayList<>();
        for(Point p : this.points){
            points2.add(new Point(p.x, p.y));
        }
        trajectory2=new Trajectory(points2);
        return trajectory2;
    }

    //scan and transfer to segments
    public ArrayList<Segment> GetSegments(){
        ArrayList<Segment> segments=new ArrayList<Segment>();
        ListIterator<Point> iter=points.listIterator();
        Point p1=iter.next();
        Point p2;
        while(iter.hasNext()){
            p2=iter.next();
            segments.add(new Segment(p1,p2));
            p1=p2;
        }
        return segments;
    }


    //************************for EDWP********************
    public class AnswerPoint{
        public  AnswerPoint(boolean _flag,Point _p){
            flag=_flag;
            p=_p;
        }
        public boolean flag;
        public Point p;
    }
    public AnswerPoint getFootPoint(Point splitPoint){
        boolean flag=true;
        Point p1=points.get(0);
        Point p2=points.get(1);
        if(Double.doubleToLongBits(Point.getLength(p1,p2)) ==Double.doubleToLongBits(0.0)){
            flag=false;
            return new AnswerPoint(flag,p2);
        }
        else{
            double k=-((p1.x-splitPoint.x)*(p2.x-p1.x)+(p1.y- splitPoint.y)*(p2.y-p1.y))/ (Math.pow(p1.y-p2.y,2)+Math.pow(p1.x-p2.x,2))*1.0;
            double xn=k*(p2.x-p1.x)+p1.x;
            double yn=k*(p2.y-p1.y)+p1.y;
            if(xn>=Math.max(p1.x,p2.x)||xn<=Math.min(p1.x,p2.x)){
                xn=p2.x;
                yn=p2.y;
                flag=false;
            }
            return new AnswerPoint(flag,new Point(xn,yn));
        }
    }

    public double getTotalLength(){
        double sum = 0;
        for (int i = 1; i < points.size(); ++i){
            sum += points.get(i).minus(points.get(i - 1)).norm2();
        }
        return sum;
    }

    public void insert(Point insertPoint){
        //find the position of the point
        Point start = points.remove(0);
        points.add(0,insertPoint);
        points.add(0,start);
    }

    public static Trajectory insert(Trajectory t1, Trajectory t2){
        Point projection = Point.project(t1.points.get(0), t1.points.get(1), t2.points.get(1));
        Trajectory t1_new = t1.copy();
        t1_new.insert(projection);
        return t1_new;
    }

    //***********************************************

    // Added by CGF TODO : DELETE THIS LINE!!!
    public Trajectory rest(){
        Trajectory rest_traj = this.copy();
        rest_traj.points.remove(0);
        return rest_traj;
    }

    public static double replace(Point e1p1, Point e1p2, Point e2p1, Point e2p2){
        double dist1 = e1p1.minus(e2p1).norm2();
        double dist2 = e1p2.minus(e2p2).norm2();
        return dist1 + dist2;
    }

    public static double coverage(Point e1p1, Point e1p2, Point e2p1, Point e2p2){
        double e1_len = e1p2.minus(e1p1).norm2();
        double e2_len = e2p2.minus(e2p1).norm2();
        return e1_len + e2_len;
    }
}






