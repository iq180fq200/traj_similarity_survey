package measures;

import entity.Point;
import entity.Polygon;
import entity.Segment;
import entity.Trajectory;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author hanxi
 * @date 4/20/2022 14 22
 * discription
 */
public class LIP implements CalDistance{
    Trajectory T1,T2;
    ArrayList<Segment> sT1;
    ArrayList<Segment> sT2;
    final int maxGap=0;

    @Override
    public double GetDistance(Trajectory T1, Trajectory T2) {
        this.T1=T1;
        this.T2=T2;
        sT1=T1.GetSegments();
        sT2=T2.GetSegments();
        return LIPGen(maxGap);
    }
    private Double LIPGen(final int maxGap){
        if(sT1.size()==0 || sT2.size()==0)
            return null;
        int sT1size=sT1.size();
        int sT2size=sT2.size();
        //accumulate for result value
        Double acc=new Double(0.0);
        int sit1=0;//the pointer of the current dealing segment in sT1
        int sit2=0;
        int sub1begin=sit1;//the point to the current subsequence of good segments
        int sub1end=sit1;
        int sub2begin=sit2;
        int sub2end=sit2;
        int gapSize=0;//the current size of gap(bad segments num)
        do{
            //if the bad segments are too much
            if(gapSize>maxGap){
                //Add the result for the subsequences of ~good~ segments just before the gap and
                //start new subsequences just after the gap.
                acc+=getLIPDistance(sub1begin,sub1end,sub2begin,sub2end); //sub1/2end is not included
                sub1begin=sit1;
                sub2begin=sit2;
                sub1end=sit1;
                sub2end=sit2;
                gapSize=0;
            }
            if(gapSize==0){
                if(sT1.get(sit1).getIntersect(sT2.get(sit2))!=null || !BadEndSeg(sub1begin, sit1, sub2begin, sit2)){
                    sub1end=sit1+1;
                    sub2end=sit2+1;

                }else{
                    gapSize=1;
                }
            }
            else{
                if(!BadEndSeg(sub1begin, sit1, sub2begin, sit2)){
                    sub1end=sit1+1;
                    sub2end=sit2+1;
                    gapSize=0;
                }
                else{
                    gapSize++;
                }
            }


        }while(++sit1!=sT1size && ++sit2!=sT2size);

        acc+=getLIPDistance(sub1begin,sT1size,sub2begin,sT2size);
        return acc;
    }
    private boolean BadEndSeg(final int sub1begin,final int trysub1end,final int sub2begin,final int trysub2end){
        /*
        If both sequences contain just one segment, there is nothing the segment
        between the end points could intersect.
        */
        if (sub1begin==trysub1end && sub2begin==trysub2end)
            return false;
        /*
        According to article, "the effect of the bad segments is local and can be
        treated by testing only a small number of segments for intersection." This
        implementation arbitrarily uses 3 as that small number.
            */
        final int max_look_back = 100000;
        /*
        Create the segment that is defined by the end points of the two sequences.
        */
        final Segment testSeg=new Segment(sT1.get(trysub1end).e,sT2.get(trysub2end).e);
        for(int i=1;i<=max_look_back;i++) {
            if (testSeg.getIntersect(sT1.get(trysub1end - i)) != null ||
                    testSeg.getIntersect(sT2.get(trysub2end - i)) != null
            ) {
                return true;
            }
            if (trysub1end - i == 0 || trysub2end - i == 0)
                break;
        }
        return false;
    }
    private double getLIPDistance(int sit1begin,int sit1end,int sit2begin,int sit2end){
        //the first segment on the second sequence that has not yet been included in a polygon
        int sit2mark=sit2begin;
        ArrayList<Point> list1,list2;//the point of the polygon that comes from segment1,2
        list1=new ArrayList<>();
        list2=new ArrayList<>();
        double totalDis=0;
        double dis=0;
        double area=0;
        double t=0;
        //iterate all segments for the first sequence
        for(int i=sit1begin;i<sit1end;i++){
            list1.add(sT1.get(i).s);
            //iterate second sequnce to find any cross point
            for(int j=sit2begin;j<sit2end;j++){
                Point cross=sT1.get(i).getIntersect(sT2.get(j));
                if(cross!=null){
                    for(int k=sit2mark;k<=j;k++){
                        list2.add(sT2.get(k).s);
                    }
                    list1.add(cross);
                    list2.add(cross);
                    //add the polygon into the polygons
                    Collections.reverse(list2);
                    list1.addAll(list2);
                    Polygon poly=new Polygon(list1);
                    area=poly.calculateArea();
//                    System.out.println(area);
                    dis=poly.calculatePerimeter();
                    totalDis+=dis;
                    t+=area*dis;
                    //reset the lists
                    list1.clear();
                    list2.clear();
                    list1.add(cross);
                    list2.add(cross);
                    sit2mark=j+1;
                    break;
                }
            }
        }
        /*
        There were no more intersections. Add the start points of the remaining segments
        between $sit2mark$ and the end of the second sequence to $list2$. Add the end
        points of the two sequences to the point lists. Close the last polygon and add
        it to the list of polygons.

        */
        for(int sit2b=sit2mark;sit2b!=sit2end;sit2b++){
            list2.add(sT2.get(sit2b).s);
        }
        list1.add(sT1.get(sit1end-1).e);
        list2.add(sT2.get(sit2end-1).e);
        //add the polygon into the polygons
        Collections.reverse(list2);
        list1.addAll(list2);
        Polygon poly=new Polygon(list1);
        area=poly.calculateArea();
//        System.out.println(area);
        dis=poly.calculatePerimeter();
        totalDis+=dis;
        t+=area*dis;

        //get the lip
        return t/totalDis;
    }

}
