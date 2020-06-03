import java.util.LinkedList;

public class SelfishRoutingAgent implements NetworkAgent{

    private NetworkCostGraph ncg;
    private  int cost [];
    private int predecessor[];

    /**
     * Basically it applies a Dijkstra algo. on the network and gives the shortest path back
     * Super inefficent, since we do this for every agent!
     * @param ncg the adj. matrix of the current latency cost for the network to decide upon
     * @param ec an object to figure out how much an edge costs, given different amounts of agents on it
     * @param decidedAgents the amount of agents, which already decided on a path, i.e. ncg created using ec with decidedAgents
     * @return The optimal path from start node ([0][0] in matrix) to end node ([#nodes - 1][#nodes - 1] in matrix)
     */
    public LinkedList<Integer> agentDecide(NetworkCostGraph ncg, EdgeCosts ec, int decidedAgents) {
        this.ncg = ncg;
        LinkedList<Integer> ret = new LinkedList<>();

        /* -- initialization -- */
        cost = new int[this.ncg.numVertices];
        predecessor = new int[this.ncg.numVertices];
        LinkedList<Integer> nodesLeft = new LinkedList<>();

        for(int i = 0; i < this.ncg.numVertices; i++){
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
            nodesLeft.remove(u);
            if(u == this.ncg.numVertices - 1){
                nodesLeft.clear();
            }
            for (int v:nodesLeft
                 ) {
                if(ec.contains(u, v)){
                    upadtaCost(u, v, ec);
                }
            }
        }

        /* -- Backtracking -- */
        int u = this.ncg.numVertices - 1;
        ret.add(u);
        while(predecessor[u] >= 0){
            u = predecessor[u];
            ret.addFirst(u);
        }
        return ret;
    }

    //TODO use NetworkCostGraph for the cost calculation or EdgeCosts?
    //You should be able to use both, i.e. networkCostGraph is just more efficient,
    // since we saved it there and we do not have to recalculate every time
    private void upadtaCost(int u, int v, EdgeCosts ec){
        int costs = cost[u] + ec.getEdgeCost(u, v);
        if(costs < cost[v]){
            cost[v] = costs;
            predecessor[v] = u;
        }
    }
}
