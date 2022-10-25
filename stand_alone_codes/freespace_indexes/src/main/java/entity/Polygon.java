package entity; /**
 * @author hanxi
 * @date 4/8/2022 15 01
 * discription
 */
import java.util.ArrayList;
import java.util.ListIterator;

public class Polygon {
    ArrayList<Point> points;
    public Polygon(ArrayList<Point> _points){
        points=_points;
    }
    public Polygon(){

    }
    public double calculateArea(){
        double area = 0.0d;

        //第一点坐标（x0,y0）
        double x0 = points.get(0).x;
        double y0 = points.get(0).y;

        double ax=points.get(1).x;
        double ay=points.get(1).y;
        double by,bx;
        bx=by=0;
        ListIterator<Point> iter=points.listIterator(2);
        Point t;
        while(iter.hasNext()){
            t=iter.next();
            bx=t.x;
            by=t.y;
            //向量a
            double va_x = ax - x0;
            double va_y = ay - y0;

            //向量b
            double vb_x = bx - x0;
            double vb_y = by - y0;
            //叉乘
            area += va_x * vb_y - vb_x * va_y;

            ax=bx;
            ay=by;
        }

        return Math.abs(area / 2);
    }
    public double calculatePerimeter(){
        double perimeter=0.0;
        Point a=points.get(0);
        ListIterator<Point> iter=points.listIterator(1);
        Point b=null;
        while(iter.hasNext()){
            b=iter.next();
            perimeter+=Point.getLength(a,b);
            a=b;
        }
        return perimeter;
    }
    public void clearPolygon(){
        points.clear();
    }
}
