package indexes.PM_Tree;

import entity.Trajectory;
import indexes.Index;
import measures.CalDistance;
import utils.KNNHeap;
import utils.global_variables;

import java.util.ArrayList;

import static indexes.index_utils.get_random_number;

public class PM_Tree extends Index {
    KNNHeap knnheap;
    double current_r;
    ArrayList<Double> dis_q2p = new ArrayList<>();
    int real_count = 0;

    int N,M,Pivot_num,HR_size,PD_size;
    CalDistance calculator;
    ArrayList<Trajectory> trajectories;
    ArrayList<Integer> pivots_id;

    TreeNode root;

    /**

     * @ param     :
     * N: number of groups for pivot selecting
     * M: the M for traditional M-Tree
     * Pivot_num: number of pivot point

     * @ return    :

     * @ Description :
     * the construction of PM_tree; note that
     * the size of HR and PD are setted equal to Pivot_num

     */
    public PM_Tree(int N,int M,int Pivot_num, CalDistance calculator, ArrayList<Trajectory> trajectories){
        this.N = N;
        this.M = M;
        this.Pivot_num = Pivot_num;
        this.HR_size = Pivot_num;
        this.PD_size = Pivot_num;
        this.calculator = calculator;
        this.trajectories = trajectories;
    }

    @Override
    public Boolean build_index() {
        //select the pivot trajectories
        select_pivot();
        //insert data
        for(int id = 0; id < trajectories.size(); id ++){
            System.out.println(id);
            insert(trajectories.get(id),id);
        }
        update_level(root,0);
        return null;
    }

    @Override
    public ArrayList<Integer> knn_search(Trajectory query, int k) {
        knnheap= new KNNHeap(k);
        current_r = Double.POSITIVE_INFINITY;
        real_count = 0;
        //calculate all the distance between pivot to query
        cal_pivot_distance(query);
        search_node(query,root,calculator.GetDistance(query,root.object));
        global_variables.answers.add(knnheap.getResults());
        global_variables.prune_rates.add(1.0 * (trajectories.size() - real_count) / trajectories.size());
        return null;
    }

    @Override
    public Boolean build_index_from_prev(Index prev_index) {
        return null;
    }





    //*****************functions for index building*********************************
    private void split(TreeNode current, Node insert_node){
        ArrayList<TreeNode> newnodes = promote_and_partition(current,insert_node);
        TreeNode new_node1 = newnodes.get(0);
        TreeNode new_node2 = newnodes.get(1);
        //if is the root
        if(current == root){
            root = new InnerNode(new_node1.object,new_node1.oid,new_node1.HR_size);
            addChild(root,new_node1);
            addChild(root,new_node2);
        }
        else{
            addChild(current.patent_node,new_node1);
            (current.patent_node).remove_sub(current);
            if(Is_Full(new_node1.patent_node)){
                split(new_node1.patent_node,new_node2);
            }
            else{
                addChild(current.patent_node,new_node2);
            }

        }
    }
    private void choose_and_add(Node item,TreeNode new_node1,TreeNode new_node2){
        double dis1 = calculator.GetDistance(item.object,new_node1.object);
        double dis2 = calculator.GetDistance(item.object,new_node2.object);
        if(dis1 <= dis2)
            addChild(new_node1,item,dis1);
        else
            addChild(new_node2,item,dis2);
    }

    private ArrayList<TreeNode> promote_and_partition(TreeNode current, Node insert_node) {
        TreeNode new_node1;
        TreeNode new_node2;
        ArrayList<Integer> feature_ids = get_random_number(M , 2);
        //select feature nodes
       Node feature_node1 = feature_ids.get(0) > M? insert_node : current.sub_tree.get(feature_ids.get(0));
       Node feature_node2 = feature_ids.get(1) > M? insert_node: current.sub_tree.get(feature_ids.get(1));
       if(current instanceof LeafNode){
           new_node1 = new LeafNode(feature_node1.object,feature_node1.oid,HR_size);
           new_node2 = new LeafNode(feature_node2.object,feature_node2.oid,HR_size);
       }

       else{
           new_node1 = new InnerNode(feature_node1.object,feature_node1.oid,HR_size);
           new_node2 = new InnerNode(feature_node2.object,feature_node2.oid,HR_size);
       }

       //assign sub nodes to new nodes
        for(Node item : current.sub_tree){
            if(item == feature_node1){
                addChild( new_node1,item,0.0);
            }
            else if(item == feature_node2){
                addChild(new_node2,item,0.0);
            }
            else{
                choose_and_add(item,new_node1,new_node2);
            }
        }
        //assign inserted node to new nodes
        Node item = insert_node;
        if(item == feature_node1){
            addChild(new_node1,item,0.0);
        }
        else if(item == feature_node2){
            addChild(new_node2,item,0.0);
        }
        else{
            choose_and_add(item,new_node1,new_node2);
        }
        ArrayList<TreeNode> result = new ArrayList<TreeNode>(2);
        result.add(new_node1);
        result.add(new_node2);
        return result;

    }
    /**

     * @ param     :
     * node: the parent node
     * dis: the new distance that may be larger than the current range

     * @ Description :update range of a single treenode according to an object's distance from this node's feature object

     */

