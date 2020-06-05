package simEngine;

public class Edge {
    private final CostFct costFct;
    private int agents;
    private boolean direction; //true from i -> j, false j -> i

    public Edge(CostFct costFct, boolean direction) {
        if (costFct == null)
            throw new NullPointerException("CostFct can not be null!");
        this.costFct = costFct;
        agents = 1;
        setDirection(direction);
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

    public void setDirection(boolean direction){
        this.direction = direction;
    }

    public boolean getDirection(){
        return this.direction;
    }

    public CostFct getCostFct() {
        return costFct;
    }

    public Float getCostAgents(int t) {
        return costFct.getCost(t);
    }

    public Float getDerivativeCost() {
        return costFct.getDerivativeCost();
    }

    public Edge copy() {
        return new Edge(costFct, direction);
    }
}
