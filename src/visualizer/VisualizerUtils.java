package visualizer;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.json.JSONArray;
import org.json.JSONObject;
import simEngine.CostFct;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static utils.UtilsFuntions.parseLinearFct;
import static utils.UtilsFuntions.readFileAsJSON;

public class VisualizerUtils {

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

    protected static ArrayList<JSONObject> getSimulationsFromJson(String simulation) {
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

    protected static Graph createGraphFromJson(JSONObject simulation) {
        Graph graph = new MultiGraph(simulation.getString("networkTitle"));
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
            //Work with edge and create the graph to display
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
            CostFct c = parseLinearFct(edge.getString("cost"));
            graph.getEdge(edgeID).addAttribute("costFct", c);
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
}
