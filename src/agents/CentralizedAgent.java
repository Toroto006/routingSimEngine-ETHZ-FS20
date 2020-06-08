package agents;

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

    LinkedList<Integer>[] adjList;
    boolean[] visitedDFS;
    boolean[] visitedPath;
    // list of paths (LinkedList) for each node
    ArrayList<ArrayList<LinkedList<Integer>>> pathsToDest;
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
        uniquePaths = getUniquePaths(ncg);
        nrUniquePaths = uniquePaths.size();

        // calculate all distribution costs
        distributionCost = new HashMap<Integer[], Double>();
        minDistVal = Float.MAX_VALUE;
        recDistributionCost(0, new Integer[nrUniquePaths], totAgents);

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
}
