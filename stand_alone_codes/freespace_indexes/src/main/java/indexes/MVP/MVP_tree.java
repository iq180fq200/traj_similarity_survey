package indexes.MVP;

import entity.Trajectory;
import indexes.DistanceTrajIDList;
import indexes.Index;
import measures.CalDistance;
import utils.KNNHeap;
import utils.global_variables;

import java.util.ArrayList;
import java.util.Queue;

public class MVP_tree extends Index {
    
    public final int k,p;
    public ArrayList<Trajectory> trajectories;
    public CalDistance calculator;
    public Node root = null;
    private double[][] path;
    private double current_r;
    private int pivot_num=0;
    KNNHeap knnheap;
    int real_count = 0;

    double[] distances;

/**

 * @ param     :
 * k (maximum fanout number of the leaf nodes)
 * p (number of distances to be recorded along the path)
 * traj (trajectories to be queried)

 * @ return    :
 * none

 * @ Description :
 * MVP tree definition without building. Use build_index to build
 * after define the MVP tree

*/

    public MVP_tree(int k, int p, int m, ArrayList<Trajectory> trajectories, CalDistance calculator) {
        this.k = k;
        this.p = p;
        this.trajectories = trajectories;
        this.calculator = calculator;
        this.path = new double[trajectories.size()][p];
        this.distances = new double[trajectories.size()];
        for(int i =0;i<trajectories.size();i++)
            distances[i] = -1.0;
    }
    @Override
    public Boolean build_index(){
        root = build_tree(new DistanceTrajIDList(trajectories),0);
        global_variables.pivot_num.add(pivot_num);
        return true;
    }


