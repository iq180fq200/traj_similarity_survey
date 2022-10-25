package indexes;


import entity.Trajectory;
import indexes.LAESA.LAESA;
import indexes.MVP.MVP_tree;
import measures.CalDistance;
import utils.global_variables;

import java.util.ArrayList;

public final class index_utils {
    public static Index build_index_on_preindex_LAESA(CalDistance calculator,ArrayList<Trajectory> trajectories,int pivot_num,Index pre_index){
        int len = global_variables.index_building_times.size();
        LAESA index;
        if(len > 0){
            Long starttime = System.currentTimeMillis();
            index = new LAESA(trajectories,calculator,pivot_num);
            index.build_index_from_prev(pre_index);
            Long endtime = System.currentTimeMillis();
            global_variables.index_building_times.add((endtime - starttime) + global_variables.index_building_times.get(len - 1));
        }
        else{
            Long starttime = System.currentTimeMillis();
            index = new LAESA(trajectories,calculator,pivot_num);
            index.build_index();
            Long endtime = System.currentTimeMillis();
            global_variables.index_building_times.add(endtime - starttime);
        }
        return index;
    }

    public static Index build_index(String index_type, CalDistance calculator, ArrayList<Trajectory> trajectories){
        Index index = null;
        Long starttime = -1l;

        Long endtime = -1l;

        switch (index_type){
            case "MVPTree":
                starttime = System.currentTimeMillis();
                index = new MVP_tree(160,10,2,trajectories,calculator);
                index.build_index();
                endtime = System.currentTimeMillis();

                break;
            case "Omni-family":
                starttime = System.currentTimeMillis();

                endtime = System.currentTimeMillis();
                break;
            case "LAESA":
                starttime = System.currentTimeMillis();
                index = new LAESA(trajectories,calculator,1);
//                index.load_index();
                index.build_index();
                endtime = System.currentTimeMillis();
                break;
            case "M-index":
                starttime = System.currentTimeMillis();

                endtime = System.currentTimeMillis();
                break;
            case "SPBTree":
                starttime = System.currentTimeMillis();

                endtime = System.currentTimeMillis();
                break;
        }
        global_variables.index_building_times.add(endtime-starttime);

        return index;
    }
    public static <T extends Object> T get_random(ArrayList<T> list){
        int index = (int) (Math.random()* list.size());
        return list.get(index);
    }


}
