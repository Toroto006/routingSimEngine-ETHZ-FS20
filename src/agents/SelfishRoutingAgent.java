package agents;

import simEngine.*;

import java.util.LinkedList;

public class SelfishRoutingAgent implements NetworkAgent {

    private double cost[];
    private int predecessor[];
    private String name = "SelfishRoutingAgent";

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Basically it applies a Dijkstra algo. on the network and gives the shortest path back
     * Super inefficent, since we do this for every agent!
     *
     * @param ncg           the adj. matrix of the current latency cost for the network to decide upon
     * @param ec            an object to figure out how much an edge costs, given different amounts of agents on it
     * @param decidedAgents the amount of agents, which already decided on a path, i.e. ncg created using ec with decidedAgents
     * @return The optimal path from start node ([0][0] in matrix) to end node ([#nodes - 1][#nodes - 1] in matrix)
     */
    public LinkedList<Integer> agentDecide(NetworkCostGraph ncg, EdgeCosts ec, int decidedAgents, int totalAgents) {
        LinkedList<Integer> ret = new LinkedList<>();
        int last = ncg.getNumVertices() - 1;

        /* -- initialization -- */
        cost = new double[last + 1];
        predecessor = new int[last + 1];
        LinkedList<Integer> nodesLeft = new LinkedList<>();

        for (int i = 0; i <= last; i++) {
            cost[i] = Integer.MAX_VALUE;
            predecessor[i] = -1;
            nodesLeft.add(i);
        }
        cost[0] = 0;

        /* -- Cost calculation -- */
        while (nodesLeft.size() > 0) {
            int u = nodesLeft.getFirst();
            for (int uu : nodesLeft
            ) {
                if (cost[uu] < cost[u]) {
                    u = uu;
                }
            }
            nodesLeft.remove(nodesLeft.indexOf(u));
            if (u == last) {
                nodesLeft.clear();
            }
            for (int v : nodesLeft
            ) {
                if (ec.contains(u, v)) {
                    updateCost(u, v, ncg);
                }
            }
        }

        /* -- Backtracking -- */
        int u = ncg.getNumVertices() - 1;
        ret.add(u);
        while (predecessor[u] >= 0) {
            u = predecessor[u];
            ret.addFirst(u);
        }
        return ret;
    }

    private void updateCost(int u, int v, NetworkCostGraph ncg) {
        double costs = cost[u] + ncg.getLatencyCost(u, v);
        if (costs < cost[v]) {
            cost[v] = costs;
            predecessor[v] = u;
        }
    }
}
