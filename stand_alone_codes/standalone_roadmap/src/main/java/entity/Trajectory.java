package entity; /**
 * @author hanxi
 * @date 4/1/2022 19 16
 * discription
 */
import java.util.ArrayList;
import java.util.ListIterator;

public class Trajectory {
    public ArrayList<Point> points;
    public ArrayList<Segment> segments;
    public ArrayList<Double> timestamps;

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

    public Trajectory(ArrayList<Point> points,ArrayList<Segment> segments,ArrayList<Double>_timestamps){
        this.points=points;
        this.segments=segments;
        this.timestamps = _timestamps;
    }




    public Trajectory(ArrayList<Integer> segmentIDs,ArrayList<Double>_timestamps, RoadMap roadMap){
        //************get the segments**************
        segments=new ArrayList<>();
        //get segments
        for(Integer id:segmentIDs)
            segments.add(roadMap.segmentTable.get(id));
        //get points
        points=new ArrayList<>();
        for (Segment seg:segments){
            points.add(seg.s);
        }
        if(segments.size()!=0)
            points.add(segments.get(segments.size()-1).e);
        timestamps = _timestamps;


    }




}






