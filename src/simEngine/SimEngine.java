package simEngine;

import agents.CentralizedAgent;
import agents.NetworkAgent;
import agents.SelfishRoutingAgent;
import agents.TaxedSelfishRoutingAgent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.util.*;

import static utils.UtilsFuntions.parseLinearFct;
import static utils.UtilsFuntions.readFileAsJSON;

public class SimEngine {

    /**
     * import network json, every vertex except dest has to hav e one outgoing edge
     *
     * @param simulation
     */
    private static SimConfig importSimulationConfiguration(String simulation) throws Exception {
        // json in java https://www.tutorialspoint.com/json/json_java_example.htm
        // we give the nodes numbers in lexicographic order for internal purpose
        // e.g. a graph with A B C as nodes will internally be a adj. matrix of size 3, where 0 is A
        String filePath = "./networks/inputGraphs/" + simulation + ".json";
        JSONObject jsonObject = readFileAsJSON(filePath);
        // From now do convertion to config
        String[] nodes = jsonObject.getJSONArray("nodes").toList().toArray(new String[0]);
        int vertices = nodes.length;
        HashMap<String, Integer> nodesMapped = createNodeMappingSI(nodes);
        NetworkGraph networkGraph = new NetworkGraph(vertices);
        // Now read the edges
        for (Object o : jsonObject.getJSONArray("edges")) {
            if (!(o instanceof JSONObject))
                throw new Exception("Decoding the json was not successful, edges are wrong/not given!");
            JSONObject edge = (JSONObject) o;
            String costFctStr = edge.getString("cost");
            CostFct c = parseLinearFct(costFctStr);
            JSONArray connections = edge.getJSONArray("connection");
            int i = nodesMapped.get(connections.get(0));
            int j = nodesMapped.get(connections.get(1));

            // System.out.println("From " + i + " to " + j + " and c: " + c);
            networkGraph.addEdge(i, j, c);
        }
        // Read the rest of the config
        int amountOfAgents = jsonObject.getInt("amountOfAgents");
        int agentsPerStep = 0;
        try {
            agentsPerStep = jsonObject.getInt("agentsPerStep");
        } catch (JSONException e) {
        }
        if (agentsPerStep == 0)
            agentsPerStep = amountOfAgents;
        String networkTitle = jsonObject.getString("networkTitle");
        return new SimConfig(nodes, networkGraph, amountOfAgents, networkTitle, agentsPerStep);
    }

    private static HashMap<Integer, String> createNodeMappingIS(String[] nodes) {
        Arrays.sort(nodes);
        HashMap<Integer, String> ret = new HashMap<>();
        int i = 0;
        for (String s : nodes) {
            ret.put(i, s);
            i++;
        }
        return ret;
    }

    private static HashMap<String, Integer> createNodeMappingSI(String[] nodes) {
        Arrays.sort(nodes);
        HashMap<String, Integer> ret = new HashMap<>();
        int i = 0;
        for (String s : nodes) {
            ret.put(s, i);
            i++;
        }
        return ret;
    }

    /**
     * This will initialise the JSONObject of one Simulation
     *
     * @param simConfig
     * @param out
     */
    private static void initialiseJSON(SimConfig simConfig, JSONObject out) throws Exception {

        out.put("networkTitle", simConfig.getNetTitle());
        out.put("amountOfAgents", simConfig.getAmountOfAgents());

        String[] nodes = simConfig.getNodes();
        JSONArray jsNodes = new JSONArray(nodes);
        out.put("nodes", (Object) jsNodes);

        Map<String, Edge> unsortedEdges = simConfig.getNetworkGraph().getEdges();
        Map<String, Edge> sortedEdges = new TreeMap<String, Edge>(unsortedEdges);
        int i = 0;
        JSONArray jsEdges = new JSONArray();
        for (Map.Entry<String, Edge> entry : sortedEdges.entrySet()) {
            String key = entry.getKey();
            Integer[] iNodes = Arrays.stream(key.split(" ")).map(s -> {
                if (s.contains("-"))
                    return 0;
                return Integer.parseInt(s);
            }).toArray(Integer[]::new);
            Edge edge = entry.getValue();

            JSONObject jsTemp = new JSONObject();

            String[] c;
            if (edge.getDirection()) {
                c = new String[]{nodes[iNodes[0]], nodes[iNodes[1]]};
            } else {
                c = new String[]{nodes[iNodes[1]], nodes[iNodes[0]]};
            }

            JSONArray connection = new JSONArray(c);
            jsTemp.put("connection", connection);
            jsTemp.put("cost", edge.getCostFct().toString());
            jsTemp.put("usage", new JSONObject());


            jsEdges.put(jsTemp);
        }
        out.put("edges", jsEdges);
    }

