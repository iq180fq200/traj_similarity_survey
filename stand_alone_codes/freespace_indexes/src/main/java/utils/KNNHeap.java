package utils;

import indexes.DistanceTrajIDList;

import java.util.*;

public class KNNHeap {
    public int k;
    public KNNHeap(int k){
        this.k = k;
    }
    private class comparator implements Comparator<DistanceTrajIDList.DistanceTrajIDpair>{
        @Override
        public int compare(DistanceTrajIDList.DistanceTrajIDpair o1, DistanceTrajIDList.DistanceTrajIDpair o2) {
            if(o1.dist > o2.dist)
                return -1;
            else if(o1.dist < o2.dist)
                return 1;
            else
            return 0;
        }
    }
    PriorityQueue<DistanceTrajIDList.DistanceTrajIDpair> pq =new PriorityQueue<>(new comparator());
    HashSet<Integer> insert_hash = new HashSet<>();
    public double Insert(int id,double distance){
        if(insert_hash.contains(id))
            if(pq.size() < k)
                return Double.POSITIVE_INFINITY;
            else
                return pq.peek().dist;

        insert_hash.add(id);
        if (pq.size() >= k){
            if(pq.peek().dist < distance)
                return pq.peek().dist;
            else{
                pq.add(new DistanceTrajIDList.DistanceTrajIDpair(distance,id));
                pq.poll();
                return pq.peek().dist;
            }
        }
        else{
            pq.add(new DistanceTrajIDList.DistanceTrajIDpair(distance,id));
            if(pq.size() < k)
                return Double.POSITIVE_INFINITY;
            else
                return pq.peek().dist;
        }
    }

    public ArrayList<Integer> getResults(){
        System.out.println(pq.size());
        ArrayList<Integer> _result = new ArrayList<Integer>();
        for(int i = 0;i < k;i++)
            _result.add(pq.poll().id);
        Collections.reverse(_result);
        return _result;
    }
}
