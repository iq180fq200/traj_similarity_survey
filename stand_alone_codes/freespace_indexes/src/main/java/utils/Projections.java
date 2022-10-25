package utils;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

/**
 * @author hanxi
 * @date 5/6/2022 10 34
 * discription
 */
public class Projections {
    public static Double[] LongLat2Mercator(double lon,double lat){
        Double[] res = new Double[2];
        Coordinate tar = null;
        try {
            //封装点，这个是通用的，也可以用POINT（y,x）
            // private static WKTReader reader = new WKTReader( geometryFactory );
            Coordinate sour = new Coordinate(lat, lon);
            //这里要选择转换的坐标系是可以随意更换的
            CoordinateReferenceSystem source = CRS.decode("EPSG:4326");
            CoordinateReferenceSystem target = CRS.decode("EPSG:3857");
            //建立转换，下面两个我屏掉的转换方式会报出需要3/7参数的异常
            // MathTransform mathTransform = CRS.findMathTransform(source, target);
            //MathTransform mathTransform1 = CRS.findMathTransform(source, target, false);
            MathTransform transform = CRS.findMathTransform(source, target, true);
            tar = new Coordinate();
            //转换
            JTS.transform(sour, tar, transform);
        } catch (FactoryException | org.opengis.referencing.operation.TransformException e) {
            e.printStackTrace();
        }
        String[] split = (tar.toString().substring(1, tar.toString().length() - 1)).split(",");
        res[0]=Double.valueOf(split[0]);
        res[1]=Double.valueOf(split[1]);
        return res;
    }
}