    /**
     * This will export the simulation, s.t. the visualizer can use it
     *
     * @param exportName
     * @param export
     */
    private static void exportSimulationsToFile(String exportName, JSONObject export) throws Exception {
        String filePath = "./networks/finishedRuns/" + exportName + "_out.json";
        FileWriter file;
        file = new FileWriter(filePath);
        try {
            file.write(export.toString());
        } catch (Exception e) {
            throw new Exception("Couldn't write to/create the following file: " + filePath);
        } finally {
            try {
                file.flush();
                file.close();
            } catch (Exception e) {
                throw new Exception("Couldn't complete the export to the following file: " + filePath);
            }
        }
        System.out.println("Simulation \"" + exportName + "\" is finished and saved");

    }

    /**
     * This runs the whole simulation for one agent
     *
     * @param simConfig the configuration of the simulation
     * @param agent     the agent to use for this simulation
     * @return
     */
    private static NetworkCostGraph runSimulation(final SimConfig simConfig, NetworkAgent agent, JSONObject export) {
        int totalEntries = simConfig.getAmountOfAgents() / simConfig.getAgentsPerStep() + 1;
        int[][] outEdges = new int[simConfig.getNetworkGraph().getEdges().size()][totalEntries];
        int j = 1;
        NetworkCostGraph networkCostGraph = new NetworkCostGraph(simConfig.getNetworkGraph());
        networkCostGraph.calculateAllCosts();
        //System.out.println("Start costMatrix:\n" + networkCostGraph.toString());
        for (int doneAgents = 0; doneAgents < simConfig.getAmountOfAgents(); doneAgents++) {

            // Run one agent
            LinkedList<Integer> agentPath = agent.agentDecide(networkCostGraph, networkCostGraph.getEdgeCosts(),
                    doneAgents, simConfig.getAmountOfAgents());
            if (agentPath == null) {
                System.err.println(agent.getClass().getSimpleName() + " returned a null path!");
            }
            for (int i = 0; i < agentPath.size() - 1; i++) {
                // Add the cost of this agent to the network
                networkCostGraph.addAgent(agentPath.get(i), agentPath.get(i + 1));
            }
            networkCostGraph.calculateAllCosts();

            if ((doneAgents + 1) % simConfig.getAgentsPerStep() == 0) {
                Map<String, Edge> mapEdges = new TreeMap<String, Edge>(networkCostGraph.getEdges());
                int i = 0;
                for (Edge e : mapEdges.values())
                    outEdges[i++][j] = e.getAgents();
                j++;
            }


        }

        JSONArray arrEdges = export.getJSONArray("edges");
        for (int i = 0; i < outEdges.length; i++) {
            JSONObject obj = (JSONObject) arrEdges.get(i);
            JSONArray path = new JSONArray(outEdges[i]);

            obj.getJSONObject("usage").put(agent.getName(), path);
        }


        // System.out.println(doneAgents + " done and current costMatrix:\n" +
        // networkCostGraph.toString());
        return networkCostGraph;
    }

    private static void runSimulationForAgent(NetworkAgent networkAgent, SimConfig simConfig, JSONObject export) {
        String agentName = networkAgent.getClass().getSimpleName();
        System.out.println("Starting the simulation of " + agentName + "!");
        runSimulation(simConfig, networkAgent, export);
        System.out.println("Finished the simulation of " + agentName + "!");
    }

    public static void main(String[] args) throws Exception {
        String SimulationName = "Simulation";
        //String[] networks = {"BraessParadoxFast1", "BraessParadoxSlow1", "Pigou", "BraessParadoxFast2"};
        //String[] networks = {"BraessParadoxFast1", "BraessParadoxSlow1-original", "Pigou"};
        String[] networks = {"BraessParadoxSlow1-original"};
        NetworkAgent[] agents = {
                //new SelfishRoutingAgent(),
                //new TaxedSelfishRoutingAgent(),
                //new TaxedSelfishRoutingAgent(new LinearFct(2f, 1f), "TaxedClassSelfishRoutingAgent"),
                new CentralizedAgent()
        };
        System.out.println("GameTheory simEngine.simEngine started!\n");

        JSONObject finalExport = new JSONObject();

        for (String network : networks) {

            SimConfig simConfig = null;
            try {
                simConfig = importSimulationConfiguration(network);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Loading of " + network + " config was not successful, exiting!");
                System.exit(-1);
            }
            JSONObject currentNetwork = new JSONObject();
            initialiseJSON(simConfig, currentNetwork);

            System.out.println(
                    "Loading of " + network + " config successful, running '" + simConfig.getNetTitle() + "'!");

            for (NetworkAgent agent : agents)
                runSimulationForAgent(agent, simConfig, currentNetwork);

            System.out.println("Finished all simulations of '" + simConfig.getNetTitle() + "'!\n");

            finalExport.put(network, currentNetwork);
        }

        exportSimulationsToFile(SimulationName, finalExport);
        System.out.println("GameTheory simEngine.simEngine finished, exiting!");
    }
}
