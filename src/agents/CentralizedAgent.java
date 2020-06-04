package agents;

import simEngine.Edge;
import simEngine.EdgeCosts;
import simEngine.NetworkCostGraph;
import simEngine.SimConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class CentralizedAgent implements NetworkAgent {
    private HashMap<Integer[], Float> distributionCost = new HashMap<Integer[], Float>();
    private ArrayList<LinkedList<Integer>> uniquePaths;
    private int n;


    @Override
    public LinkedList<Integer> agentDecide(NetworkCostGraph ncg, EdgeCosts ec, int decidedAgents) {
        // need a way to get access to total amount of agents by either passing SimConfig or just number to agentDecide
        int a = getAmountOfAgents();
        HashMap<Integer, LinkedList<Integer>> solutions = new HashMap<>(a);
        // either calculate solution and use it or just use pre-calculated solution
        if (decidedAgents == 0 || solutions.isEmpty()) {
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



        return null;
    }

    /**
     * Recursive function to calculate all distributions over unique paths and save cost of each
     * distribution-path-combination in DP table.
     */
    public void recDistributionCost(int current, Integer[] distribution, int leftToDistribute) {
        if (current == n) {
            distribution[n] = leftToDistribute;
            distributionCost.put(distribution, )
            return
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
     * @param ec   an object to figure out how much an edge costs, given different amounts of agents on it
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

    }
}
