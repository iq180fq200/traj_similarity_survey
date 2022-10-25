package indexes.MVP;

import entity.Trajectory;
import indexes.DistanceTrajIDList;

public class LeafNode extends Node{
    DistanceTrajIDList D1,D2;
    public LeafNode(DistanceTrajIDList D1, DistanceTrajIDList D2, int v1_id,int v2_id){
        this.D1 = D1;
        this.D2 = D2;
        super.V1_id = v1_id;
        super.V2_id = v2_id;
    }
}
