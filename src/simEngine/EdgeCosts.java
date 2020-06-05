package simEngine;

import java.util.HashMap;
import java.util.Map;

public class EdgeCosts {
    private final Map<String, Edge> edges;

    public EdgeCosts() {
        edges = new HashMap<>();
    }

    private String createKey(int i, int j) {
        if (i < j)
            return i + " " + j + " ->";
        return j + " " + i + " <-";
    }

    /**
     * Function to add edges
     * @param i from node
     * @param j to node
     * @param c the simEngine.CostFct of this edge, given the number of agents on it
     */
    public void addEdge(int i, int j, CostFct c) throws Exception {
        if (i == j)
            throw new Exception("In this graph there can not exist edges to itself!");
        String key = createKey(i, j);
        if (edges.containsKey(key))
            throw new Exception("The edge (" + i +", " + j + ") was already added!");
        
        edges.put(key, new Edge(c, i < j));
    }

    /**
     * Calculates the latency cost of the identified edge calculate using the amount of agents on it
     * @param i from node
     * @param j to node
     * @return cost calculated
     */
    public Float getEdgeCost(int i, int j) {
        return edges.get(createKey(i, j)).getCost();
    }

    public Float getDerivativeEdgeCost(int i, int j) {
        return edges.get(createKey(i, j)).getDerivativeCost();
    }

    public Float getEdgeCostCustomAgents(int i, int j, int t) {
        return edges.get(createKey(i, j)).getCostAgents(t);
    }

    public boolean contains(int i, int j) {
        return edges.containsKey(createKey(i, j));
    }

    public void copy(EdgeCosts edgeCosts) {
        for (Map.Entry<String, Edge> entry : edgeCosts.edges.entrySet()) {
            this.edges.put(entry.getKey(), entry.getValue().copy());
        }
    }

    public void addAgent(int i, int j) {
        edges.get(createKey(i, j)).addAgent();
    }

    public Map<String, Edge> getEdges() {
        return edges;
    }
}
