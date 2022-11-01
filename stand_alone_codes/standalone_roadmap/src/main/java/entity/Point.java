package entity;
//import com.vividsolutions.jts.geom.Coordinate;
//import javax.measure.unit.NonSI;
//import org.jscience.geography.coordinates.LatLong;
//import org.jscience.geography.coordinates.UTM;
//import org.jscience.geography.coordinates.crs.ReferenceEllipsoid;

import java.util.ArrayList;
import java.util.Set;

/**
 * @author hanxi
 * @date 4/1/2022 19 15
 * discription
 */

public class Point {
//    //get the distance between two points
//    public static double getLength(Point p1,Point p2){
//        return GeoUtils.DistanceOfTwoPoints(p1.x,p1.y,p2.x,p2.y, GeoUtils.GaussSphere.WGS84);
//    }
    public Double x,y;
    public int osmID=-1;
    //no need to translate coordinate
    public Point (Double lat,Double lon,boolean flag){
        double[] t=Mercator.merc(lon,lat);
        this.x=t[0];
        this.y=t[1];
    }
    public Point (Double lat,Double lon,boolean flag,long osmID){
        double[] t=Mercator.merc(lon,lat);
        this.x=t[0];
        this.y=t[1];
        this.osmID=new Integer((int) osmID);
    }
    public Point (Double x,Double y){
        this.x=x;
        this.y=y;
    }
    public static double getLength(Point b1,Point b2,RoadMap roadmap){
        if (roadmap != null)
            return roadmap.distanceTable[b1.osmID][b2.osmID];
        else
            return Math.sqrt(Math.pow((b1.x - b2.x),2) + Math.pow((b1.y - b2.y),2));
    }
}

