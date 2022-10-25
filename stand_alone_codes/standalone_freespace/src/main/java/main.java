import com.opencsv.CSVWriter;
import entity.Point;
import entity.Trajectory;
import measures.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class main {
    private static int[][] ground_truth=null;
    private static String data_dir="";
    private static double threshold;//the threshold for both EDR and LCSS

    private static double[] boundary =  {Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};

    public enum TrialType {
        GROUND_TRUTH,
        NOISED,
        SAMPLE_RATE,
        DATA_AMOUNT,
        TRAJECTORY_LENGTH,
        QUERY_LENGTH
    }


//valuables: input file type, algorithm, number of k, trajectory length, trajectory number; time, result
    public static void main(String[] args) throws IOException {
        data_dir = args[0];
        threshold = Double.valueOf(args[1]);
        TrialType trialType;
        boolean needTrans;// if need to transfer from long,lat to mercator when building the trajectory
        String filePath;
        double ratio_length=1.0;
        int seg_number=10000;
        double sampleRate=1.0;
        double noiseRate=0.0;
        double queryLength=1.0;


        //*********ground truth*********************
        trialType=TrialType.GROUND_TRUTH;
        ratio_length=1.0;
        seg_number=10000;
        sampleRate=1.0;
        noiseRate=0.0;
        queryLength=1.0;
        filePath=data_dir+"/trajectories/origin.txt";
        needTrans=true;
        testOneRound(trialType,needTrans,filePath,ratio_length,seg_number,sampleRate,noiseRate,queryLength);
        //*********************

        //different sample rate*********************
        double [] sample_rates={0.1,0.15,0.2,0.25,0.3,0.35,0.4,0.6,0.8};
        trialType=TrialType.SAMPLE_RATE;
        ratio_length=1.0;
        seg_number=10000;
        noiseRate=0.0;
        queryLength=1.0;
        filePath=data_dir+"/trajectories/origin.txt";
        needTrans=true;
        for(double rate:sample_rates){
            sampleRate=rate;
            testOneRound(trialType,needTrans,filePath,ratio_length,seg_number,sampleRate,noiseRate,queryLength);
        }

        //different query number*********************
        int [] qns={2000,4000,6000,8000};
        trialType=TrialType.DATA_AMOUNT;
        ratio_length=1.0;
        sampleRate=1.0;
        noiseRate=0.0;
        queryLength=1.0;
        filePath=data_dir+"/trajectories/origin.txt";
        needTrans=true;
        for(int num:qns){
            seg_number=num;
            testOneRound(trialType,needTrans,filePath,ratio_length,seg_number,sampleRate,noiseRate,queryLength);
        }

        //different length*********************
        double [] length_rates={0.2,0.4,0.6,0.8};
        trialType=TrialType.TRAJECTORY_LENGTH;
       seg_number=10000;
        sampleRate=1.0;
        noiseRate=0.0;
        queryLength=1.0;
        filePath=data_dir+"/trajectories/origin.txt";
        needTrans=true;
        for(double lrate:length_rates){
            ratio_length=lrate;
            testOneRound(trialType,needTrans,filePath,ratio_length,seg_number,sampleRate,noiseRate,queryLength);
        }
//
        //***********************different noise
        double [] noise_rate={0.03,0.06,0.08,0.1,0.13,0.16,0.19,0.22};
        trialType=TrialType.NOISED;
        seg_number=10000;
        sampleRate=1.0;
        queryLength=1.0;
        needTrans=true;//already mercato
        for(double noise:noise_rate){
            noiseRate=noise;
            filePath=data_dir+"/trajectories/noise_"+Double.toString(noise).replace('.','_')+".txt";
            testOneRound(trialType,needTrans,filePath,ratio_length,seg_number,sampleRate,noiseRate,queryLength);
        }

        //***********different  query length
        double [] query_length={0.2,0.4,0.6,0.8};
        trialType=TrialType.QUERY_LENGTH;
        seg_number=10000;
        sampleRate=1.0;
        noiseRate=0.0;
        filePath=data_dir+"/trajectories/origin.txt";
        needTrans=true;
        for(double ql:query_length){
            queryLength=ql;
            testOneRound(trialType,needTrans,filePath,ratio_length,seg_number,sampleRate,noiseRate,queryLength);
        }

    }

    public static void testOneRound(TrialType trailType,boolean needTrans,String filePath,double ratio_length,int seg_number,double sampleRate,double noiseRate,double queryLength) throws IOException {
        //****************************to check when the global structure of experiment is changed
        String queryfilePath=data_dir+"/queries/query.txt";
        int kk=50;
        String[] methods={"DTW",
                "LCSS",
                "EDR",
                "ERP",
                "Frechet",
                "Hausdorff",
                "OWD",
                "LIP",
                "EDwP",
        "Seg-Frechet"};// all the methods to be tested
//        String[] methods ={
//                "Seg-Frechet"
//        };
        //*****************************************************************************



        //********** the output file folder and names*******************
        String _all_result_directory = "./results_"+data_dir.split("/")[data_dir.split("/").length - 1];
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

        switch(trailType){
            case TRAJECTORY_LENGTH:
                resultFile="length_ratio_"+Double.toString(ratio_length);
                break;
            case DATA_AMOUNT:
                resultFile="trajectory_amount_"+Integer.toString(seg_number);
                break;
            case SAMPLE_RATE:
                resultFile="sample_rate_"+Double.toString(sampleRate);
                break;
            case NOISED:
                resultFile="noise_rate_"+Double.toString(noiseRate);
                break;
            case GROUND_TRUTH:
                resultFile="ground_Truth";
                break;
            case QUERY_LENGTH:
                resultFile="query_length"+"_"+Double.toString(queryLength);
                break;
            default:
                resultFile="";
        }

        String resultPath=resultFolder+resultFile;
        //***************************************************************************************

        //*******************input candidates*********************
        ArrayList<Trajectory> trajectories = inputTrajectories(filePath,needTrans,ratio_length,seg_number,sampleRate);
        //***************************************************************************************************


        //****************************to check when the global structure of experiment is changed
        //get the test trajectory
        Trajectory test=inputTrajectories(queryfilePath,needTrans,Math.min(ratio_length,queryLength),1,1.0).get(0);
        System.out.println("preparation done");
        //*****************************************************************************


        //*****************************experiment**********************************
        int[][] answers=new int[methods.length][];
        long[] times=new long[methods.length];
        //*****************DTW*****************
        NormalTest(answers,times,trajectories,test,kk,0,new DTW());
        System.out.println("DTW finish");
        //******************************

        //*****************LCSS*****************
        NormalTest(answers,times,trajectories,test,kk,1,new LCSS(threshold));
        System.out.println("LCSS finish");
        //******************************

        //*****************EDR*****************
        NormalTest(answers,times,trajectories,test,kk,2,new EDR(threshold));
        System.out.println("EDR finish");
        //******************************

        //*****************ERP*****************
        //get the center of the candidate trajs as the gap point
        Point gap_p = new Point((boundary[0]+boundary[2])/2,(boundary[1]+boundary[3])/2,true);
        NormalTest(answers,times,trajectories,test,kk,3,new ERP(gap_p));
        System.out.println("ERP finish");
        //******************************

        //*****************Frechet*****************
        NormalTest(answers,times,trajectories,test,kk,4,new DiscreteFrechetDistance());
        System.out.println("Frechet finish");
        //******************************

        //*****************Hausdorff*****************
        NormalTest(answers,times,trajectories,test,kk,5,new Hausdorff());
        System.out.println("Hausdorff finish");
        //******************************

        //***********OWD***************
        NormalTest(answers,times,trajectories,test,kk,6,new OWD());
        System.out.println("OWD finish");
        //**************************************************************

        //*****************LIP*****************
        CalDistance ttt=new LIP();
        NormalTest(answers,times,trajectories,test,kk,7,ttt);
        System.out.println("LIP finish");
        //******************************

        //**************EDwP DP**************
        EDWPtest_dp(answers,times,trajectories,test,kk,8);
        System.out.println("EDWP_DP new finish");


        //**************NetFrechet****************
        NormalTest(answers,times,trajectories,test,kk,9,new NetFrechet());
        System.out.println("Seg-Frechet Finish");

        Output(resultPath,answers,times,methods);
    }


    /*
    normal test mesures without predealing the data(not ruin the trajectory structure)
     */
    static void NormalTest(int[][] answers,long[] times, ArrayList<Trajectory> trajectories, Trajectory test, int kk,int methodNumber,CalDistance measure){
        //time counter
        long begintime;
        long endtime;
        long costTime;
        //k query time
        int[] ids;
        begintime = System.currentTimeMillis();
        ids=GetTopKNearest(kk,measure,test,trajectories);//copy a tet trajectory every round
        endtime=System.currentTimeMillis();
        costTime = (endtime - begintime);
        answers[methodNumber]=ids;
        times[methodNumber]=costTime;
    }


    /*
    EDWP test
     */
//    static void EDWPtest(int[][] answers,long[] times, ArrayList<Trajectory> trajectories, Trajectory test, int kk,int methodNumber){
//        //time counter
//        long begintime;
//        long endtime;
//        long costTime;
//        long costTimeGrid;
//        //make a copy of all the trajectories to be search
//        ArrayList<Trajectory> trajectories1=new ArrayList<>();
//        for(Trajectory t: trajectories)
//            trajectories1.add(t.copy());
//        //k query time
//        int[] ids;
//        begintime = System.currentTimeMillis();
//        ids=GetTopKNearest1(kk,new EDWP(),test,trajectories1);//copy a tet trajectory every round
//        endtime=System.currentTimeMillis();
//        costTime = (endtime - begintime);
//        answers[methodNumber]=ids;
//        times[methodNumber]=costTime;
//    }

    static void EDWPtest_recursive(int[][] answers, long[] times, ArrayList<Trajectory> trajectories, Trajectory test, int kk, int methodNumber){
        //time counter
        long begintime;
        long endtime;
        long costTime;
        long costTimeGrid;
        //make a copy of all the trajectories to be search
        ArrayList<Trajectory> trajectories1=new ArrayList<>();
        for(Trajectory t: trajectories)
            trajectories1.add(t.copy());
        //k query time
        int[] ids;
        begintime = System.currentTimeMillis();
        ids=GetTopKNearest(kk,new EDWP_Recursive(),test,trajectories1);//copy a tet trajectory every round
        endtime=System.currentTimeMillis();
        costTime = (endtime - begintime);
        answers[methodNumber]=ids;
        times[methodNumber]=costTime;
    }

    static void EDWPtest_dp(int[][] answers, long[] times, ArrayList<Trajectory> trajectories, Trajectory test, int kk, int methodNumber){
        //time counter
        long begintime;
        long endtime;
        long costTime;
        long costTimeGrid;
        //make a copy of all the trajectories to be search
        ArrayList<Trajectory> trajectories1=new ArrayList<>();
        for(Trajectory t: trajectories)
            trajectories1.add(t.copy());
        //k query time
        int[] ids;
        begintime = System.currentTimeMillis();
        ids=GetTopKNearest(kk,new EDWP_DP(),test,trajectories1);//copy a tet trajectory every round
        endtime=System.currentTimeMillis();
        costTime = (endtime - begintime);
        answers[methodNumber]=ids;
        times[methodNumber]=costTime;
    }

    /*
        out put to csv
         */
    private static void Output(String resultPath, int[][] answers, long[] times, String[] methods) throws IOException {
        //get the ground truth for the first round
        if(ground_truth==null)
            ground_truth=answers;
        //Instantiating the CSVWriter class
        CSVWriter writer = new CSVWriter(new FileWriter(resultPath+".csv"));
        //Writing data to a csv file
        String line1[] = {"method","time", "results"};
        writer.writeNext(line1);
        for(int i=0;i<times.length;i++){
            StringBuffer searchResult=new StringBuffer();
            //get the truly rate
            for(int j=0;j<answers[i].length;j++){
                if(j==0){
                    searchResult.append(answers[i][j]);
                }
                else{
                    searchResult.append(';');
                    searchResult.append(answers[i][j]);
                }
            }
            String line[]={methods[i],Long.toString(times[i]),searchResult.toString()};
            writer.writeNext(line);
        }
        //Flushing data from writer to file
        writer.flush();
        System.out.println("Data entered");
    }


    /*
     * implement the top k search (no need to copy a test trajectory every comparison)
     */
    private static int[] GetTopKNearest(int k, CalDistance calculator, Trajectory query, ArrayList<Trajectory> all){
        double[] distances=new double[all.size()];
        int i=0;
        for(Trajectory T2:all){
            distances[i]=calculator.GetDistance(query,T2);
//            break;
//            System.out.println(i);
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
            else if(this.score == s.score)
                return 0;
            return -2;
        }
    }


    /*
    input the candidate trajectories
     */
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

                if(x<boundary[0])
                    boundary[0]=x;
                if(x>boundary[2])
                    boundary[2] = x;
                if(y < boundary[1])
                    boundary[1] = y;
                if (y >boundary[3])
                    boundary[3] = y;

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
