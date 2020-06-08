package visualizer;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.json.JSONObject;

import java.util.List;

public class runAnimations extends Thread {
    private final int amountOfAgents;
    private final Graph g;
    private final int usageAmount;
    private final List<String> agents;
    private int totalAnimationTime;

    public runAnimations(Graph g, int amountOfAgents, List<String> agents) {
        this.amountOfAgents = amountOfAgents;
        this.usageAmount = g.getAttribute("usageAmount");
        this.g = g;
        this.agents = agents;
        totalAnimationTime = 3000;
    }

    public runAnimations(Graph g, int amountOfAgents, List<String> agents, int totalAnimationTime) {
        this(g, amountOfAgents, agents);
        this.totalAnimationTime = totalAnimationTime;
    }

    @Override
    public void run() {
        g.display();
        try {
            Thread.sleep(500);
            while (true) {
                for (String agent: agents) {
                    for (int i = 0; i < usageAmount; i++) {
                        updateAnimationAgent(g, agent, i);
                        Thread.sleep(totalAnimationTime/usageAmount);
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Update the edges with colors corresponding to entities passes.
     */
    public void updateAnimationAgent(Graph graph, String agentName, int iteration) {
        for(Edge edge:graph.getEachEdge()) {
            List<Object> usage = ((JSONObject) edge.getAttribute("usage")).getJSONArray(agentName).toList();
            double color  = ((Integer)usage.get(iteration))*1.0/amountOfAgents;
            edge.setAttribute("ui.color", color);
        }
    }
}
