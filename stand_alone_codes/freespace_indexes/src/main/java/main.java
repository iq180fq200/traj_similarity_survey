import entity.Trajectory;
import indexes.Index;
import indexes.MVP.MVP_tree;
import measures.*;
import utils.IO;
import utils.Trajectory_dataset;
import utils.global_variables;

import java.io.*;
import java.util.ArrayList;

import static indexes.index_utils.build_index;
import static indexes.index_utils.build_index_on_preindex_LAESA;
import static utils.calculator_utils.get_ERP_reference;

public class main {
    private static String data_dir="";



//valuables: input file type, algorithm, number of k, trajectory length, trajectory number; time, result
    public static void main(String[] args) throws IOException {
        data_dir = args[0];
        String filePath;
        int seg_number = 10000;
        String[] str_calculators = {
                "Discrete Frechet",
                "Hausdorff",
                "ERP"
        };


        // get the trajectories
        filePath = data_dir + "/trajectories/origin.txt";
        Trajectory_dataset dataset = IO.inputTrajectories(filePath, true, 1.0, seg_number, 1.0);
        ArrayList<Trajectory> trajectories = dataset.trajectories;
        String queryfilePath = data_dir + "/queries/query.txt";
        Trajectory query = IO.inputTrajectories(queryfilePath, true, 1.0, seg_number, 1.0).trajectories.get(0);

        //get the calculators
        CalDistance[] calculators = {
                new DiscreteFrechetDistance(),
                new Hausdorff(),
                new ERP(get_ERP_reference(dataset.boundary))
        };


        String _all_result_directory = "./results_"+data_dir.split("\\\\")[data_dir.split("\\\\").length - 1];
        File all_result_directory = new File(_all_result_directory);
        if (!all_result_directory.exists()){
            all_result_directory.mkdir();
        }
        File result_directory;

//        for (int i = 0; i < str_calculators.length; i++) {
//            for(int k = min_pivot_num; k < max_pivot_num ;k = k + step){
//                global_variables.pivot_num.add(k);
//                Index[] indices = new Index[str_indices.length];
//                CalDistance calculator = calculators[i];
//                for (int j = 0; j < indices.length; j++) {
//                    indices[j] = build_index(str_indices[j], calculator, trajectories,k);
//                }
//                for (int j = 0; j < indices.length; j++) {
//                    Long start = System.currentTimeMillis();
//                    indices[j].knn_search(query, 50);
//                    Long end = System.currentTimeMillis();
//                    global_variables.searching_times.add(end - start);
//                }
//            }
//
//            IO.Output( all_result_directory+"/"+str_calculators[i]);
//            global_variables.clearSearchResults();
//            global_variables.index_building_times.clear();
//
//        }

        //for LAESA
        int min_pivot_num = 1;
        int max_pivot_num = 51;
        int step = 2;
        result_directory = new File(_all_result_directory+"/LAESA");
        if (!result_directory.exists()){
            result_directory.mkdir();
        }
        for (int i = 0; i < str_calculators.length; i++) {
            CalDistance calculator = calculators[i];
            Index pre_index = null;
            for(int k = min_pivot_num; k <= max_pivot_num ;k = k + step){
                global_variables.pivot_num.add(k);
                Index index = build_index_on_preindex_LAESA(calculator, trajectories,k,pre_index);
                Long start = System.currentTimeMillis();
                index.knn_search(query, 50);
                Long end = System.currentTimeMillis();
                global_variables.searching_times.add(end - start);
                pre_index = index;
            }

            IO.Output( result_directory+"/"+str_calculators[i]);
            global_variables.clearlogs();
        }


        //for MVP tree
        result_directory = new File(_all_result_directory+"/MVP");
        if (!result_directory.exists()){
            result_directory.mkdir();
        }
        for (int i = 0; i < str_calculators.length; i++) {
            CalDistance calculator = calculators[i];
            Index index = build_index("MVPTree",calculator,trajectories);
            ((MVP_tree) index).print_index();
            Long start = System.currentTimeMillis();
            index.knn_search(query, 50);
            Long end = System.currentTimeMillis();
            global_variables.searching_times.add(end - start);
            IO.Output( result_directory+"/"+str_calculators[i]);
            global_variables.clearlogs();
        }






    }
}
