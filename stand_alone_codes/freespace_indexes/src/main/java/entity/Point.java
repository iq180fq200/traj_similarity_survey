package entity;


import static utils.Projections.LongLat2Mercator;

/**
 * @author hanxi
 * @date 4/1/2022 19 15
 * discription
 */

public class Point {
    //get the distance between two points
    public static double getLength(Point b1,Point b2){
        double a=Math.sqrt(Math.pow(b1.x-b2.x,2)+Math.pow(b1.y-b2.y,2));
        return a;
    }
    public Double x,y;
    //no need to translate coordinate
    public Point (Double x,Double y){
        this.x=x;
        this.y=y;
    }

    //need to translate coordinate
    public Point(Double x,Double y,boolean flag){
        Double[] t= LongLat2Mercator(x,y);
        this.x=t[0];
        this.y=t[1];
    }

    // Added by CGF TODO : DELETE THIS LINE!!!

    /**
     * Get the "Vector".
     * @return The vector end. (origin: 0,0)
     */
    public static Point minus(Point p1, Point p2){
        return new Point(p1.x - p2.x, p1.y - p2.y);
    }

    public Point minus(Point p){
        return new Point (this.x - p.x, this.y - p.y);
    }

    public static Point sum(Point p1, Point p2){
        return new Point(p1.x + p2.x, p1.y + p2.y);
    }

    public double norm2() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public static double product(Point v1, Point v2){
        return v1.x * v2.x + v1.y * v2.y;
    }

    public Point scale(double scalar){
        assert (scalar >= 0 && scalar <= 1);
        return new Point(scalar * this.x, scalar * this.y);
    }

    /**
     * Project a point to a segment
     * Note point are also used as vectors.
     * @param a start of segment
     * @param b end of segment
     * @param p the point
     * @return projection point
     */
    public static Point project(Point a, Point b, Point p){
        Point ap = minus(a, p); // Vec
        Point ab = minus(a, b); // Vec
        if (ab.norm2() == 0){
            return new Point(p.x, p.y);
        }
        double proj_scale = product(ap, ab) / Math.pow(ab.norm2(), 2);
        if (proj_scale < 0) {
            proj_scale = 0; // Note ! If point out of line, then skip
        }
        else if (proj_scale > 1) proj_scale = 1;
        return sum(a, ab.scale(proj_scale));
    }
}

