package visualizer;

import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.randomWalk.RandomWalk;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.json.JSONObject;

import java.util.ArrayList;

import static utils.UtilFuntions.readFileAsJSON;

public class SimulationVisualizer {

    private static ArrayList<JSONObject> getSimulationsFromJson(String simulation) {
        String filePath = "./networks/" + simulation + ".json";
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

        graph.addAttribute("ui.stylesheet", styleSheet);
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");
        return graph;
    }

    public static void main(String[] args) {
        System.out.println("Starting SimulationVisualizer!");
        SimulationVisualizer simVis = new SimulationVisualizer();
        //simVis.TestRandomWalk();
        //System.exit(-1);
        ArrayList<JSONObject> sims = getSimulationsFromJson("Simulation_out.json");
        for (JSONObject sim: sims) {
            Graph g = createGraphFromJson(sim);
            g.display();
        }
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
                    "node {"+
                    "	size: 6px;"+
                    "	fill-color: #444;"+
                    "}";
}
