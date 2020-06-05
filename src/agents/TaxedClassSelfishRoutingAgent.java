package agents;

import simEngine.EdgeCosts;
import simEngine.NetworkCostGraph;

import java.util.LinkedList;

public class TaxedClassSelfishRoutingAgent implements NetworkAgent {

    private double cost[];
    private int predecessor[];
    private double factor;

    @Override
    public LinkedList<Integer> agentDecide(NetworkCostGraph ncg, EdgeCosts ec, int decidedAgents, int totalAgents) {

        factor = 4.0 / totalAgents * decidedAgents;
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
            for (int uu : nodesLeft) { //find next node with lowest cost
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
                    updateCost(u, v, ec);
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

    private void updateCost(int u, int v, EdgeCosts ec) {
        double costs = cost[u] + ec.getEdgeCostCustomAgents(u, v, ec.getAgentsOnEdge(u, v) + 1) + ec.getAgentsOnEdge(u, v) * ec.getDerivativeEdgeCost(u, v) * factor;
        if (costs < cost[v]) {
            cost[v] = costs;
            predecessor[v] = u;
        }
    }
}