    private void update_range(TreeNode node, double dis){
        node.range = node.range < dis ? dis : node.range;
    }

    /**

     * @ param     :
     * node: the parent node
     * new_node: newly added subnode

     * @ Description :update range of a single treenode according to a newly added subnode

    */

    private void update_range(TreeNode node, Node new_node){
        if(new_node instanceof TreeNode){
            //if is a newly created node
            if(Double.compare(node.range,-1)==0){
                node.range = new_node.parent_dist + ((TreeNode) new_node).range;
            }
            else{
                if(new_node.parent_dist + ((TreeNode) new_node).range > node.range)
                    node.range = new_node.parent_dist + ((TreeNode) new_node).range;
            }
        }
        else if(new_node instanceof DataNode){
            if(Double.compare(node.range,-1)==0){
                node.range = new_node.parent_dist;
            }
            else{
                if(new_node.parent_dist > node.range)
                    node.range = new_node.parent_dist;
            }
        }

    }

    /**

     * @ Description :update hyper rings of a single treenode according to a newly added subnode

     */
    private void update_hyper_rings(TreeNode node, Node new_node){
        ArrayList<Double> HR_min = node.HR_min;
        ArrayList<Double> HR_max = node.HR_max;
        int size = node.HR_size;
        if(new_node instanceof DataNode){
            ArrayList<Double> pivot2traj_dis = ((DataNode) new_node).PD;
            if(HR_min.size() == 0){
                for(int i = 0;i < size;i++){
                    HR_min.add(pivot2traj_dis.get(i));
                    HR_max.add(pivot2traj_dis.get(i));
                }
            }
            else{
                for(int i = 0; i < size; i++){
                    if(pivot2traj_dis.get(i) > HR_max.get(i)){
                        HR_max.set(i,pivot2traj_dis.get(i));
                    }
                    if(pivot2traj_dis.get(i) < HR_min.get(i)){
                        HR_min.set(i,pivot2traj_dis.get(i));
                    }
                }
            }
        }
        else if(new_node instanceof TreeNode){
            if(HR_min.size() == 0){
                for(int i = 0;i < size;i++){
                    HR_min.add(((TreeNode) new_node).HR_min.get(i));
                    HR_max.add(((TreeNode) new_node).HR_max.get(i));
                }
            }
            else{
                double sub_min,sub_max;
                for(int i = 0; i < size;i++){
                     sub_min = ((TreeNode) new_node).HR_min.get(i);
                     sub_max = ((TreeNode) new_node).HR_max.get(i);
                     if(HR_min.get(i) > sub_min)
                         HR_min.set(i,sub_min);
                     if(HR_max.get(i) < sub_max)
                         HR_max.set(i,sub_max);
                }
            }

        }



    }

//    /**
//
//     * @ Description :calculate the new cover radius(range) of a treennode by traverse all its subtrees' radius
//
//    */
//
//    private void calCoverRadius(TreeNode node){
//        node.range = 0.0;
//        double t_dis;
//        if(node instanceof LeafNode){
//            for(Node datanodes : node.sub_tree){
//                t_dis = datanodes.parent_dist;
//                if(t_dis > node.range)
//                    node.range = t_dis;
//            }
//        }
//        else{
//            for(Node treenode : node.sub_tree){
//                t_dis = treenode.parent_dist + ((TreeNode) treenode).range;
//                if(t_dis > node.range)
//                    node.range = t_dis;
//            }
//        }
//
//    }
//
//    /**
//
//     * @ Description :calculate the new hyper rings of a treennode by traverse all its subtrees' hyper rings
//
//     */
//    private void calHyperRings(TreeNode node){
//        double t_dis;
//        node.HR_min.clear();
//        node.HR_max.clear();
//        if(node instanceof LeafNode){
//            for(Node datanode : node.sub_tree){
//                update_hyper_rings(node,((DataNode) datanode).PD);
//            }
//        }
//        else{
//            for (int i = 0; i < HR_size; ++i) {
//                double cur_min = Double.POSITIVE_INFINITY;
//                double cur_max = 0.0;
//                for (int j = 0; j < node.sub_tree.size(); ++j) {
//                    double sub_min;
//                    double sub_max;
//                    sub_min = ((TreeNode)node.sub_tree.get(j)).HR_min.get(i);
//                    sub_max = ((TreeNode)node.sub_tree.get(j)).HR_max.get(i);
//                    if (cur_min > sub_min)
//                        cur_min = sub_min;
//                    if (cur_max < sub_max)
//                        cur_max = sub_max;
//                }
//                node.HR_min.add(cur_min);
//                node.HR_max.add(cur_max);
//            }
//        }
//    }


