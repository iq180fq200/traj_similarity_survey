/**
 * @author hanxi
 * @date 5/1/2022 17 17
 * discription
 */

import entity.RoadMap;
import entity.Trajectory;
import measures.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class geolife_bus {
    private static int[][] ground_truth=null;
    private static double TP_lamda;
    private static Long ERP_ref_ID;
    private static double EDR_LCSS_threshold;

    public enum TrialType {
        GROUND_TRUTH,
        NOISED,
        DATA_AMOUNT,
        TRAJECTORY_LENGTH,
        QUERY_TRAJECTORY //to test different trajectory shapes
    }
    private static String data_dir="";


    //valuables: input file type, algorithm, number of k, trajectory length, trajectory number; time, result
    public static void main(String[] args) throws Exception {
        data_dir = args[0];

        TrialType trialType;
        String candidateFilePath;//input candidate trajectories
        String queryFilePath;
        double ratio_length=1.0;
        int seg_number=10000;
        double noiseRate=0.0;

        //road map
        RoadMap roadMap=new RoadMap("D:/trajectory_similarity/stand_alone_data/TDrive_data/road/node.csv","D:/trajectory_similarity/stand_alone_data/TDrive_data/road/edge_weight.csv");



        //different noise,rate,trajectory length,data num
        //*********ground truth*********************
        trialType= TrialType.GROUND_TRUTH;
        ratio_length=1.0;
        seg_number=10000;
        noiseRate=0.0;
        candidateFilePath=data_dir+"/trajectories/trajs_bus_1.txt";
        queryFilePath=data_dir+"/queries/query_bus.txt";
        testOneRound(roadMap,trialType,candidateFilePath,ratio_length,seg_number,noiseRate,queryFilePath);
        //*********************

    }

    public static void testOneRound(RoadMap roadMap,TrialType trailType,String filePath,double ratio_length,int seg_number,double noiseRate,String queryFilePath) throws Exception {
        //****************************to check when the global structure of experiment is changed
        String queryfilePath=queryFilePath;
        int kk=50;
        String[] methods = {"LCRS",
                "LORS"};
//        String[] methods = {"LCRS",
//                "LORS",
//                "TP"
//                };
        //*****************************************************************************


        //********** to check everytime before the experiment*******************
        String resultPath = "./output_bus";

        //*******************input candidates*********************
//        String timestamp_path_candidates = filePath.replace(".txt","")+"_time.txt";
        ArrayList<Trajectory> trajectories = inputTrajectories(roadMap,filePath,ratio_length,seg_number);
        //***************************************************************************************************


        //****************************to check when the global structure of experiment is changed
        //get the test trajectory
//        String timestamp_path_query = "";
//        if(queryfilePath.contains("_query"))
//            timestamp_path_query = queryFilePath.replace("_query.txt","")+"_time.txt";
//        else
//            timestamp_path_query = queryFilePath.replace(".txt","")+"_time.txt";
        Trajectory test=inputTrajectories(roadMap,queryfilePath,ratio_length,1).get(0);
        //*****************************************************************************


        //*****************************experiment**********************************
        int[][] answers=new int[methods.length][];
        long[] times=new long[methods.length];
        //*****************LCRS****************
        NormalTest(answers,times,trajectories,test,kk,0,new LCRS(),roadMap);
        System.out.println("LCRS finish");
        //******************************

        //*****************LORS*****************
        NormalTest(answers,times,trajectories,test,kk,1,new LORS(),roadMap);
        System.out.println("LORS finish");
        //******************************

        Output(resultPath,answers,times,methods);
    }

    /*
    normal test mesures without predealing the data(not ruin the trajectory structure)
     */
    static void NormalTest(int[][] answers,long[] times, ArrayList<Trajectory> trajectories, Trajectory test, int kk,int methodNumber,CalculateDistance measure,RoadMap roadMap) throws Exception {
        //time counter
        long begintime;
        long endtime;
        long costTime;
        long costTimeGrid;
        //k query time
        int[] ids;
        begintime = System.currentTimeMillis();
        ids=GetTopKNearest(kk,measure,test,trajectories,roadMap);//copy a tet trajectory every round
        endtime=System.currentTimeMillis();
        costTime = (endtime - begintime);
        answers[methodNumber]=ids;
        times[methodNumber]=costTime;
    }
    /*
    out put to csv
     */
    private static void Output(String resultPath, int[][] answers, long[] times, String[] methods) throws IOException {
        File f=new File(resultPath+".csv");//指定文件
        FileOutputStream fos=new FileOutputStream(f);//创建输出流fos并以f为参数
        OutputStreamWriter osw=new OutputStreamWriter(fos);//创建字符输出流对象osw并以fos为参数
        BufferedWriter bw=new BufferedWriter(osw);//创建一个带缓冲的输出流对象bw，并以osw为参数
        bw.write("methods"+","+"time"+","+ "results");//使用bw写入一行文字，为字符串形式String
        bw.newLine();//换行
        for(int i=0;i<times.length;i++){
            StringBuffer searchResult=new StringBuffer();
            System.out.println(answers[i].length);
            System.out.println(answers[i][0]);
            boolean flag=true;
            System.out.println(i);
            for(int ele:answers[i]){
                if(flag){
                    flag=false;
                    searchResult.append(ele);
                }
                else{
                    searchResult.append(";");
                    searchResult.append(ele);
                }

            }
            bw.write(methods[i]+","+Long.toString(times[i])+","+searchResult.toString());
            bw.newLine();
        }
        bw.close();
        System.out.println("Data entered");
    }

    /*
     * implement the top k search (no need to copy a test trajectory every comparison)
     */
    private static int[] GetTopKNearest(int k, CalculateDistance calculator, Trajectory query, ArrayList<Trajectory> all,RoadMap roadMap) throws Exception {
        double[] distances=new double[all.size()];
        int i=0;
        for(Trajectory T2:all){
            if(T2.segments.size()==0)
                distances[i]=Double.MAX_VALUE;
            else
                distances[i]=calculator.GetDistance(query,T2,roadMap);
            i++;
        }
        return indexesOfTopElements(distances,k);
    }

    /**
     * Return the indexes correspond to the top-k minest in an array.
     */
    private static int[] indexesOfTopElements(double[] orig, int nummax) {
        int[] result=new int[nummax];
        ArrayList<ScoreWithIndex> scoreWithIndices=new ArrayList<>();
        for(int i=0;i<orig.length;i++){
//            if(i==5018)
//                System.out.println();
            scoreWithIndices.add(new ScoreWithIndex(orig[i],i));
        }
        Collections.sort(scoreWithIndices);
        for(int i=0;i<nummax;i++)
            result[i]=scoreWithIndices.get(i).index;

        return result;
    }

    private static class ScoreWithIndex implements Comparable{
        double score;
        int index;
        ScoreWithIndex(double score,int index){
            this.score=score;
            this.index=index;
        }

        @Override
        public int compareTo(Object o) {
            ScoreWithIndex s=(ScoreWithIndex)o;
            if(this.score>s.score)
                return 1;
            else if(this.score<s.score)
                return -1;
            else
                return 0;
        }
    }


    /*
    input the candidate trajectories
     */
    private static ArrayList<Trajectory> inputTrajectories(RoadMap roadMap,String filePath,String timestamp_filePath, Double ratio_length,Integer seg_num) throws IOException {
        File f = new File(filePath);
        BufferedReader bf = new BufferedReader(new FileReader(f));
        String str;
        String str_time;

        ArrayList<Trajectory> trajectories=new ArrayList<>();
        int count=0;
        // get the timestamps
        File times_f = new File(timestamp_filePath);
        BufferedReader bf_times = new BufferedReader(new FileReader(times_f));


        while((str=bf.readLine())!=null && count<seg_num)
        {
            str_time = bf_times.readLine();
            str_time = str_time.replace(" ","").replace("[","").replace("]","").replace("'","");
            ArrayList<Integer> segIDs=new ArrayList<>();
            ArrayList<Double> timestamps = new ArrayList<Double>();
            String[] s=str.replace("[","").replace("]","").replace(" ","").split(",");
            String[] times = str_time.replace("[","").replace("]","").replace(" ","").split(",");
            if(s.length==1)
                s=s[0].split(",");
            int maxSegNum=(int) Math.ceil(ratio_length*s.length);
            int max_time_point = maxSegNum + 1;
            int cc=0;
            int tt=0;
            //if the trajectory is not null
            if(s.length!=1&&s[0]!="nan"){
                for(String str_seg:s){
                    if(cc>=maxSegNum)
                        break;
                    segIDs.add(Integer.valueOf(str_seg));
                    cc++;
                }
            }
            if(times.length!=1&&times[0]!="nan"){
                for(String time:times){
                    if(cc>=max_time_point)
                        break;
                    timestamps.add(Double.valueOf(time));
                    cc++;
                }
            }
            trajectories.add(new Trajectory(segIDs,timestamps,roadMap));

            count++;
        }


        return trajectories;

    }


    /*
   input the candidate trajectories
    */
    private static ArrayList<Trajectory> inputTrajectories(RoadMap roadMap,String filePath,Double ratio_length,Integer seg_num) throws IOException {
        File f = new File(filePath);
        BufferedReader bf = new BufferedReader(new FileReader(f));
        String str;

        ArrayList<Trajectory> trajectories=new ArrayList<>();
        int count=0;


        while((str=bf.readLine())!=null && count<seg_num)
        {
            ArrayList<Integer> segIDs=new ArrayList<>();
            ArrayList<Double> timestamps = new ArrayList<Double>();
            String[] s=str.replace("[","").replace("]","").replace(" ","").split(",");
            if(s.length==1)
                s=s[0].split(",");
            int maxSegNum=(int) Math.ceil(ratio_length*s.length);
            int max_time_point = maxSegNum + 1;
            int cc=0;
            int tt=0;
            //if the trajectory is not null
            if(s.length!=1&&s[0]!="nan"){
                for(String str_seg:s){
                    if(cc>=maxSegNum)
                        break;
                    segIDs.add(Integer.valueOf(str_seg));
                    cc++;
                }
            }
            trajectories.add(new Trajectory(segIDs,timestamps,roadMap));

            count++;
        }


        return trajectories;

    }
}




