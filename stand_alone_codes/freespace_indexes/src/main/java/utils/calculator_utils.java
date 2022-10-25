package utils;

import entity.Point;

public class calculator_utils {
    public static Point get_ERP_reference(double[] boundary){
        return new Point((boundary[0]+boundary[2])/2,(boundary[1]+boundary[3])/2,true);
    }
}
