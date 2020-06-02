import java.util.HashMap;
import java.util.Map;

public class EdgeCosts {
    private Map<String, CostFct> edges;

    public EdgeCosts() {
        edges = new HashMap<>();
    }

    private String createKey(int i, int j) {
        return i + "" + j;
    }

    /**
     * Function to add edges
     * @param i from node
     * @param j to node
     * @param c the CostFct of this edge, given the number of agents on it
     */
    public void addEdge(int i, int j, CostFct c) {
        edges.put(createKey(i, j), c);
    }

    /**
     * Calculates the latency cost of the identified edge given the amount of agents on it
     * @param i from node
     * @param j to node
     * @param t the amount of agents on this edge, to calculate cost
     * @return cost calculated
     */
    public int getEdgeCost(int i, int j, int t) {
        return edges.get(createKey(i, j)).getCost(t);
    }
}
