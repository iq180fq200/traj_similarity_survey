package indexes.PM_Tree;

import entity.Trajectory;

import java.util.ArrayList;

public class InnerNode extends TreeNode {
    public  InnerNode(Trajectory trajectory, Integer oid, int HR_size){
        super(trajectory, oid, HR_size);
        sub_tree = new ArrayList<>();
    }

}
