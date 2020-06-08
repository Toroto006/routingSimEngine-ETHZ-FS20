package visualizer;

import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.randomWalk.RandomWalk;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerPipe;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.graphstream.algorithm.Toolkit.randomNode;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.nodePosition;
import static utils.UtilFuntions.readFileAsJSON;

public class SimulationVisualizer {

    private static ArrayList<JSONObject> getSimulationsFromJson(String simulation) {
        String filePath = "./networks/finishedRuns/" + simulation + ".json";
        JSONObject simJson = null;
        try {
            simJson = readFileAsJSON(filePath);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not read " + filePath + " so exiting!");
            System.exit(-1);
        }
        ArrayList<JSONObject> ret = new ArrayList<>();
        for (String key: simJson.keySet())
            ret.add(simJson.getJSONObject(key));
        return ret;
    }

    private static Graph createGraphFromJson(JSONObject simulation) {
        Graph graph = new MultiGraph(simulation.getString("networkTitle"));
        //TODO do for all/told agents
        System.out.println(simulation.keySet());
        List<Object> nodes = simulation.getJSONArray("nodes").toList();
        for (Object e: nodes) {
            if (! (e instanceof String)) {
                System.out.println("The read simultaion json has the wrong format!");
                System.exit(-2);
            }
            String nodeID = (String) e;
            graph.addNode(nodeID);
        }
        System.out.println("Finished creating graph from " + simulation.getString("networkTitle") + "!");
        boolean usageAmountSet = false;
        for (Object e: simulation.getJSONArray("edges")) {
            if (! (e instanceof JSONObject)) {
                System.out.println("The read simultaion json has the wrong format!");
                System.exit(-2);
            }
            JSONObject edge = (JSONObject) e;
            //System.out.println(edge.keySet());

            JSONArray conn = edge.getJSONArray("connection");
            String edgeID = conn.getString(0)+conn.getString(1);
            graph.addEdge(edgeID, conn.getString(0), conn.getString(1), true);
            JSONObject use = edge.getJSONObject("usage");
            graph.getEdge(edgeID).addAttribute("usage", use);
            graph.getEdge(edgeID).addAttribute("ui.label", edge.getString("cost"));
            if (!usageAmountSet) {
                graph.addAttribute("usageAmount", use.getJSONArray(use.keySet().iterator().next()).toList().size());
                usageAmountSet = true;
            }
            //System.out.println(use.keySet());
        }
        graph.addAttribute("ui.stylesheet", styleSheet);
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");
        //Set start & endNodes
        graph.getNode((String) nodes.get(0)).addAttribute("layout.frozen");
        graph.getNode((String) nodes.get(nodes.size()-1)).addAttribute("layout.frozen");
        graph.getNode((String) nodes.get(0)).setAttribute("xy", 0, 0);
        graph.getNode((String) nodes.get(nodes.size()-1)).setAttribute("xy", 1, 0);
        graph.getNode((String) nodes.get(0)).addAttribute("ui.label", "Start");
        graph.getNode((String) nodes.get(nodes.size()-1)).addAttribute("ui.label", "End");

        return graph;
    }

    public static void main(String[] args) {
        System.out.println("Starting SimulationVisualizer!");
        ArrayList<JSONObject> sims = getSimulationsFromJson("Simulation_out");
        System.out.println("Read simulations from json successfully!");
        ArrayList<runAnimations> graphs = new ArrayList<>();
        for (JSONObject sim: sims) {
            Graph g = createGraphFromJson(sim);
            List<String> agents = new LinkedList<>(((JSONObject) g.getEdge(0).getAttribute("usage")).keySet());
            graphs.add(new runAnimations(g, sim.getInt("amountOfAgents"), agents, 5000));
            //Viewer v = g.display(true);
            //testDrawing(v, g);
        }
        for (Thread g: graphs)
            g.start();
        for (Thread g: graphs) {
            try {
                g.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void testDrawing(Viewer v, Graph g) {
        g.addAttribute("ui.agent", "agent { \tshape: box; \tsize: 100px, 100px; \tfill-mode: plain; \tfill-color: red;}");

        ViewerPipe pipe = v.newViewerPipe();
        pipe.addAttributeSink(g);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        pipe.pump();
        SpriteManager sman = new SpriteManager(g);
        Sprite s1 = sman.addSprite("S1");
        Sprite s2 = sman.addSprite("S2");
        s1.addAttribute("ui.agent", "agent");
        s2.addAttribute("ui.agent", "agent");
        Node n1 = randomNode(g);
        Node n2 = randomNode(g);
        //System.out.println(n1.getId() + " and " + n2.getId());

        for(int i = 0; i < 2000; i++) {
            pipe.pump();
            double p1[] = nodePosition(n1);
            double p2[] = nodePosition(n2);
            s1.setPosition(p1[0], p1[1], p1[2]);
            s2.setPosition(p2[0], p2[1], p2[2]);
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("done");
    }

    protected static String styleSheet =
            "edge {"+
            "	size: 3px;"+
            "	fill-color: green, orange, red;"+
            "	fill-mode: dyn-plain;"+
            "}"+
            "node.start {"+
            "	size: 12px;"+
            "	fill-color: green;"+
            "}"+
            "node.end {"+
            "	size: 12px;"+
            "	fill-color: blue;"+
            "}"+
            "node {"+
            "	size: 7px;"+
            "	fill-color: #444;"+
            "}";
}
