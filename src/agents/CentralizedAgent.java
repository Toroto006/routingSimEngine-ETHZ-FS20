package agents;

import scala.Int;
import simEngine.EdgeCosts;
import simEngine.NetworkCostGraph;

import java.util.*;

public class CentralizedAgent implements NetworkAgent {
    static HashMap<Integer, LinkedList<Integer>> solutions;
    static int totAgents;
    static EdgeCosts ec;
    HashMap<Integer[], Double> distributionCost = new HashMap<>(1000);
    double minDistVal = Double.MAX_VALUE;
    int[] minDistCombination;
    static ArrayList<LinkedList<Integer>> uniquePaths;
    static int nrUniquePaths;
    static String name = "CentralizedAgent";

    int numVertices;

    @Override
    public String getName() {
        return this.name;
    }

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
        uniquePaths = AgentUtils.getUniquePaths(ncg);
        nrUniquePaths = uniquePaths.size();


        long startTime = System.nanoTime();


        // calculate all distribution costs
        distributionCost = new HashMap<Integer[], Double>();
        minDistVal = Float.MAX_VALUE;
        recDistributionCost(0, new Integer[nrUniquePaths], totAgents);

        // the code you want to measure time for goes here
        long endTime = System.nanoTime();
        long elapsedNs = endTime - startTime;
        double elapsedS = elapsedNs / 1.0e9;
        System.out.println("It took " + elapsedS + " seconds to calculate.");

        /*// find minimal value of all distribution costs
        double min = Integer.MAX_VALUE;
        for (Map.Entry<Integer[], Double> current : distributionCost.entrySet()) {
            min = Math.min(min, current.getValue());
        }*/

        // save all distributions that create a minimal value in list
        ArrayList<Integer[]> bestDistributions = new ArrayList<>();
        for (Map.Entry<Integer[], Double> current : distributionCost.entrySet()) {
            if (current.getValue() == minDistVal) {
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
     * Recursive function to calculate all distributions of agents over unique paths and save cost of each
     * distribution-path-combination in DP table.
     *
     * @param current current path being assigned agents
     * @param distribution distribution of agents per path
     * @param leftToDistribute agents left to distribute
     */
    public void recDistributionCost(int current, Integer[] distribution, int leftToDistribute) {
        // last path gets all remaining agents
        // calculate how much distribution cost and add to set of all distribution costs
        if (current == nrUniquePaths-1) {
            distribution[current] = leftToDistribute;
            double currentDistVal = calcDistributionCost(distribution);
            //double currentDistVal = totalLatency(distribution);
            // new minimal distribution found
            if (currentDistVal < minDistVal) {
                distributionCost.clear();
                Integer[] copy = new Integer[nrUniquePaths];
                for (int i = 0; i < nrUniquePaths; i++) {
                    copy[i] = distribution[i];
                }
                distributionCost.put(copy, currentDistVal);
                minDistVal = currentDistVal;
                //minDistCombination = distribution;
            }
            // another minimal distribution found
            if (currentDistVal == minDistVal) {
                Integer[] copy = new Integer[nrUniquePaths];
                for (int i = 0; i < nrUniquePaths; i++) {
                    copy[i] = distribution[i];
                }
                distributionCost.put(copy, currentDistVal);
            }
            // else forget this distribution
            return;
        }
        // assign range of all left agents to current path
        for (int i = 0; i < leftToDistribute; i++) {
            distribution[current] = i;
            // go to next path, there are i less agents to distribute
            recDistributionCost(current+1, distribution, leftToDistribute-i);
        }
    }


    /**
     * Calculate cost of a given path.
     *
     * @param path path as a list of nodes
     * @return Cost as a real value.
     */
    public double calcPathCost(LinkedList<Integer> path, Integer[] distribution) {
        double pathCost = 0;
        // empty path or only 1 node
        if (path.isEmpty() || path.size() < 2) {
            return 0.0;
        }

        // path of at least length 1

        Iterator<Integer> it = path.listIterator();

        int current = -1;
        int next = it.next();
        while (it.hasNext()) {
            current = next;
            next = it.next();
            int nrAgentsOnEdge = 0;
            // check for all unique paths if they have agents on edge defined by current-next
            for(int i = 0; i < uniquePaths.size(); i++) {
                Iterator<Integer> it2 = uniquePaths.get(i).listIterator();
                if (path.isEmpty() || path.size() < 2) {
                    break;
                }
                int node1 = -1;
                int node2 = it2.next();
                while (it2.hasNext()) {
                    node1 = node2;
                    node2 = it2.next();
                    if (node1 == current && node2 == next) {
                        nrAgentsOnEdge += distribution[i];
                        break;
                    }
                }
            }
            // calculate path cost for current edge
            pathCost += ec.getEdgeCostCustomAgents(current, next, nrAgentsOnEdge);
        }
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
            distributionSum += distribution[i] * calcPathCost(uniquePaths.get(i), distribution);
        }
        return (distributionSum/totAgents);
    }

    /**
     * Calculates total latency of a distribution
     *
     * @param distribution a distribution of agents over paths
     * @return total latency
     */
    public Double totalLatency(Integer[] distribution) {
        int[][] agents = new int[numVertices][numVertices];
        double[][] latency = new double[numVertices][numVertices];

        int i = 0;
        for (LinkedList<Integer> path :
             uniquePaths) {
            Iterator<Integer> it = uniquePaths.get(i).listIterator();
            if (path.isEmpty() || path.size() < 2) {
                break;
            }
            int node1 = -1;
            int node2 = it.next();
            while (it.hasNext()) {
                node1 = node2;
                node2 = it.next();
                agents[node1][node2] += distribution[i];
            }
            i++;
        }

        double totalLatency = 0;
        for (int j = 0; j < numVertices; j++) {
            for (int k = 0; k < numVertices; k++) {
                if (ec.contains(j,k)) {
                    latency[j][k] = ec.getEdgeCostCustomAgents(j,k,agents[j][k]);
                    totalLatency += latency[j][k] * agents[j][k];
                }
            }
        }
        //need to get to proportional cost
        return totalLatency/totAgents;
    }
}
