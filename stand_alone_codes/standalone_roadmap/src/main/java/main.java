/**
 * @author hanxi
 * @date 5/1/2022 17 17
 * discription
 */

import entity.Point;
import entity.RoadMap;
import entity.Trajectory;
import measures.*;

import java.io.FileWriter;

import java.io.*;
import java.util.*;

public class main {
    private static int[][] ground_truth=null;
    private static double TP_lamda;
    private static Long ERP_ref_ID;
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
        TP_lamda = Double.valueOf(args[1]);
        ERP_ref_ID = Long.valueOf(args[2]);


        TrialType trialType;
        String candidateFilePath;//input candidate trajectories
        String queryFilePath;
        double ratio_length=1.0;
        int seg_number=10000;
        double noiseRate=0.0;

        //road map
        RoadMap roadMap=new RoadMap(data_dir+"/road/node.csv",data_dir+"/road/edge_weight.csv",data_dir+"/road/roadDistance.txt");



        //different noise,rate,trajectory length,data num
        //*********ground truth*********************
        trialType=TrialType.GROUND_TRUTH;
        ratio_length=1.0;
        seg_number=10000;
        noiseRate=0.0;
        candidateFilePath=data_dir+"/trajectories/origin.txt";
        queryFilePath=data_dir+"/queries/query.txt";
        testOneRound(roadMap,trialType,candidateFilePath,ratio_length,seg_number,noiseRate,queryFilePath);
        //*********************

        //*********************different query number****************************
        int[] query_nums={2000,4000,6000,8000};
        trialType=TrialType.DATA_AMOUNT;
        ratio_length=1.0;
        noiseRate=0.0;
        candidateFilePath=data_dir+"/trajectories/origin.txt";
        queryFilePath=data_dir+"/queries/query.txt";
        for (int n:query_nums){
            seg_number=n;
            testOneRound(roadMap,trialType,candidateFilePath,ratio_length,seg_number,noiseRate,queryFilePath);
        }

        //**************different length****************************
        double[] ratio_lengths={0.2,0.4,0.6,0.8};
        trialType=TrialType.TRAJECTORY_LENGTH;
        seg_number=10000;
        noiseRate=0.0;
        candidateFilePath=data_dir+"/trajectories/origin.txt";
        queryFilePath=data_dir+"/queries/query.txt";
        for (double l:ratio_lengths){
            ratio_length=l;
            testOneRound(roadMap,trialType,candidateFilePath,ratio_length,seg_number,noiseRate,queryFilePath);
        }

        //        //*************different noise***********************
        double[] noises={0.03,0.05,0.07,0.1,0.15,0.2,0.3};
        trialType=TrialType.NOISED;
        seg_number=10000;
        ratio_length=1.0;
        for (double noise:noises){
            noiseRate=noise;
            candidateFilePath=data_dir+"/trajectories/noise_"+Double.toString(noise).replace('.','_')+".txt";
            queryFilePath=data_dir+"/queries/query.txt";
            testOneRound(roadMap,trialType,candidateFilePath,ratio_length,seg_number,noiseRate,queryFilePath);
        }
        //***************************************************

        //*******************different query trajectory************************
        trialType=TrialType.QUERY_TRAJECTORY;
        String[] querypaths={data_dir+"/queries/straight_line_query.txt",data_dir+"/queries/polyline_nooverlap_query.txt",data_dir+"/queries/polyline_overlap_query.txt",data_dir+"/queries/round_query.txt"};
        candidateFilePath=data_dir+"/trajectories/shape.txt";
        ratio_length=1.0;
        seg_number=Integer.valueOf(args[3]);
        noiseRate=0.0;
        for(String qp:querypaths){
            queryFilePath=qp;
            testOneRound(roadMap,trialType,candidateFilePath,ratio_length,seg_number,noiseRate,queryFilePath);
        }

    }

    public static void testOneRound(RoadMap roadMap,TrialType trailType,String filePath,double ratio_length,int seg_number,double noiseRate,String queryFilePath) throws Exception {
        //****************************to check when the global structure of experiment is changed
        String queryfilePath=queryFilePath;
        int kk=50;
        String[] methods = {"LCRS",
                "LORS",
                "NetEDR",
                "NetERP",
                "NetLCSS",
                "TP",
                "NetDTW"};
//        String[] methods = {"LCRS",
//                "LORS",
//                "TP"
//                };
        //*****************************************************************************


        //********** to check everytime before the experiment*******************
        String _all_result_directory = "./results_"+data_dir.split("\\\\")[data_dir.split("\\\\").length - 1];
        File all_result_directory = new File(_all_result_directory);
        if (!all_result_directory.exists()){
            all_result_directory.mkdir();
        }


        String resultFolder=_all_result_directory + "/" + trailType.name()+"/";
        //if the folder doesn't exist, make it
        File directory = new File(resultFolder);
        if (! directory.exists()){
            directory.mkdir();
        }
        String resultFile;

        switch (trailType) {
            case TRAJECTORY_LENGTH:
                resultFile = "length_ratio_" + Double.toString(ratio_length);
                break;
            case DATA_AMOUNT:
                resultFile = "trajectory_amount_" + Integer.toString(seg_number);
                break;
            case NOISED:
                resultFile = "noise_rate_" + Double.toString(noiseRate);
                break;
            case GROUND_TRUTH:
                resultFile = "ground_Truth";
                break;
            case QUERY_TRAJECTORY:
                resultFile = data_dir.split("\\\\")[data_dir.split("\\\\").length - 1].replace(".txt","");
                break;
            default:
                resultFile = "";
        }

        String resultPath = resultFolder + resultFile;

        //*******************input candidates*********************
        String timestamp_path_candidates = filePath.replace(".txt","")+"_time.txt";
        ArrayList<Trajectory> trajectories = inputTrajectories(roadMap,filePath,timestamp_path_candidates,ratio_length,seg_number);
        //***************************************************************************************************


        //****************************to check when the global structure of experiment is changed
        //get the test trajectory
        String timestamp_path_query = "";
        if(queryfilePath.contains("_query"))
            timestamp_path_query = queryFilePath.replace("_query.txt","")+"_time.txt";
        else
            timestamp_path_query = queryFilePath.replace(".txt","")+"_time.txt";
        Trajectory test=inputTrajectories(roadMap,queryfilePath,timestamp_path_query,ratio_length,1).get(0);
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

        //*****************EDR*****************
        NormalTest(answers,times,trajectories,test,kk,2,new NetEDR(),roadMap);
        System.out.println("EDR finish");
        //******************************

        //*****************ERP*****************
        NormalTest(answers,times,trajectories,test,kk,3,new NetERP(ERP_ref_ID),roadMap);
        System.out.println("ERP finish");
        //******************************

        //*****************NetLCSS****************
        NormalTest(answers,times,trajectories,test,kk,4,new NetLCSS(),roadMap);
        System.out.println("LCSS finish");
        //******************************

        //***********TP***************
        NormalTest(answers,times,trajectories,test,kk,5,new TP(TP_lamda),roadMap);
        System.out.println("TP finish");
        //**************************************************************


        //***********NetDTW***************
        NormalTest(answers,times,trajectories,test,kk,6,new NetDTW(),roadMap);
        System.out.println("NetDTW finish");
        //**************************************************************

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
            String[] s=str.replace("[","").replace("]","").split(", ");
            String[] times = str_time.split(",");
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

}
