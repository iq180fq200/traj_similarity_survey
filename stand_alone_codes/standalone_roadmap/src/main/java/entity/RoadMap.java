package entity;

/**
 * @author hanxi
 * @date 4/29/2022 15 34
 * discription
 */

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class RoadMap {
    public static HashMap<Long,Point> pointTable;
    public static ArrayList<Segment> segmentTable;
//    double[][] distanceTable =new double[75000][75000];
    double[][] distanceTable;

    //initialize pointTable
    public RoadMap(String pointTableFilePath, String segmentTablePath, String roadDistanceTablePath) throws IOException {


        //***************************get the pointTable************
        File f = new File(pointTableFilePath);
        BufferedReader bf = new BufferedReader(new FileReader(f));
        pointTable=new HashMap<>();
        String str;
        bf.readLine();//skip the header line
        System.out.println("reading point table");
        while((str=bf.readLine())!=null){
            String[] elements=str.split(",");//elements of one line in the node table
            Long osmID=new Double(Double.parseDouble(elements[0])).longValue();
            Double lat=Double.parseDouble(elements[2]);
            Double lng=Double.parseDouble(elements[1]);
            pointTable.put(osmID,new Point(lat,lng,true,osmID));
        }
        System.out.println("point table done");
        bf.close();


        distanceTable = new double[pointTable.size()][pointTable.size()];

        //***************************get the segmentTable************
        File f1 = new File(segmentTablePath);
        BufferedReader bf1 = new BufferedReader(new FileReader(f1));
        segmentTable=new ArrayList<>();
        System.out.println("reading segment table");
        String str1;
        bf1.readLine();//skip the header line
        while((str1=bf1.readLine())!=null){
            String[] elements=str1.split(",");//elements of one line in the node table
            Long sID=new Double(Double.parseDouble(elements[1])).longValue();
            Long eID=new Double(Double.parseDouble(elements[2])).longValue();
            Point sPoint=pointTable.get(sID);
            Point ePoint=pointTable.get(eID);
            Double length=new Double(Double.parseDouble(elements[3]));
            segmentTable.add(new Segment(sPoint,ePoint,length,Integer.parseInt(elements[0])));
        }
        bf1.close();
        System.out.println("segment table done");


        File f3=new File(roadDistanceTablePath);
        BufferedReader bf2=new BufferedReader(new FileReader(f3));
        String str2;
        int countline=0;
        while((str2=bf2.readLine())!=null && countline < distanceTable.length){
            String[] elements=str2.split(",");//elements of one line in the node table
            for(int i=0;i<distanceTable.length;i++){
                distanceTable[countline][i]=Double.valueOf(elements[i]);
            }
            countline+=1;
            if(countline%100==0)
                System.out.println(countline);
        }
        bf2.close();
        System.out.println("road map constructed");
    }


}
