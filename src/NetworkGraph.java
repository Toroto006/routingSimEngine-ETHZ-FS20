public class NetworkGraph {
    protected int numVertices;
    protected final EdgeCosts edgeCosts;

    // Initialize the matrix
    public NetworkGraph(int numVertices) {
        this.numVertices = numVertices;
        edgeCosts = new EdgeCosts();
    }

    // Add edges
    public void addEdge(int i, int j, CostFct c) throws Exception {
        if (i < 0 || j < 0)
            throw new IndexOutOfBoundsException("no negative indices for vertices");
        if (i >= numVertices || j >= numVertices)
            throw new IndexOutOfBoundsException("index for at least one vertex out of bounds");
        edgeCosts.addEdge(i, j, c);
    }

    // Does edge exist
    public boolean existsEdge(int i, int j) {
        return numVertices > 0 && edgeCosts.contains(i, j);
    }

    public EdgeCosts getEdgeCosts(){
        return edgeCosts;
    }

    // Print the matrix
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Following is the current NetworkGraphMatrix:\n");
        for (int i = 0; i < numVertices; i++) {
            s.append(i).append(": ");
            for (int j = 0; j < numVertices; j++) {
                if (i == j) {
                    s.append("0 ");
                    continue;
                }
                if(edgeCosts.contains(i, j))
                    s.append(edgeCosts.getEdgeCost(i, j)).append(" ");
                else
                    s.append("∞ ");
            }
            s.append("\n");
        }
        return s.toString();
    }

    public void addAgent(int i, int j) {
        if (!existsEdge(i, j))
            throw new IndexOutOfBoundsException("The given edge does not exist in this networkGraph!");
        edgeCosts.addAgent(i, j);
    }
}