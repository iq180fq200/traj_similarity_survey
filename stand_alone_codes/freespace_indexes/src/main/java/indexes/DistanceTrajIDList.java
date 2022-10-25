package indexes;

import entity.Trajectory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DistanceTrajIDList {
    public int capability;
    public ArrayList<DistanceTrajIDpair> list;
    public int list_length = 0;

    public void addItem(DistanceTrajIDpair pair) {
        list.add(pair);
        list_length += 1;
    }

    public void addItem(double distance, int id) {
        addItem(new DistanceTrajIDpair(distance, id));
        assert list_length <= capability : "list_length: " + list_length + "capability" + capability;
    }

    public DistanceTrajIDList(int capability) {
        this.capability = capability;
        list = new ArrayList<>(capability);
    }

    public void set_distance(int id, double dist) {
        assert list_length > id;
        list.get(id).SetDistance(dist);
    }

    public DistanceTrajIDList(ArrayList<Trajectory> trajs) {
        this.capability = trajs.size();
        list = new ArrayList<>(capability);
        for (int i = 0; i < trajs.size(); i++)
            addItem(0, i);
    }


    public void removeSwap(int idx) {
        assert idx < list_length;
        if (--list_length > 0) {
            list.set(idx, list.get(list_length));
        }
    }

    public Object clone(){
        DistanceTrajIDList new_list = new DistanceTrajIDList(capability);
        for(int i = 0; i < list_length; i++){
            double dis = list.get(i).dist;
            int id = list.get(i).id;
            new_list.addItem(dis,id);
        }
        return new_list;
    }

    private void sort_by_distance(){
        list.subList(list_length,list.size()).clear();

        Collections.sort(list);
    }

    public double get_median_dist(){
        sort_by_distance();
        if(list_length%2!=0){
            return list.get((list_length - 1)/2).dist;
        }
        else{
            double a = list.get((list_length)/2).dist;
            double b = list.get((list_length)/2 - 1).dist;
            return (a+b)/2;
        }

    }

    public ArrayList<DistanceTrajIDList> divide_sub_list(double m){
        DistanceTrajIDList ss1 = new DistanceTrajIDList(capability);
        DistanceTrajIDList ss2 = new DistanceTrajIDList(capability);
        int comp;
        for(int i = 0;i < list_length;i++){
           comp = Double.compare(list.get(i).dist,m);
           if(comp <= 0){
               ss1.addItem(list.get(i).dist,list.get(i).id);
           }
           if(comp >= 0){
               ss2.addItem(list.get(i).dist,list.get(i).id);
           }

        }
        ArrayList<DistanceTrajIDList> result = new ArrayList<DistanceTrajIDList>();
        result.add(ss1);
        result.add(ss2);
        return result;
    }
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        for(int i = 0;i < list_length - 1;i++){
            result.append(list.get(i).id);
            result.append(",");
        }
        result.append(list.get(list_length - 1).id);
        result.append(";");
        return result.toString();
    }

    public static class DistanceTrajIDpair implements Comparable {
        public int id;
        public double dist;

        public DistanceTrajIDpair(double dis, int id) {
            dist = dis;
            this.id = id;
        }

        void SetDistance(double dis) {
            this.dist = dis;
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof DistanceTrajIDpair) {
                DistanceTrajIDpair other = (DistanceTrajIDpair) o;
                return Double.compare(this.dist, other.dist);
            } else {
                throw new RuntimeException("run type");
            }
        }


    }
}