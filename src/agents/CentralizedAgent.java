package agents;

import simEngine.EdgeCosts;
import simEngine.NetworkCostGraph;
import java.util.LinkedList;

public class CentralizedAgent implements NetworkAgent {
    @Override
    public LinkedList<Integer> agentDecide(NetworkCostGraph ncg, EdgeCosts ec, int decidedAgents, int totalAgents) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
