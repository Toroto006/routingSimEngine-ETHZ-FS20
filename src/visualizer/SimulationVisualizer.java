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
        System.out.println("Finished creating graph from " + simulation + "!");
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
        for (Object e: simulation.getJSONArray("edges")) {
            if (! (e instanceof JSONObject)) {
                System.out.println("The read simultaion json has the wrong format!");
                System.exit(-2);
            }
            JSONObject edge = (JSONObject) e;
            JSONArray conn = edge.getJSONArray("connection");
            String edgeID = conn.getString(0)+conn.getString(1);
            graph.addEdge(edgeID, conn.getString(0), conn.getString(1), true);
            JSONObject use = edge.getJSONObject("usage");
            graph.getEdge(edgeID).addAttribute("use", use);

            //for(JSONObject net: use)


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
        graph.getNode((String) nodes.get(nodes.size()-1)).addAttribute("layout.label", "End");

        return graph;
    }

    public static void main(String[] args) {
        System.out.println("Starting SimulationVisualizer!");
        //SimulationVisualizer simVis = new SimulationVisualizer();
        //simVis.TestRandomWalk();
        //System.exit(-1);
        ArrayList<JSONObject> sims = getSimulationsFromJson("Simulation_out");
        for (JSONObject sim: sims) {
            Graph g = createGraphFromJson(sim);
            Viewer v = g.display(true);


            testDrawing(g);

        }
    }

    private static void testDrawing(Graph g) {
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
        System.out.println(n1.getId() + " and " + n2.getId());

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

    public void TestRandomWalk() {
        //http://graphstream-project.org/doc/Algorithms/Random-walks-on-graphs/
        Graph graph = new MultiGraph("random walk");
        Generator gen   = new DorogovtsevMendesGenerator();
        RandomWalk rwalk = new RandomWalk();

        // We generate a 400 nodes Dorogovstev-Mendes graph.
        gen.addSink(graph);
        gen.begin();
        for(int i=0; i<400; i++) {
            gen.nextEvents();
        }
        gen.end();

        // We display the graph.
        graph.addAttribute("ui.stylesheet", styleSheet);
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");
        graph.display();

        // We configure the random walk to use twice as
        // much entities as nodes in the graph. To use
        // a small evaporation on the number of passes
        // per element and a last visited edge list of
        // 40 elements.
        rwalk.setEntityCount(graph.getNodeCount()*2);
        rwalk.setEvaporation(0.97);
        rwalk.setEntityMemory(40);
        rwalk.init(graph);

        // Compute the walks for 3000 steps only as an
        // example, but the test could run forever with
        // a dynamic graph if needed.
        for(int i=0; i<10000; i++) {
            rwalk.compute();
            if (i % 100 == 0)
                updateGraph(graph, rwalk);
        }
        rwalk.terminate();

        // Only when finished we change the edges colors
        // according to the number of passes. This call could
        // be made inside the loop above to show the evolution
        // of the entities passes.

        // We take a small screen-shot of the result.
        //graph.addAttribute("ui.screenshot", "randomWalk.png");
    }

    /**
     * Update the edges with colors corresponding to entities passes.
     */
    public void updateGraph(Graph graph, RandomWalk rwalk) {
        double mine = Double.MAX_VALUE;
        double maxe = Double.MIN_VALUE;

        // Obtain the maximum and minimum passes values.
        for(Edge edge: graph.getEachEdge()) {
            double passes = rwalk.getPasses(edge);
            if(passes>maxe) maxe = passes;
            if(passes<mine) mine = passes;
        }

        // Set the colors.
        for(Edge edge:graph.getEachEdge()) {
            double passes = rwalk.getPasses(edge);
            double color  = ((passes-mine)/(maxe-mine));
            edge.setAttribute("ui.color", color);
        }
    }

    protected static String styleSheet =
            "edge {"+
            "	size: 3px;"+
            "	fill-color: red, yellow, green, gray;"+
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
