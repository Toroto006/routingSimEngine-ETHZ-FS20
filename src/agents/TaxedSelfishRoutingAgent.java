package agents;

import simEngine.EdgeCosts;
import simEngine.NetworkCostGraph;
import java.util.LinkedList;

public class TaxedSelfishRoutingAgent implements NetworkAgent {

    private NetworkCostGraph ncg;
    private  double cost [];
    private int predecessor[];

    @Override
    public LinkedList<Integer> agentDecide(NetworkCostGraph ncg, EdgeCosts ec, int decidedAgents, int totalAgents) {
        this.ncg = ncg;
        LinkedList<Integer> ret = new LinkedList<>();
        int last = this.ncg.getNumVertices() - 1;

        /* -- initialization -- */
        cost = new double[last + 1];
        predecessor = new int[last + 1];
        LinkedList<Integer> nodesLeft = new LinkedList<>();

        for(int i = 0; i <= last; i++){
            cost[i] = Integer.MAX_VALUE;
            predecessor[i] = -1;
            nodesLeft.add(i);
        }
        cost[0] = 0;

        /* -- Cost calculation -- */
        while(nodesLeft.size() > 0 ){
            int u = nodesLeft.getFirst();
            for (int uu:nodesLeft
            ) {
                if(cost[uu] < cost[u]){
                    u = uu;
                }
            }
            nodesLeft.remove(nodesLeft.indexOf(u));
            if(u == last){
                nodesLeft.clear();
            }
            for (int v:nodesLeft
            ) {
                if(ec.contains(u, v)){
                    updateCost(u, v, ncg);
                }
            }
        }

        /* -- Backtracking -- */
        int u = this.ncg.getNumVertices() - 1;
        ret.add(u);
        while(predecessor[u] >= 0){
            u = predecessor[u];
            ret.addFirst(u);
        }
        return ret;
    }

    //Both update cost work now!
    private void updateCost(int u, int v, EdgeCosts ec){
        double costs = cost[u] + ec.getEdgeCost(u, v);
        if(costs < cost[v]){
            cost[v] = costs;
            predecessor[v] = u;
        }
    }

    //TODO update cost fct
    private void updateCost(int u, int v, NetworkCostGraph ncg){
        double costs = cost[u] + ncg.getLatencyCost(u, v); //TODO + tax
        if(costs < cost[v]){
            cost[v] = costs;
            predecessor[v] = u;
        }
    }
}
