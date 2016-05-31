package compilation;

import java.util.ArrayList;

/**
* Created by Felix on 30/05/16.
*/
class SenseNode extends Node {

    public DirectedGraph recSub  = new DirectedGraph();
    public RecMethod recMeth;
    public ArrayList<Node> stores = new ArrayList<Node>();

    public SenseNode(String name){
        super("SENSE",name);
        this.err = 0;
        this.isVisited = false;
    }

    public SenseNode(Node n){
        super(n);
        this.err = 0;
        this.isVisited = false;
    }

    public void copyStore(ArrayList<Node> new_stores){
        this.stores = new ArrayList<Node>(new_stores.size());
        for (int i=0; i<new_stores.size(); i++){
            this.stores.add(new_stores.get(i));
        }
    }
}
