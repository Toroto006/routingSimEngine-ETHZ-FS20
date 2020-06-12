package agents;


import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import simEngine.EdgeCosts;
import simEngine.LinearFct;
import simEngine.NetworkCostGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static java.lang.System.exit;


public class TaxedSelfishRoutingAgent implements NetworkAgent {

    private double cost[];
    private int predecessor[];
    private LinearFct fct;
    private String name = "TaxedSelfishRoutingAgent";
    private int numVertices;
    private LinkedList<Integer>[] adjList;
    private ArrayList<LinkedList<Integer>> uniquePaths;
    private LinkedList<SolutionPath> solutions;

    private final double PRECISION = 0.0001d;
    private final double MAXVALUE = 100d;

    @Override
    public String getName() {
        return this.name;
    }


    public void test() {
        int n = 8;
        Model model = new Model(n + "-queens problem");
        IntVar[] vars = new IntVar[n];
        for (int q = 0; q < n; q++) {
            vars[q] = model.intVar("Q_" + q, 1, n);
        }
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                model.arithm(vars[i], "!=", vars[j]).post();
                model.arithm(vars[i], "!=", vars[j], "-", j - i).post();
                model.arithm(vars[i], "!=", vars[j], "+", j - i).post();
            }
        }
        Solution solution = model.getSolver().findSolution();
        if (solution != null) {
            System.out.println(solution.toString());
        }
    }

    @Override
    public LinkedList<Integer> agentDecide(NetworkCostGraph ncg, EdgeCosts ec, int decidedAgents, int totalAgents) {
        if (decidedAgents == 0 || solutions.isEmpty()) {
            HashMap<String, LinkedList<Integer>> edges = new HashMap<>();
            ArrayList<LinkedList<Integer>> uniquePaths = getUniquePaths(ncg);
            int amountPaths = uniquePaths.size();


            for (int p = 0; p < amountPaths; p++) {
                LinkedList<Integer> path = uniquePaths.get(p);
                Integer from = path.get(0);
                for (int e = 1; e < path.size(); e++) {
                    Integer to = path.get(e);
                    if (!edges.containsKey(from + " " + to))
                        edges.put(from + " " + to, new LinkedList<Integer>());
                    edges.get(from + " " + to).add(p);

                    from = to;
                }
            }

            Model model = new Model(getName() + "-Routing-" + ncg.getClass().getSimpleName());
            RealVar[] vars = new RealVar[amountPaths];
            for (int q = 0; q < amountPaths; q++) {
                vars[q] = model.realVar("p_" + q, 0d, 1d, PRECISION);
            }
            for (int p = 0; p < amountPaths; p++) {
                LinkedList<Integer> path = uniquePaths.get(0);
                Integer from = path.get(0);
                for (int e = 1; e < path.size(); e++) {
                    Integer to = path.get(e);
                    LinkedList<Integer> edgeList = edges.get(from + " " + to);
                    Float[] par = ec.getParameters(from, to);
                    LinkedList<RealVar> ls = new LinkedList<>();
                    for (Integer i : edgeList) {
                        RealVar x1 = model.realVar(0d, MAXVALUE, PRECISION);
                        model.arithm(vars[p], "*", model.realVar(par[0]), "=", x1);
                        RealVar temp = model.realVar("p:" + p + ",e:" + from + "-" + to, 0d, MAXVALUE, PRECISION);
                        model.sum(new RealVar[]{}, "=", )
                        ls.add();
                    }
                    from = to;

                }
            }
        }
        exit(13);
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

    /**
     * This will create a list of all unique paths without loops from source to destination.
     *
     * @param ncg the adj. matrix of the current latency cost for the network to decide upon
     * @return List of all unique paths.
     */
    public ArrayList<LinkedList<Integer>> getUniquePaths(NetworkCostGraph ncg) {
        int source = 0;
        int dest = ncg.getAdjMatrix().length - 1;
        numVertices = ncg.getNumVertices();

        //create adjacency list
        adjList = new LinkedList[numVertices];
        for (int i = 0; i < adjList.length; i++) {
            adjList[i] = new LinkedList<Integer>();
            for (int j = 0; j < numVertices; j++) {
                if (ncg.getAdjMatrix()[i][j] != Float.MAX_VALUE) {
                    adjList[i].add(j);
                }
            }
        }
        uniquePaths = new ArrayList<>();
        printAllPaths(source, dest);

        return uniquePaths;
    }

    /*
    Thanks to https://www.geeksforgeeks.org/find-paths-given-source-destination/, Himanshu Shekhar
    for printAllPaths and printAllPathsUtil
     */

    /**
     * Finds all paths from s to d
     *
     * @param s source node
     * @param d destination node
     */
    public void printAllPaths(int s, int d)
    {
        boolean[] isVisited = new boolean[numVertices];
        ArrayList<Integer> pathList = new ArrayList<>();

        //add source to path[]
        pathList.add(s);

        //Call recursive utility
        printAllPathsUtil(s, d, isVisited, pathList);
    }

    // A recursive function to print
    // all paths from 'u' to 'd'.
    // isVisited[] keeps track of
    // vertices in current path.
    // localPathList<> stores actual
    // vertices in the current path

    /**
     * A recursive function to print all paths from 'u' to 'd'.
     *
     * @param u current node to find path from to d
     * @param d destination node
     * @param isVisited keeps track of visited nodes
     * @param localPathList current found path
     */
    private void printAllPathsUtil(Integer u, Integer d, boolean[] isVisited, List<Integer> localPathList) {

        // Mark the current node
        isVisited[u] = true;

        if (u.equals(d))
        {
            //System.out.println(localPathList);
            LinkedList<Integer> temp = new LinkedList<Integer>(localPathList);
            uniquePaths.add(temp);
            // if match found then no need to traverse more till depth
            isVisited[u]= false;
            return ;
        }

        // Recur for all the vertices adjacent to current vertex
        for (Integer i : adjList[u])
        {
            if (!isVisited[i])
            {
                // store current node
                // in path[]
                localPathList.add(i);
                printAllPathsUtil(i, d, isVisited, localPathList);

                // remove current node
                // in path[]
                localPathList.remove(i);
            }
        }

        // Mark the current node
        isVisited[u] = false;
    }
}