    private void addChild(TreeNode patent, Node newnode){
        patent.sub_tree.add(newnode);
        newnode.patent_node = patent;
        newnode.parent_dist = calculator.GetDistance(patent.object,newnode.object);
        update_range(patent,newnode);
        update_hyper_rings(patent,newnode);
    }

    private void addChild(TreeNode patent, Node newnode,Double dis2patent){
        patent.sub_tree.add(newnode);
        newnode.patent_node = patent;
        newnode.parent_dist = dis2patent;
        update_range(patent,newnode);
        update_hyper_rings(patent,newnode);
    }


    private void insert(Trajectory traj, int id){
        DataNode newnode = new DataNode(traj,id,PD_size);
        cal_pivot_distance(newnode);
        if(root == null){
            root = new LeafNode(traj,id,HR_size);
            addChild(root,newnode,0.0);
        }
        else{
            //update the range for the root node
            double dis = calculator.GetDistance(traj, root.object);
            insert_node(root,newnode,dis);
        }
    }

    /**

     * @ param     :
     * dist2cur_node: the distance of the datanode object to the current node

     * @ Description :to insert a new datanode to a cur_node means insert it to its children/subtrees by traverse the subtrees until getting the leaf node;
     * whenever use this function, the range of the cur_node has already been updated while the hyper_rings has not been updated yet.
     *

    */
    private void insert_node(TreeNode cur_node, DataNode new_node, double dist2cur_node){
        if(cur_node instanceof LeafNode){
            if(Is_Full(cur_node)){
                split(cur_node,new_node);
            }
            else{
                addChild(cur_node,new_node,dist2cur_node);
            }
        }

        else{
            //deal with the current inner node
            update_hyper_rings(cur_node,new_node);
            update_range(cur_node,dist2cur_node);

            //select next range_node
            Nextnode_Dis next_node_dis = findNextNode_and_calDist((InnerNode) cur_node,new_node);
            if(next_node_dis.node == null)
                System.out.println();
            insert_node(next_node_dis.node,new_node,next_node_dis.dis);

        }
    }
    /**

     * @ Description: a wrapped struct for a tree node and a double distance
     * which is used for the return value of function findNextNode_and_calDist

    */
    private class Nextnode_Dis{
        TreeNode node;
        Double dis;
        Nextnode_Dis(TreeNode node,Double dis){
            this.node = node;
            this.dis = dis;
        }
    }


    /**

     * @ param     :
     * cur_node: the current node that we are traversing
     * new_node: the new datanode to be inserted
     * next_node: the pointer of the next node to be found

     * @ return    :
     * the distance between the datanode with the next node

     * @ Description :
     * set the next node, update the range of the next node, and return the distance between the datanode with the next node

    */
    private Nextnode_Dis findNextNode_and_calDist(InnerNode cur_node, DataNode new_node) {
        double result_dis = Double.POSITIVE_INFINITY;
        double back_up_result_dis = Double.POSITIVE_INFINITY;
        TreeNode next_node = null;
        TreeNode back_up_next_node = null;
        for(int i = 0; i < cur_node.sub_tree.size() ; i++){
            double dist = calculator.GetDistance(new_node.object,cur_node.sub_tree.get(i).object);
            double current_range = ((TreeNode)(cur_node.sub_tree.get(i))).range;
            if(Double.compare(dist, current_range)<= 0){
                if(next_node == null || dist < result_dis){
                    next_node = (TreeNode) cur_node.sub_tree.get(i);
                    result_dis = dist;
                }
            }

            else if(next_node == null){
                if(Double.compare(dist - current_range,back_up_result_dis) < 0){
                    back_up_result_dis = dist;
                    back_up_next_node = (TreeNode) cur_node.sub_tree.get(i);
                }
            }
        }
        if(next_node == null){
            next_node = back_up_next_node;
            result_dis = back_up_result_dis;
        }
        return new Nextnode_Dis(next_node,result_dis);
    }

