import entity.Point;
import entity.Trajectory;
import measures.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

public class kmeans_test {


    public static void main(String[] args) throws IOException {
//        CalDistance calculator = new LCSS(8000.0);
//        CalDistance calculator = new ERP(new Point(-1032652.53274407,5051509.85538227));
//        CalDistance calculator = new Hausdorff();
        CalDistance calculator = new EDWP_DP();
        String outputFileName = "kmeans_cato_EDWP.csv";
        int k = 4;

        ArrayList<Trajectory> refp;
        double[] accumulate;
        HashSet<Trajectory> refp_hash;
        double[][] p_dist;
        String filePath = "D://trajectory_similarity/stand_alone_data/k_means/cluster_data.txt";
        ArrayList<Trajectory> trajectories = inputTrajectories(filePath,false,1.0,100,1.0);

        //get the distances and the pivot trajectories
        p_dist = new double[k][trajectories.size()];
        refp = new ArrayList<>();
        refp_hash = new HashSet<>();
        double max;
        accumulate = new double[trajectories.size()];
        Trajectory b;

        Trajectory last_ref = trajectories.get(18);
        refp.add(last_ref);
        refp_hash.add(last_ref);

        while(refp.size() < k){
            max = 0;
            b = last_ref;
            int b_id = refp.size()-1;
            for (int j=0;j<trajectories.size();j++){
                Trajectory item = trajectories.get(j);
                //calculate the distance between other pivots to this pivot
                if(refp.contains(item)){
                    if(item.equals(b))
                        p_dist[b_id][j] = 0;
                    else{
                        p_dist[b_id][j] = calculator.GetDistance(b,item);
                    }
                    continue;
                }

                p_dist[b_id][j] = calculator.GetDistance(b,item);
                accumulate[j] += p_dist[b_id][j];
                if (accumulate[j] > max){
                    max = accumulate[j];
                    last_ref = item;
                }
            }
            System.out.println("pivot"+refp.size()+"has done");
            refp.add(last_ref);
            refp_hash.add(last_ref);
        }
        max = 0;
        b = last_ref;
        int b_id = refp.size()-1;
        for (int j=0;j<trajectories.size();j++){
            Trajectory item = trajectories.get(j);
            //calculate the distance between other pivots to this pivot
            if(refp.contains(item)){
                if(item.equals(b))
                    p_dist[b_id][j] = 0;
                else{
                    p_dist[b_id][j] = calculator.GetDistance(b,item);
                }
                continue;
            }

            p_dist[b_id][j] = calculator.GetDistance(b,item);
            accumulate[j] += p_dist[b_id][j];
            if (accumulate[j] > max){
                max = accumulate[j];
                last_ref = item;
            }
        }
        System.out.println("pivot"+refp.size()+"has done");
        File f = new File(outputFileName);
        FileOutputStream fos1=new FileOutputStream(f);
        OutputStreamWriter dos1=new OutputStreamWriter(fos1);

        //print the types of the trajectory
        for(int i =0;i < trajectories.size();i ++){
            int cato = 0;
            double dis = Double.POSITIVE_INFINITY;
            for(int j = 0; j < k; j++){
                if(p_dist[j][i] < dis){
                    dis = p_dist[j][i];
                    cato = j;
                }
            }
            dos1.write(String.valueOf(i)+","+String.valueOf(cato)+"\n");
        }
        dos1.close();


    }
    private static ArrayList<Trajectory> inputTrajectories(String filePath,boolean needTrans,Double ratio_length,Integer seg_num,Double sampleRatio) throws IOException {
        File f = new File(filePath);
        BufferedReader bf = new BufferedReader(new FileReader(f));
        String str;

        ArrayList<Trajectory> trajectories=new ArrayList<>();
        int count=0;
        int totalsize=0;

        while((str=bf.readLine())!=null && count<seg_num)
        {
            ArrayList<Point> points=new ArrayList<>();
            String[] s=str.split(";");
            if(s.length==1 || s.length==0)
                continue;
            int maxpointNum=(int) Math.ceil(ratio_length*s.length);
            int cc=0;
            int tt=0;
            for(String str_point:s){
//                System.out.println(tt);
//                tt++;
                if(cc>=maxpointNum)
                    break;
                String[] xy=str_point.split(",");
//                System.out.println(xy[0]+","+xy[1]);
                Double x=Double.valueOf(xy[0]);
                Double y=Double.valueOf(xy[1]);
                if(Double.compare(x,0)==0)
                    continue;

//                if(x<boundary[0])
//                    boundary[0]=x;
//                if(x>boundary[2])
//                    boundary[2] = x;
//                if(y < boundary[1])
//                    boundary[1] = y;
//                if (y >boundary[3])
//                    boundary[3] = y;

                if(!needTrans){
                    points.add(new Point(x,y));
                }
                else{
                    points.add(new Point(x,y,true));
                }
                cc++;
            }
            totalsize+=points.size();
            trajectories.add(new Trajectory(points));
            count++;
        }
//        System.out.println(totalsize/count);

        //deal with sample ratio
        for(Trajectory t:trajectories){
            int totalSize=t.points.size();
            int extractSize=(int) Math.floor(totalSize*sampleRatio);
            int numPerGroup=(int) Math.floor(totalSize/extractSize);
            ArrayList<Point> newpArray=new ArrayList<>();
            int id=0;
            while(id<t.points.size()){
                newpArray.add(t.points.get(id));
                id+=numPerGroup;
            }
            t.points=newpArray;
        }
        return trajectories;

    }


}

