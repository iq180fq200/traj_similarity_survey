package indexes.LAESA;

import entity.Trajectory;
import indexes.DistanceTrajIDList;
import indexes.Index;
import measures.CalDistance;
import utils.KNNHeap;
import utils.global_variables;

import java.util.ArrayList;
import java.util.HashSet;

public class LAESA extends Index {
    private ArrayList<Trajectory> refp;

    public Trajectory next_refp;
    public double[] accumulate;
    HashSet<Trajectory> refp_hash;
    private double[][] p_dist;//m*p
    private int pivot_num;
    int maxnc;
    public LAESA(ArrayList<Trajectory> trajs, CalDistance calculator,int m){
        super.trajectories = trajs;
        super.calculator = calculator;
        pivot_num = m;
    }

    private int processPoints(double[] rdist, DistanceTrajIDList traj_lowerbound, double threshold, double dxs, int nc){
        double best = Double.POSITIVE_INFINITY; // best candidate
        int bestindex = -1;
        for(int i = 0;i < traj_lowerbound.list_length;i++) {
            double gp = traj_lowerbound.list.get(i).dist;
            // Integrate new information, if available:
            if(rdist != null) {
                final double t = Math.abs(rdist[traj_lowerbound.list.get(i).id] - dxs);
                if(t > gp) {
                    gp = t;
                    traj_lowerbound.list.get(i).dist = gp;
                }
            }
            // Point elimination (nc is only used for reference points)
            if(gp > threshold && nc > maxnc) {
                if(traj_lowerbound.list.get(i).id == 3242)
                    System.out.println();
                traj_lowerbound.removeSwap(i);
                i--;
            }
            else if(gp < best) {
                best = gp;
                bestindex = i;
            }
        }
        if((bestindex != -1) && traj_lowerbound.list.get(bestindex).id == 3242)
            System.out.println();
        return bestindex;
    }

    @Override
    public ArrayList<Integer> knn_search(Trajectory query, int k) {
        this.maxnc = pivot_num / k;
        double dBest = Double.POSITIVE_INFINITY;//the current range of knn
        //create a heap for knn
        KNNHeap knnHeap = new KNNHeap(k);

        //get the lists
        DistanceTrajIDList P_lowerbound = new DistanceTrajIDList(trajectories.size() - pivot_num);
        DistanceTrajIDList RP_lowerbound = new DistanceTrajIDList(pivot_num);

        //initialize
        for(int i=0;i<trajectories.size();i++){
            Trajectory traj = trajectories.get(i);
            (refp_hash.contains(traj) ? RP_lowerbound : P_lowerbound).addItem(0,i);
        }

        DistanceTrajIDList.DistanceTrajIDpair s = RP_lowerbound.list.get(0);
        RP_lowerbound.removeSwap(0);

        boolean isrp = true;
        int nc = 0;
        Trajectory pivot;

        while(true){
            pivot = trajectories.get(s.id);
            final double dxs = calculator.GetDistance(query,pivot);
            nc++;
            dBest = knnHeap.Insert(s.id,dxs);
            double[] rdist = isrp?p_dist[findInRef(s.id)] : null;
            int rpsi = processPoints(rdist,RP_lowerbound,dBest,dxs,nc);
            int psi = processPoints(rdist,P_lowerbound,dBest,dxs,Integer.MAX_VALUE);

            // Choose next element s:
            if(rpsi !=-1) {
                s = RP_lowerbound.list.get(rpsi);
                RP_lowerbound.removeSwap(rpsi);
                isrp = true;
            }
            else if(psi != -1) {
                s = P_lowerbound.list.get(psi);
                P_lowerbound.removeSwap(psi);
                isrp = false;
            }
            else {
                break; // No more candidates
            }

        }
        global_variables.answers.add(knnHeap.getResults());
        global_variables.prune_rates.add(1.0 * (trajectories.size() - nc) / trajectories.size());
        return null;
    }

    private int findInRef(int id){
        Trajectory pivot = trajectories.get(id);
        for(int i = 0; i < refp.size(); i++)
            if(refp.get(i).equals(pivot))
                return i;
        return -1;
    }

    @Override
    public Boolean build_index() {
        BP_selection();
        return null;
    }