    public Node build_tree(DistanceTrajIDList idlist,int level) {
        DistanceTrajIDList idlist1 = (DistanceTrajIDList) idlist.clone();//for store the current D1
        int S_v1_id,S_v2_id;//the two pivots
        Trajectory S_v1,S_v2;
        if(idlist.list_length == 0)
            return null;//all the points(trajs in this question) to be deal with
        // if no inner nodes
        if(idlist.list_length <= k + 2){
//            int index = (int) (Math.random()* idlist.list_length);
            int index = 0;
            S_v1_id = idlist.list.get(index).id;
            S_v1 = get_traj_idlist(index,idlist);

            idlist1.removeSwap(index);//get a random node as the first pivot

            cal_all_dis(idlist1,S_v1,level,false);
            pivot_num++;
            System.out.println("pivot "+pivot_num+" has done");

            DistanceTrajIDList idlist2 = (DistanceTrajIDList) idlist1.clone();//for store the current D2

            int max_id = find_max_distance_id(idlist1);//get the second pivot
            S_v2_id = idlist1.list.get(max_id).id;
            S_v2 = get_traj_idlist(max_id,idlist1);
            idlist2.removeSwap(max_id);
            idlist1.removeSwap(max_id);

            try{
                if(idlist2.list_length != idlist1.list_length){
                    throw new Exception("debug exception");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            cal_all_dis(idlist2,S_v2,level+1,false);
            pivot_num++;
            System.out.println("pivot "+pivot_num+" has done");

            return new LeafNode(idlist1,idlist2,S_v1_id,S_v2_id);
        }

        //if have inner nodes
        else{
//            int index = (int) (Math.random()* idlist.list_length);
            int index = 0;
            S_v1 = get_traj_idlist(index,idlist);
            S_v1_id = idlist.list.get(index).id;
            idlist1.removeSwap(index);//get a random node as the first pivot

            cal_all_dis(idlist1,S_v1,level,true);
            pivot_num++;
            System.out.println("pivot "+pivot_num+" has done");

            //find the middle
            double m1 = idlist1.get_median_dist();

            ArrayList<DistanceTrajIDList>sublists = idlist1.divide_sub_list(m1);

            DistanceTrajIDList ss1 = sublists.get(0);//all the ss1 is less than m1
            DistanceTrajIDList ss2 = sublists.get(1);

            S_v2_id = ss2.list.get(ss2.list_length -1).id;
            S_v2 = trajectories.get(S_v2_id);
           ss2.removeSwap(ss2.list_length - 1);

           cal_all_dis(ss2,S_v2,level+1,true);
           cal_all_dis(ss1,S_v2,level+1,true);
            pivot_num++;
            System.out.println("pivot "+pivot_num+" has done");


            //find the other middles and divide the children sets
            double m2 = ss1.get_median_dist();
            double m3 = ss2.get_median_dist();

            ArrayList<DistanceTrajIDList>sublists_1 = ss1.divide_sub_list(m2);
            DistanceTrajIDList ss11 = sublists_1.get(0);
            DistanceTrajIDList ss12 = sublists_1.get(1);

            ArrayList<DistanceTrajIDList>sublists_2 = ss2.divide_sub_list(m3);
            DistanceTrajIDList ss21 = sublists_2.get(0);//all the ss1 is less than m1
            DistanceTrajIDList ss22 = sublists_2.get(1);


            return new InnerNode(S_v1_id,S_v2_id,m1,m2,m3,build_tree(ss11,level + 2),build_tree(ss12,level + 2),build_tree(ss21,level + 2),build_tree(ss22,level + 2));
        }

    }
    private void cal_all_dis(DistanceTrajIDList idlist,Trajectory pivot,int level,boolean record){
        for(int i=0; i< idlist.list_length;i++){
//                if(idlist.list.get(i).id == 91)
//                    System.out.println();
                double dis = calculator.GetDistance(pivot,get_traj_idlist(i,idlist));
                idlist.set_distance(i,dis);
                if(record && level < p){
                   path[idlist.list.get(i).id][level] = dis;
                }//set the path
            }

    }

    Trajectory get_traj_idlist(int i, DistanceTrajIDList trajectories_list){
        int id = trajectories_list.list.get(i).id;
        return trajectories.get(id);

    }

    @Override
    public ArrayList<Integer> knn_search(Trajectory query, int k) {
        knnheap= new KNNHeap(k);
        current_r = Double.POSITIVE_INFINITY;
        double[] current_path = new double[p];
        for(int i = 0;i < p;i++)
            current_path[i] = -1;
        real_count = 0;
        search_node(query,root,current_path,0);
        global_variables.answers.add(knnheap.getResults());
        global_variables.prune_rates.add(1.0 * (trajectories.size() - real_count) / trajectories.size());
        return null;
    }
    private double get_distance(Trajectory query, int id){
        double result;
        if(distances[id] < 0){
            result = calculator.GetDistance(query,trajectories.get(id));
            real_count++;
            distances[id] = result;
            return result;
        }
        else
            return distances[id];
    }

    private void search_node(Trajectory query,Node tree,double[] current_path,int level){
        double d_q_sv1,d_q_sv2;
        d_q_sv1 = get_distance(query,tree.V1_id);

        d_q_sv2 = get_distance(query,tree.V2_id);
        if(d_q_sv1 <= current_r)
            current_r = knnheap.Insert(tree.V1_id,d_q_sv1);
        if(d_q_sv2 <= current_r)
            current_r = knnheap.Insert(tree.V2_id, d_q_sv2);
        if(tree instanceof LeafNode){
            double d_s_sv1;
            double d_s_sv2;
            int s_id;
            double d_s_q;
            for(int i = 0;i<((LeafNode) tree).D1.list_length;i++){
                d_s_sv1 = ((LeafNode) tree).D1.list.get(i).dist;
                d_s_sv2 = ((LeafNode) tree).D2.list.get(i).dist;
                s_id = ((LeafNode) tree).D1.list.get(i).id;
                if(Math.abs(d_q_sv1 - d_s_sv1) < current_r &&
                Math.abs(d_q_sv2 - d_s_sv2) < current_r){
                    boolean flag = true;
                    for(int j = 0;j<p;j++){
                        if(current_path[j] < 0){
                            break;
                        }
                        else if(Math.abs(current_path[j] - path[s_id][j]) >= current_r){
                            flag = false;
                            break;
                        }
                    }
                    if(flag){
                        d_s_q = get_distance(query,s_id);
                        current_r = knnheap.Insert(s_id,d_s_q);
                    }
//                    else{
//                        System.out.println("prune");
//                    }
                }
//                else{
//                    System.out.println("prune");
//                }
            }

        }
        else{
            if(level < p)
                current_path[level] = d_q_sv1;
            if(level < p - 1)
                current_path[level+1] = d_q_sv2;
            if(d_q_sv1 + current_r > ((InnerNode) tree).m1){
                if(d_q_sv2 + current_r > ((InnerNode) tree).m3)
                    search_node(query,((InnerNode) tree).c4,current_path.clone(),level + 2);
//                else{
//                    System.out.println("prune");
//                }
                if(d_q_sv2 - current_r < ((InnerNode) tree).m3)
                    search_node(query,((InnerNode) tree).c3,current_path.clone(),level + 2);
//                else{
//                    System.out.println("prune");
//                }
            }
            if(d_q_sv1 - current_r < ((InnerNode) tree).m1){
                if(d_q_sv2 + current_r > ((InnerNode) tree).m2)
                    search_node(query,((InnerNode) tree).c2,current_path.clone(),level + 2);
//                else{
//                    System.out.println("prune");
//                }
                if(d_q_sv2 - current_r < ((InnerNode) tree).m2)
                    search_node(query,((InnerNode) tree).c1,current_path.clone(),level + 2);
//                else{
//                    System.out.println("prune");
//                }
            }

        }

    }

    @Override
    public Boolean build_index_from_prev(Index prev_index) {
        return null;
    }
    /**

     * @ param     :index of item in the idlist

     * @ return    :trajectory

     * @ Description :get the i_th trajectory in the idlist

    */

    private int find_max_distance_id(DistanceTrajIDList idlist){
        double max = Double.NEGATIVE_INFINITY;
        int max_id = -1;
        double crt_dist; //current distance
        for(int i = 0;i < idlist.list_length; i++){
            crt_dist = idlist.list.get(i).dist;
            if(crt_dist > max){
                max_id = i;
                max = crt_dist;
            }

        }
        return max_id;
    }

    public void print_index(){
        if(root == null)
            return;
        ArrayList<object_flag> queue = new ArrayList<>();
        queue.add(new object_flag(root,true));
        object_flag item;
        while(queue.size()!=0){
            item = queue.get(0);
            queue.remove(0);
            print_node(item.item,item.flag);
            if(item.item instanceof InnerNode){
                queue.add(new object_flag(((InnerNode) item.item).c1,false));
                queue.add(new object_flag(((InnerNode) item.item).c2,false));
                queue.add(new object_flag(((InnerNode) item.item).c3,false));
                if(item.flag)
                    queue.add(new object_flag(((InnerNode) item.item).c4,true));
                else
                    queue.add(new object_flag(((InnerNode) item.item).c4,false));
            }
            else if(item.item instanceof LeafNode){
                if(item.flag)
                    queue.add(new object_flag(((LeafNode) item.item).D1,true));
                else
                    queue.add(new object_flag(((LeafNode) item.item).D1,false));
            }
        }

    }

    private class object_flag{
        public boolean flag;
        public Object item;
        object_flag(Object item,boolean flag){
            this.flag = flag;
            this.item = item;
        }
    }

    private void print_node(Object node,boolean last_in_level){
        if (node instanceof InnerNode){
            System.out.print(((InnerNode) node).V1_id);
            System.out.print(",");
            System.out.print(((InnerNode) node).V2_id);
            System.out.print(";");
            if(last_in_level){
                System.out.println();
            }
        }
        else if(node instanceof LeafNode){
            System.out.print(((LeafNode) node).V1_id);
            System.out.print(",");
            System.out.print(((LeafNode) node).V2_id);
            System.out.print(";");
            if(last_in_level){
                System.out.println();
            }
        }
        else{
            System.out.print(node);
        }
    }


}
