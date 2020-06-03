public class Edge {
    private final CostFct costFct;
    private int agents;

    public Edge(CostFct costFct) {
        if (costFct == null)
            throw new NullPointerException("CostFct can not be null!");
        this.costFct = costFct;
        agents = 0;
    }

    public int getCost() {
        return costFct.getCost(agents);
    }

    public void addAgent() {
        agents++;
    }

    public void setAgents(int agents) {
        this.agents = agents;
    }

    public int getAgents() {
        return this.agents;
    }

    public CostFct getCostFct() {
        return costFct;
    }
}
