package agents;


import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;
import simEngine.EdgeCosts;
import simEngine.LinearFct;
import simEngine.NetworkCostGraph;

import java.util.LinkedList;

import static java.lang.System.exit;


public class TaxedSelfishRoutingAgent implements NetworkAgent {

    private double cost[];
    private int predecessor[];
    private LinearFct fct;
    private String name =  "TaxedSelfishRoutingAgent";


    @Override
    public String getName() {
        return this.name;
    }


    public void test() {
        int n = 8;
        Model model = new Model(n + "-queens problem");
        IntVar[] vars = new IntVar[n];
        for(int q = 0; q < n; q++){
            vars[q] = model.intVar("Q_"+q, 1, n);
        }
        for(int i  = 0; i < n-1; i++){
            for(int j = i + 1; j < n; j++){
                model.arithm(vars[i], "!=",vars[j]).post();
                model.arithm(vars[i], "!=", vars[j], "-", j - i).post();
                model.arithm(vars[i], "!=", vars[j], "+", j - i).post();
            }
        }
        Solution solution = model.getSolver().findSolution();
        if(solution != null){
            System.out.println(solution.toString());
        }
    }

    @Override
    public LinkedList<Integer> agentDecide(NetworkCostGraph ncg, EdgeCosts ec, int decidedAgents, int totalAgents) {

        test();
        exit(1);
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
        double costs = cost[u] + (ec.getEdgeCostCustomAgents(u, v, ec.getAgentsOnEdge(u, v) + 1) + ec.getAgentsOnEdge(u, v) * ec.getDerivativeEdgeCost(u, v));
        if (costs < cost[v]) {
            cost[v] = costs;
            predecessor[v] = u;
        }
    }
}