    private void BP_selection(){
        int m = pivot_num;
        // Store chosen reference points
        p_dist = new double[m][trajectories.size()];
        refp = new ArrayList<>();

        refp_hash = new HashSet<>();
        double max;
        accumulate = new double[trajectories.size()];
        Trajectory b;
//        Trajectory last_ref = get_random(super.trajectories);
        Trajectory last_ref = trajectories.get(0);
        refp.add(last_ref);
        refp_hash.add(last_ref);
        while(refp.size() < m){
            max = 0;
            b = last_ref;
            int b_id = refp.size()-1;
            for (int j=0;j<trajectories.size();j++){
                Trajectory item = trajectories.get(j);
                //calculate the distance between other pivots to this pivot
                if(refp.contains(item)){
                    if(item.equals(b))
                        p_dist[b_id][j] = 0;
                    else{
                        p_dist[b_id][j] = calculator.GetDistance(b,item);
                    }
                    continue;
                }

                p_dist[b_id][j] = calculator.GetDistance(b,item);
                accumulate[j] += p_dist[b_id][j];
                if (accumulate[j] > max){
                    max = accumulate[j];
                    last_ref = item;
                }
            }
            System.out.println("pivot"+refp.size()+"has done");
            refp.add(last_ref);
            refp_hash.add(last_ref);
        }

        max = 0;
        b = last_ref;
        int b_id = refp.size()-1;
        for (int j=0;j<trajectories.size();j++){
            Trajectory item = trajectories.get(j);
            //calculate the distance between other pivots to this pivot
            if(refp.contains(item)){
                if(item.equals(b))
                    p_dist[b_id][j] = 0;
                else{
                    p_dist[b_id][j] = calculator.GetDistance(b,item);
                }
                continue;
            }

            p_dist[b_id][j] = calculator.GetDistance(b,item);
            accumulate[j] += p_dist[b_id][j];
            if (accumulate[j] > max){
                max = accumulate[j];
                last_ref = item;
            }
        }
        System.out.println("pivot"+refp.size()+"has done");
        next_refp = last_ref;
    }


    @Override
    public Boolean build_index_from_prev(Index prev_index) {
        BP_selection_on_prev(prev_index);
        return null;
    }

    private void BP_selection_on_prev(Index prev_index){
        LAESA _index = (LAESA) prev_index;
        int m = pivot_num;
        p_dist = new double[m][trajectories.size()];

        //deep copy from the previous index
        refp = new ArrayList<>(_index.refp);
        accumulate = _index.accumulate.clone();
        refp_hash = new HashSet<>(_index.refp_hash);
        int i = 0;
        for(double[] item : _index.p_dist){
            p_dist[i] = item.clone();
            i++;
        }
        System.out.println("pivot 1 to "+refp.size()+"has done");



        double max;
        Trajectory b;
        Trajectory last_ref = ((LAESA) prev_index).next_refp;
        refp.add(last_ref);
        refp_hash.add(last_ref);
        while(refp.size() < m){
            max = 0;
            b = last_ref;
            int b_id = refp.size()-1;
            for (int j=0;j<trajectories.size();j++){
                Trajectory item = trajectories.get(j);
                //calculate the distance between other pivots to this pivot
                if(refp.contains(item)){
                    if(item.equals(b))
                        p_dist[b_id][j] = 0;
                    else{
                        p_dist[b_id][j] = calculator.GetDistance(b,item);
                    }
                    continue;
                }

                p_dist[b_id][j] = calculator.GetDistance(b,item);
                accumulate[j] += p_dist[b_id][j];
                if (accumulate[j] > max){
                    max = accumulate[j];
                    last_ref = item;
                }
            }
            System.out.println("pivot"+refp.size()+"has done");
            refp.add(last_ref);
            refp_hash.add(last_ref);
        }

        max = 0;
        b = last_ref;
        int b_id = refp.size()-1;
        for (int j=0;j<trajectories.size();j++){
            Trajectory item = trajectories.get(j);
            //calculate the distance between other pivots to this pivot
            if(refp.contains(item)){
                if(item.equals(b))
                    p_dist[b_id][j] = 0;
                else{
                    p_dist[b_id][j] = calculator.GetDistance(b,item);
                }
                continue;
            }

            p_dist[b_id][j] = calculator.GetDistance(b,item);
            accumulate[j] += p_dist[b_id][j];
            if (accumulate[j] > max){
                max = accumulate[j];
                last_ref = item;
            }
        }
        System.out.println("pivot"+refp.size()+"has done");
        next_refp = last_ref;
    }


}
