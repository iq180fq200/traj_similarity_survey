package entity;

/**
 * @author hanxi
 * @date 4/12/2022 10 51
 * discription
 */
public class Segment {
    public Point s;
    public Point e;
    public Integer sID;
    public Double length;
    public Segment(Point s,Point e,double length,Integer sID){
        this.s=s;
        this.e=e;
        this.length=length;
        this.sID=sID;
    }
    //get intersect point,if not exist, return null
    public Point getIntersect(Segment other){
        return lineIntersect(s.x,s.y,e.x,e.y,other.s.x,other.s.y,other.e.x,other.e.y);
    }

    private static Point lineIntersect(Double x1, Double y1, Double x2, Double y2, Double x3, Double y3, Double x4, Double y4) {
        Double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (denom == 0.0) { // Lines are parallel.
            return null;
        }
        Double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3))/denom;
        Double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3))/denom;
        if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f) {
            // Get the intersection point.
            return new Point((x1 + ua*(x2 - x1)), (y1 + ua*(y2 - y1)));
        }
        return null;
    }

    public double pointToSegmentDistance(Point p){
        double dist=pointToLine(s.x,s.y,e.x,e.y,p.x,p.y);

        return dist;
    }
    private double pointToLine(double x1, double y1, double x2, double y2, double x0,
                               double y0) {
        double space = 0;
        double a, b, c;
        a = lineSpace(x1, y1, x2, y2);// 线段的长度
        b = lineSpace(x1, y1, x0, y0);// (x1,y1)到点的距离
        c = lineSpace(x2, y2, x0, y0);// (x2,y2)到点的距离
        if (c <= 0.000001 || b <= 0.000001) {
            space = 0;
            return space;
        }
        if (a <= 0.000001) {
            space = b;
            return space;
        }
        if (c * c >= a * a + b * b) {
            space = b;
            return space;
        }
        if (b * b >= a * a + c * c) {
            space = c;
            return space;
        }
        double p = (a + b + c) / 2;// 半周长
        double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));// 海伦公式求面积
        space = 2 * s / a;// 返回点到线的距离（利用三角形面积公式求高）
        return space;
    }

    // 计算两点之间的距离
    private double lineSpace(double x1, double y1, double x2, double y2) {
        double lineLength = 0;
        lineLength = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2)
                * (y1 - y2));
        return lineLength;
    }
}
