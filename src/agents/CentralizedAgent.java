package agents;

import simEngine.Edge;
import simEngine.EdgeCosts;
import simEngine.NetworkCostGraph;
import simEngine.SimConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class CentralizedAgent implements NetworkAgent {
    HashMap<Integer, LinkedList<Integer>> solutions;
    private int totalAgents;
    private HashMap<Integer[], Float> distributionCost = new HashMap<>();
    private ArrayList<LinkedList<Integer>> uniquePaths;
    private int n;


    @Override
    public LinkedList<Integer> agentDecide(NetworkCostGraph ncg, EdgeCosts ec, int decidedAgents) {
        // either calculate solution and use it or just use pre-calculated solution
        if (decidedAgents == 0 || solutions.isEmpty()) {
            // need a way to get access to total amount of agents by either passing SimConfig or just number to agentDecide
            totalAgents = getAmountOfAgents();
            solutions = new HashMap<>(totalAgents);
            solutions = completeSolution(ncg, ec);
        }
        return solutions.get(decidedAgents);

        throw new UnsupportedOperationException("Not implemented yet!");
    }

    /**
     * This will calculate the solution for all agents by finding the optimal paths and corresponding loads.
     *
     * @param ncg the adj. matrix of the current latency cost for the network to decide upon
     * @param ec  an object to figure out how much an edge costs, given different amounts of agents on it
     * @return Map of agent nr. to an optimal path. Multiple agents may refer to same path.
     */
    public HashMap<Integer, LinkedList<Integer>> completeSolution(NetworkCostGraph ncg, EdgeCosts ec) {
        uniquePaths = getUniquePaths(ncg);
        n = uniquePaths.size();

        // calculate all distribution costs
        recDistributionCost(0, new Integer[n], totalAgents);

        // find minimal value of all distribution costs
        float min = Integer.MAX_VALUE;
        for (Map.Entry<Integer[], Float> current : distributionCost.entrySet()) {
            min = Math.min(min, current.getValue());
        }

        // save all distributions that create a minimal value in list
        ArrayList<Integer[]> bestDistributions = new ArrayList<>();
        for (Map.Entry<Integer[], Float> current : distributionCost.entrySet()) {
            if (current.getValue() == min) {
                bestDistributions.add(current.getKey());
            }
        }

        // do not know how to handle multiple minimal distributions
        // for now just choosing first one
        Integer[] bestDistribution = bestDistributions.get(0);

        HashMap<Integer, LinkedList<Integer>> solutionsPerAgent = new HashMap<>(totalAgents);
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
     * @param ec an object to figure out how much an edge costs, given different amounts of agents on it
     * @return Cost as a real value.
     */
    public Float calcPathCost(LinkedList<Integer> path, EdgeCosts ec) {
        return null;
    }

    /**
     * Calculates the total cost of a given distribution
     *
     * @param distribution distribution of agents over unique paths.
     * @return Cost to route agents according to distribution over paths
     */
    public Float calcDistributionCost(Integer[] distribution) {
        return null;
    }
}
