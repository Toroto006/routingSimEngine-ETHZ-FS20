import java.util.LinkedList;

public interface NetworkAgent {

    /**
     * This function will be called for each agent once, and it has to decide what it wants to do
     * @param ncg the adj. matrix of the current latency cost for the network to decide upon
     * @param ec an object to figure out how much an edge costs, given different amounts of agents on it
     * @param decidedAgents the amount of agents, which already decided on a path, i.e. ncg created using ec with decidedAgents
     * @return a list of the path this agent wants to take, including start and destination (in this direction)
     */
    public LinkedList<Integer> agentDecide(NetworkCostGraph ncg, EdgeCosts ec, int decidedAgents);
}
