package indexes.MVP;

import entity.Trajectory;

public class InnerNode extends Node{
    double m1,m2,m3;
    Node c1,c2,c3,c4;
    public InnerNode(int v1_id,int v2_id,double m1, double m2, double m3, Node c1, Node c2, Node c3, Node c4){
        this.m1 = m1;
        this.m2 = m2;
        this.m3 = m3;
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.c4 = c4;
        super.V1_id = v1_id;
        super.V2_id = v2_id;

    }
}
