package entity;

/**
 * @author hanxi
 * @date 4/13/2022 11 38
 * discription
 */
public class Grid {
    public static final double minLong=-180;
    public static final double minLat=-90;
    public static final double gridGrain=0.001;
    public int x,y;
    public Grid(int x,int y){
        this.x=x;
        this.y=y;
    }
    public Grid(double longitude,double latitude){
        this.x=(int) Math.ceil((longitude-minLong)/gridGrain);
        this.y=(int) Math.ceil((latitude-minLat)/gridGrain);
    }
}
