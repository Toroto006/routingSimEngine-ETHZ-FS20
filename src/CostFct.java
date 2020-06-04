public interface CostFct {

    /**
     * This is a cost function of an edge, if t is 0 still return useful values!
     * @param t the amount of agents on this edge
     * @return the cost if you go over this edge
     */
    public Float getCost(int t);

}