    private boolean Is_Full(TreeNode node){
        return node.sub_tree.size() >= M;
    }

    /**

     * @ Description :calculate the pivot distance arrays of a datanode

    */

    private void cal_pivot_distance(DataNode node){
        double t_dis;
        if(node.PD.size()==0){
            for(int i = 0; i < node.PD_size; i++){
                t_dis = calculator.GetDistance(node.object,trajectories.get(pivots_id.get(i)));
                node.PD.add(t_dis);
            }
        }
    }
    private void cal_pivot_distance(Trajectory trajectory){
        dis_q2p.clear();
        for(int i = 0; i < PD_size; i++){
            dis_q2p.add(calculator.GetDistance(trajectory,trajectories.get(pivots_id.get(i))));
        }
    }

    private void select_pivot(){
        double max_distance = 0.0;
        ArrayList<Integer> tmp;
        for(int i=0;i<N;i++){
            tmp = get_random_number(trajectories.size(),Pivot_num);
            //calculate the total paiwise distance
            double t_distance = 0;
            for(int j = 0;j < Pivot_num;j++){
                for(int k = 0;k < Pivot_num;k++){
                    t_distance += calculator.GetDistance(trajectories.get(tmp.get(j)),trajectories.get(tmp.get(k)));
                }
            }
            if(t_distance > max_distance){
                max_distance = t_distance;
                pivots_id = (ArrayList<Integer>) tmp.clone();
            }
        }
        global_variables.pivot_num.add(N);
    }

    void update_level(Node node,int level){
        if(node instanceof DataNode)
            return;
        else{
            ((TreeNode)node).level = level;
            for(Node sub_node: ((TreeNode)node).sub_tree){
                update_level(sub_node,level+1);
            }
        }
    }

    //**************************functions for knn search**********************************
    /**

     * @ param     :
     * dist_parent_q: the distance between the query and the feature object of the treeNode

    */

    private void search_node(Trajectory query, TreeNode parent,double dist_parent_q) {
        double dist = 0;
        if(parent instanceof LeafNode)
            for(Node item:parent.sub_tree){
                if(Double.compare(Math.abs(dist_parent_q - item.parent_dist),current_r) <= 0
                && IsPivotFilter(item)){
                    dist = calculator.GetDistance(item.object,query);
                    real_count ++;
                    current_r = knnheap.Insert(item.oid,dist);
            }
                System.out.println("leaf prune");
        }
        else{
            for(Node item:parent.sub_tree){
                if(Double.compare(Math.abs(dist_parent_q - item.parent_dist),current_r + ((TreeNode)item).range) <= 0){
                    dist = calculator.GetDistance(item.object,query);
                    if(Double.compare(dist,current_r + ((TreeNode) item).range) <= 0 && IsPivotFilter(item))
                        search_node(query, (TreeNode) item,dist);
                    else if(Double.compare(dist,current_r + ((TreeNode) item).range) <= 0)
                        System.out.println("pivot prune");
                }
                else{
                    System.out.println("range prune");
                }
            }
        }

    }
    private boolean IsPivotFilter(Node node){
        if(node instanceof DataNode){
            double PD_i = 0;
            for (int i = 0; i < PD_size; ++i) {
                PD_i = ((DataNode) node).PD.get(i);
                if (Math.abs(dis_q2p.get(i) - PD_i) > current_r) {
                    return false;
                }
            }
        }
        else if(node instanceof TreeNode){
            for (int i = 0; i < HR_size; ++i) {
                if ((dis_q2p.get(i) - current_r > ((TreeNode) node).HR_max.get(i)) || (dis_q2p.get(i) + current_r < ((TreeNode) node).HR_min.get(i)) ) {
                    return false;
                }
            }
        }
        return true;
    }


}
