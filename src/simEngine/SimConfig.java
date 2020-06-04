package simEngine;

public class SimConfig {
    private final NetworkGraph networkGraph;
    private final String[] nodes;
    private final String networkTitle;
    private int amountOfAgents;
    private int agentsPerStep = 100;

    public SimConfig(String[] nodes, NetworkGraph networkGraph, int amountOfAgents, String networkTitle) {
        this.networkGraph = networkGraph;
        this.nodes = nodes;
        this.amountOfAgents = amountOfAgents;
        this.networkTitle = networkTitle;
    }

    public SimConfig(String[] nodes, NetworkGraph networkGraph, int amountOfAgents, String networkTitle, int agentsPerStep) {
        this.networkGraph = networkGraph;
        this.nodes = nodes;
        this.amountOfAgents = amountOfAgents;
        this.networkTitle = networkTitle;
        this.agentsPerStep = agentsPerStep;
    }

    public NetworkGraph getNetworkGraph() {
        return this.networkGraph;
    }
    public int getAmountOfAgents() {
        return amountOfAgents;
    }

    public String getNetTitle() {
        return networkTitle;
    }

    public String[] getNodes() {
        return nodes;
    }

    public int getAgentsPerStep() {
        return agentsPerStep;
    }

    public void setAgentsPerStep(int agents){
        this.agentsPerStep = agents;
    }
}
