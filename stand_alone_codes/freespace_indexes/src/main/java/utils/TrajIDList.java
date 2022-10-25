//package utils;
//
//import entity.Trajectory;
//
//import java.util.ArrayList;
//
///**
//
// * @ Description: to use integer(traj ID) to represent the trajectorylist
//
// */
//public class TrajIDList{
//    ArrayList<Trajectory> trajs;
//    private ArrayList<Integer> ids;
//    public int list_length;
//    public TrajIDList(ArrayList<Trajectory> trajs){
//        this.trajs = trajs;
//        ids = new ArrayList<>(trajs.size());
//        for(int i = 0;i < trajs.size();i++)
//            ids.set(i,i);
//        list_length = trajs.size();
//    }
//    public void removeSwap(int idx){
//        assert idx < list_length;
//        if(--list_length > 0){
//            ids.set(idx,ids.get(list_length));
//        }
//    }
//    public void remove(int idx){
//        ids.remove(idx);
//        list_length --;
//    }
//    public order_by_distances(double[] distances){
//
//    }
//
//}
