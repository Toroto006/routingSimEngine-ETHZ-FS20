public class SimConfig {
    private final NetworkGraph networkGraph;
    private boolean loaded;
    private int amountOfAgents;

    public SimConfig(NetworkGraph networkGraph){
        this.networkGraph = networkGraph;
    }
    
    public NetworkGraph getNetworkGraph() {
        return this.networkGraph;
    }

    public boolean loadedSuccessful() {
        return loaded;
    }

    public int getAmountOfAgents() {
        return amountOfAgents;
    }
}
