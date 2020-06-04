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
    static private int n;

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
        n = uniquePaths.size();

        // calculate all distribution costs
        recDistributionCost(0, new Integer[n], totAgents);

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
        if (current == n) {
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
        return null;
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
