package agents;

import simEngine.Edge;
import simEngine.EdgeCosts;
import simEngine.NetworkCostGraph;
import simEngine.SimConfig;

import java.util.*;

public class CentralizedAgent implements NetworkAgent {
    static HashMap<Integer, LinkedList<Integer>> solutions;
    static private int totAgents;
    static private EdgeCosts ec;
    static private HashMap<Integer[], Double> distributionCost = new HashMap<>();
    static private ArrayList<LinkedList<Integer>> uniquePaths;
    static private int nrUniquePaths;

    LinkedList<Integer>[] adjList;
    boolean[] visitedDFS;
    boolean[] visitedPath;
    // list of paths (LinkedList) for each node
    ArrayList<ArrayList<LinkedList<Integer>>> pathsToDest;
    int numVertices;

    @Override
    public LinkedList<Integer> agentDecide(NetworkCostGraph ncg, EdgeCosts ec1, int decidedAgents, int totalAgents) {
        // either calculate solution and use it or just use pre-calculated solution
        if (decidedAgents == 0 || solutions.isEmpty()) {
            totAgents= totalAgents;
            ec = ec1;
            solutions = new HashMap<>(totalAgents);
            solutions = completeSolution(ncg);
        }
        return solutions.get(decidedAgents);
    }

    /**
     * This will calculate the solution for all agents by finding the optimal paths and corresponding loads.
     *
     * @param ncg the adj. matrix of the current latency cost for the network to decide upon
     * @return Map of agent nr. to an optimal path. Multiple agents may refer to same path.
     */
    public HashMap<Integer, LinkedList<Integer>> completeSolution(NetworkCostGraph ncg) {
        uniquePaths = getUniquePaths(ncg);
        nrUniquePaths = uniquePaths.size();

        // calculate all distribution costs
        recDistributionCost(0, new Integer[nrUniquePaths], totAgents);

        // find minimal value of all distribution costs
        double min = Integer.MAX_VALUE;
        for (Map.Entry<Integer[], Double> current : distributionCost.entrySet()) {
            min = Math.min(min, current.getValue());
        }

        // save all distributions that create a minimal value in list
        ArrayList<Integer[]> bestDistributions = new ArrayList<>();
        for (Map.Entry<Integer[], Double> current : distributionCost.entrySet()) {
            if (current.getValue() == min) {
                bestDistributions.add(current.getKey());
            }
        }

        // do not know how to handle multiple minimal distributions
        // for now just choosing first one
        Integer[] bestDistribution = bestDistributions.get(0);

        HashMap<Integer, LinkedList<Integer>> solutionsPerAgent = new HashMap<>(totAgents);
        int agentsum = 0;
        for (int i = 0; i < bestDistribution.length; i++) {
            int agents = bestDistribution[i];
            for (int j = 0; j < agents; j++) {
                solutionsPerAgent.put(agentsum+j, uniquePaths.get(i));
            }
            agentsum += agents;
        }

        return solutionsPerAgent;
    }

    /**
     * Recursive function to calculate all distributions over unique paths and save cost of each
     * distribution-path-combination in DP table.
     */
    public void recDistributionCost(int current, Integer[] distribution, int leftToDistribute) {
        if (current == nrUniquePaths-1) {
            distribution[current] = leftToDistribute;
            distributionCost.put(distribution, calcDistributionCost(distribution));
            return;
        }
        for (int i = 0; i < leftToDistribute; i++) {
            distribution[current] = i;
            recDistributionCost(current+1, distribution, leftToDistribute-1);
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

    // Prints all paths from
    // 's' to 'd'
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
    private void printAllPathsUtil(Integer u, Integer d,
                                   boolean[] isVisited,
                                   List<Integer> localPathList) {

        // Mark the current node
        isVisited[u] = true;

        if (u.equals(d))
        {
            System.out.println(localPathList);
            LinkedList<Integer> temp = new LinkedList<Integer>(localPathList);
            uniquePaths.add(temp);
            // if match found then no need to traverse more till depth
            isVisited[u]= false;
            return ;
        }

        // Recur for all the vertices
        // adjacent to current vertex
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


    private void prependNodeToPath (int current, int next) {
        Iterator<LinkedList<Integer>> it = pathsToDest.get(next).listIterator();
        LinkedList<Integer> list = new LinkedList<>();
        while (it.hasNext()) {
            list.clear();
            list.add(current);
            list.addAll(it.next()); // now current is prepended to a path from next to dest
            pathsToDest.get(current).add(list);
        }
    }

    /**
     * Calculate cost of a given path.
     *
     * @param path path as a list of nodes
     * @return Cost as a real value.
     */
    public double calcPathCost(LinkedList<Integer> path) {
        double pathCost = 0;
        // empty path or only 1 node
        if (path.isEmpty() || path.size() < 2) {
            return 0.0;
        }

        // path of at least length 1

        Iterator<Integer> it = path.listIterator();

        int current = it.next();
        int next = it.next();
        while (it.hasNext()) {
            pathCost += ec.getEdgeCost(current, next);
            current = next;
            next = it.next();
        }
        pathCost += ec.getEdgeCost(current, next);
        return pathCost;
    }

    /**
     * Calculates the total cost of a given distribution
     *
     * @param distribution distribution of agents over unique paths.
     * @return Cost to route agents according to distribution over paths
     */
    public Double calcDistributionCost(Integer[] distribution) {
        double distributionSum = 0;
        for (int i = 0; i < uniquePaths.size(); i++) {
            distributionSum += distribution[i] * calcPathCost(uniquePaths.get(i));
        }
        return (1.0/totAgents*distributionSum);
    }
}
