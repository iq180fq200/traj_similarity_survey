package Trajectory_transfer;

import entity.Point;
import entity.Trajectory;

import java.io.*;
import java.util.ArrayList;

/**
 * @author hanxi
 * @date 4/25/2022 14 47
 * discription
 */
public class AddNoiseTransformation {
    private final double noiseRate;
    private final double noiseDistance;

    /**
     * Setup default noise rate = 0.25 (25%), and noise distance = 0.01.
     */
    public AddNoiseTransformation() {
        //**************to be changed before running*******************
        this.noiseRate = 0.05;
        this.noiseDistance = 0.12;//degree
    }

    /**
     * @param noiseRate     Rate of noise to add (0.0 = 0%, 1.0 = 100%)
     * @param noiseDistance Distance threshold for noisy points.
     * @throws IllegalArgumentException
     */
    public AddNoiseTransformation(double noiseRate, double noiseDistance) throws IllegalArgumentException {
        if (noiseDistance < 0 || noiseRate < 0) {
            throw new IllegalArgumentException(
                    "Noise thresholds must be positive.");
        }
        this.noiseRate = noiseRate;
        this.noiseDistance = noiseDistance;
    }

    public Trajectory getTransformation(Trajectory t) {
        // make sure the original trajectory is unchanged
        ArrayList<Point> result = addNoise(t.copy().points);
        return new Trajectory(result);
    }

    protected ArrayList<Point> addNoise(ArrayList<Point> list) {
        ArrayList<Point> result = new ArrayList<Point>(list.size());

        double ratio = Math.random();
        int addPointCount = (int) (list.size() * noiseRate);

        if (list.size() < 2) {
            return list;
        }
        if (addPointCount < 1) {
            addPointCount = 1;
        }
        if (addPointCount >= list.size()) {
            addPointCount = list.size() - 1;
        }

        int[] valueList = topN(list.size() - 1, addPointCount);//get the index of point to be changed
        for (int i = 0; i < list.size(); i++) {
            boolean t = true;
            for (int j = 0; j < valueList.length; j++) {
                if (valueList[j] == i) {
                    double choice = Math.random();
                    if (choice < ratio) {
                        Point temp = getMidNoisePoint(list.get(i), list.get(i + 1));
                        result.add(temp);
                    } else {
                        Point temp = getNoisePoint(list.get(i));
                        result.add(temp);
                        t = false;
                    }
                }
            }
            if (t) {
                result.add(list.get(i));
            }
        }

        return result;
    }

    private Point getNoisePoint(Point p) {
        double noisex = (Math.random() * 2 - 1) * noiseDistance;
        double noisey = (Math.random() * 2 - 1) * noiseDistance;

        double x = p.x + noisex;
        double y = p.y + noisey;

        return new Point(x, y);
    }

    private Point getMidNoisePoint(Point p, Point q) {
        double noisex = (Math.random() * 2 - 1) * noiseDistance;
        double noisey = (Math.random() * 2 - 1) * noiseDistance;

        double x = (p.x + q.x) / 2 + noisex;
        double y = (p.y + q.y) / 2 + noisey;


        return new Point(x, y);
    }

    protected int[] topN(int allSize, int N) {
        int[] result = new int[N];

        double[] valueList = new double[allSize];
        for (int i = 0; i < valueList.length; i++) {
            valueList[i] = Math.random();
        }
        int[] allSizeList = sort(valueList);

        for (int i = 0; i < N; i++) {
            result[i] = allSizeList[i];
        }

        for (int i = 0; i < N; i++) {
            int min = result[i];
            int minIndex = i;
            for (int j = i + 1; j < N; j++) {
                if (min > result[j]) {
                    min = result[j];
                    minIndex = j;
                }
            }
            int temp = result[i];
            result[i] = min;
            result[minIndex] = temp;
        }

        return result;
    }

    protected int[] sort(double[] list) {
        int[] result = new int[list.length];
        boolean[] mark = new boolean[list.length];

        for (int i = 0; i < mark.length; i++) {
            mark[i] = true;
            result[i] = -1;
        }
        int count = 0;
        for (int i = 0; i < list.length; i++) {
            double max = -1;
            int index = -1;
            for (int j = 0; j < list.length; j++) {
                if (mark[j]) {
                    if (max == -1) {
                        max = list[j];
                        index = j;
                    } else if (max < list[j]) {
                        max = list[j];
                        index = j;
                    }
                }
            }
            mark[index] = false;
            result[count] = index;
            count++;
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        String filePath = "src/main/resources/originDataFreeSpace.txt";
        ArrayList<Trajectory> trajectories = inputTrajectories(filePath, 1.0, 10000, 1.0);
        ArrayList<Trajectory> trajectories1 = new ArrayList<>();
        AddNoiseTransformation transformation = new AddNoiseTransformation();
        for (Trajectory t : trajectories) {
            trajectories1.add(transformation.getTransformation(t));
        }
        //output trajectory1
        filePath="./src/noisedDataFreeSpace_"+Double.toString(transformation.noiseRate)+".txt";
        OutputTrajectory(filePath,trajectories1);
    }

    private static void OutputTrajectory(String filePath, ArrayList<Trajectory> trajectories1) throws IOException {
        FileWriter fw = new FileWriter(filePath);

        for (int i = 0; i < trajectories1.size(); i++) {
            fw.write(trajectories1.get(i).toString()+"\n");
        }
        fw.close();
    }

    private static ArrayList<Trajectory> inputTrajectories(String filePath,Double ratio_length,Integer seg_num,Double sampleRatio) throws IOException {
        File f = new File(filePath);
        BufferedReader bf = new BufferedReader(new FileReader(f));
        String str;

        ArrayList<Trajectory> trajectories=new ArrayList<>();
        int count=0;

        while((str=bf.readLine())!=null && count<seg_num)
        {
            ArrayList<Point> points=new ArrayList<>();
            String[] s=str.split(";");
            if(s.length==1 || s.length==0)
                continue;
            int maxpointNum=(int) Math.ceil(ratio_length*s.length);
            int cc=0;
            for(String str_point:s){
                if(cc>=maxpointNum)
                    break;
                String[] xy=str_point.split(",");
                Double x=Double.valueOf(xy[0]);
                Double y=Double.valueOf(xy[1]);
                if(Double.compare(x,0)==0)
                    continue;

                points.add(new Point(x,y,true));
                cc++;
            }
            trajectories.add(new Trajectory(points));
            count++;
        }

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

