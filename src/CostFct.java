public interface CostFct {

    /**
     * This is a cost function of an edge
     * @param t the amount of agents on this edge
     * @return the cost if you go over this edge
     */
    public int getCost(int t);
}
