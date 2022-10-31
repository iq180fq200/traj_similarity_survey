package indexes.PM_Tree;

import entity.Trajectory;

import java.util.ArrayList;

public class DataNode extends Node{
    ArrayList<Double> PD;
    int PD_size;


    DataNode(Trajectory trajectory, Integer oid, int pd_size) {
        this.object = trajectory;
        this.oid = oid;
        PD_size = pd_size;
        PD = new ArrayList<>(pd_size);
    }
}
