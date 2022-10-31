package indexes.PM_Tree;

import entity.Trajectory;

import java.util.ArrayList;

public class TreeNode extends Node{
    int level;
    ArrayList<Double> HR_min;
    ArrayList<Double> HR_max;
    int HR_size;
    double range;
    ArrayList<Node> sub_tree;

    TreeNode(Trajectory trajectory, Integer oid, int HR_size){
        this.object = trajectory;
        this.oid = oid;
        this.HR_size = HR_size;
        HR_min = new ArrayList<>(HR_size);
        HR_max = new ArrayList<>(HR_size);
        range = -1;

    }
    void remove_sub(Node sub){
        try{
            boolean success = sub_tree.remove(sub);
            if(!success)
                throw new Exception("the node isn't in the subnode list, cannot remove!");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
