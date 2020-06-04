package simEngine;

public class Edge {
    private final CostFct costFct;
    private int agents;

    public Edge(CostFct costFct) {
        if (costFct == null)
            throw new NullPointerException("simEngine.CostFct can not be null!");
        this.costFct = costFct;
        agents = 1;
    }

    public Float getCost() {
        return costFct.getCost(agents);
    }

    public void addAgent() {
        agents++;
    }

    public void setAgents(int agents) {
        this.agents = agents;
    }

    public int getAgents() {
        return this.agents-1;
    }

    public CostFct getCostFct() {
        return costFct;
    }

    public Float getCost(int t) {
        return costFct.getCost(t);
    }
}
