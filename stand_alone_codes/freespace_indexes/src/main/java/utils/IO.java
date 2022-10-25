package utils;

import com.opencsv.CSVWriter;
import entity.Point;
import entity.Trajectory;

import java.io.*;
import java.util.ArrayList;

import utils.global_variables;

import static utils.global_variables.*;

public class IO {
    public static Trajectory_dataset inputTrajectories(String filePath, boolean needTrans, Double ratio_length, Integer seg_num, Double sampleRatio) throws IOException {
        File f = new File(filePath);
        BufferedReader bf = new BufferedReader(new FileReader(f));
        String str;
        Trajectory_dataset dataset = new Trajectory_dataset();

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

                if(x<dataset.boundary[0])
                    dataset.boundary[0]=x;
                if(x>dataset.boundary[2])
                    dataset.boundary[2] = x;
                if(y < dataset.boundary[1])
                    dataset.boundary[1] = y;
                if (y >dataset.boundary[3])
                    dataset.boundary[3] = y;

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
        dataset.trajectories = trajectories;
        return dataset;

    }
    public static void Output(String resultPath) throws IOException {
        //Instantiating the CSVWriter class
        CSVWriter writer = new CSVWriter(new FileWriter(resultPath+".csv"));
        //Writing data to a csv file
        String line1[] = {"pivot_num","index_building_time", "searching_time","prune_rate","search_results"};
        writer.writeNext(line1);
        for(int i=0;i<answers.size();i++){
            StringBuffer searchResult=new StringBuffer();
            //get the truly rate
            for(int j=0;j<answers.get(i).size();j++){
                if(j==0){
                    searchResult.append(answers.get(i).get(j));
                }
                else{
                    searchResult.append(';');
                    searchResult.append(answers.get(i).get(j));
                }
            }
            String line[]={Integer.toString(pivot_num.get(i)),Long.toString(index_building_times.get(i)),Long.toString(searching_times.get(i)),Double.toString(prune_rates.get(i)),searchResult.toString()};
            writer.writeNext(line);
        }
        //Flushing data from writer to file
        writer.flush();
        System.out.println("Data entered");
    }

    public static void SaveArray(String filepath){

    }
}
