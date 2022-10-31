package indexes.PM_Tree;

import entity.Trajectory;

import java.util.ArrayList;

public class LeafNode extends TreeNode {
    public LeafNode(Trajectory trajectory, Integer oid, int HR_size){
        super(trajectory, oid, HR_size);
        sub_tree = new ArrayList<>();
    }
}
