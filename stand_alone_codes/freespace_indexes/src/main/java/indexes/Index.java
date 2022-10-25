package indexes;

import entity.Trajectory;
import measures.CalDistance;

import java.util.ArrayList;

public abstract class Index {
    public ArrayList<Trajectory> trajectories;
    public abstract Boolean build_index();
    public CalDistance calculator;
    public abstract ArrayList<Integer> knn_search(Trajectory query,int k);

    public abstract Boolean build_index_from_prev(Index prev_index);
//    public abstract Boolean load_index();
}
