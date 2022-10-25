package utils;

import java.util.ArrayList;

public class global_variables {
    public static ArrayList<Long> index_building_times = new ArrayList<>();
    public static ArrayList<Long> searching_times = new ArrayList<>();
    public static ArrayList<Double> prune_rates = new ArrayList<>();

    public static ArrayList<Integer> pivot_num = new ArrayList<>();
    public static ArrayList<ArrayList<Integer>> answers = new ArrayList<>();

//    public static final String[] str_indices = {
//            "MVPTree",
//            "Omni-family",
//            "LAESA",
//            "M-index",
//            "SPBTree"
//    };

    public static final String[] str_indices = {
            "LAESA"
    };

    public static void clearlogs(){
        global_variables.prune_rates.clear();
        global_variables.searching_times.clear();
        for (ArrayList<Integer> item:answers)
            item.clear();
        global_variables.answers.clear();
        global_variables.index_building_times.clear();
    }
}
