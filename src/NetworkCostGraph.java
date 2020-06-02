public class NetworkCostGraph {
    private int adjMatrix[][];
    private int numVertices;

    // Initialize the matrix
    public NetworkCostGraph(int numVertices) {
        this.numVertices = numVertices;
        adjMatrix = new int[numVertices][numVertices];
    }

    // Add edges
    public void addEdge(int i, int j, int w) {
        adjMatrix[i][j] = w;
        adjMatrix[j][i] = w;
    }

    public int getLatencyCost(int i, int j) {
        return adjMatrix[i][j];
    }

    // Print the matrix
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < numVertices; i++) {
            s.append(i + ": ");
            for (int j : adjMatrix[i]) {
                s.append(j + " ");
            }
            s.append("\n");
        }
        return s.toString();
    }
}