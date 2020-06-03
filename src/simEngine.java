import org.json.*;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class simEngine {
    //Read file content into string with - Files.readAllBytes(Path path)
    //https://howtodoinjava.com/java/io/java-read-file-to-string-examples/
    private static String readAllBytesJava7(String filePath) throws IOException
    {
        String content = "";
        content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        return content;
    }

    /**
     * import network json, every vertex except dest has to hav e one outgoing edge
     * @param simulation
     */
    private static SimConfig importSimulationConfiguration(String simulation) throws Exception {
        //json in java https://www.tutorialspoint.com/json/json_java_example.htm
        //TODO implement the import and konversion of the json network and
        //we give the nodes numbers in lexicographic order for internal purpose
        //e.g. a graph with A B C as nodes will internally be a adj. matrix of size 3, where 0 is A
        String filePath = "./networks/"+simulation+".json";
        String jsonConfigString = null;
        try {
            jsonConfigString = readAllBytesJava7(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Loading the json file was not successful!");
        }
        JSONObject jsonObject = new JSONObject(jsonConfigString);
        //From now do convertion to config
        String[] nodes = jsonObject.getJSONArray("nodes").toList().toArray(new String[0]);
        int vertices = nodes.length;
        HashMap<String, Integer> nodesMapped = createNodeMappingSI(nodes);
        NetworkGraph networkGraph = new NetworkGraph(vertices);
        //Now read the edges
        Pattern pattern = Pattern.compile("(\\d*\\.?\\d*)t\\+(\\d*\\.?\\d*)", Pattern.MULTILINE);
        for(Object o : jsonObject.getJSONArray("edges")) {
            if (!(o instanceof JSONObject))
                throw new Exception("Decoding the json was not successful, edges are wrong/not given!");
            JSONObject edge = (JSONObject)o;
            int i = 0, j = 0;
            CostFct c = null;
            if (edge.keySet().size() != 2)
                throw new Exception("At least one edge has not the correct amount of keys!");
            for (Object k: edge.keySet() ) {
                if (!(k instanceof String))
                    throw new Exception("Decoding the json was not successful, edges do not have string keys!");
                if (k.equals("cost")){
                    String costFctStr = edge.getString((String)k);
                    Matcher m = pattern.matcher(costFctStr);
                    m.find();
                    try {
                        Float a = Float.valueOf(m.group(1));
                        Float b = Float.valueOf(m.group(2));
                        c = new LinearFct(a, b);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new Exception("Something went wrong while decoding the linear function!");
                    }
                } else {
                    //We have the name of a vertex
                    i = nodesMapped.get(k);
                    j = nodesMapped.get(edge.getString((String)k));
                }
            }
            //System.out.println("From " + i + " to " + j + " and c: " + c);
            networkGraph.addEdge(i, j, c);
        }
        //Read the rest of the config
        int amountOfAgents = jsonObject.getInt("amountOfAgents");
        String networkTitle = jsonObject.getString("networkTitle");
        return new SimConfig(nodes, networkGraph, amountOfAgents, networkTitle);
    }

    private static HashMap<Integer, String> createNodeMappingIS(String[] nodes) {
        Arrays.sort(nodes);
        HashMap<Integer, String> ret = new HashMap<>();
        int i = 0;
        for (String s: nodes) {
            ret.put(i, s);
            i++;
        }
        return ret;
    }

    private static HashMap<String, Integer> createNodeMappingSI(String[] nodes) {
        Arrays.sort(nodes);
        HashMap<String, Integer> ret = new HashMap<>();
        int i = 0;
        for (String s: nodes) {
            ret.put(s, i);
            i++;
        }
        return ret;
    }

    /**
     * This will export the simulation, s.t. the visualizer can use it
     */
    private static void exportSimulation(String simulationName, SimConfig simConfig, Map<String, NetworkCostGraph> ncgDones) throws Exception {
        for (String agentName: ncgDones.keySet()) {
            //json in java https://www.tutorialspoint.com/json/json_java_example.htm
            String exportName = simulationName + "_" + agentName;
            NetworkCostGraph ncgDone = ncgDones.get(agentName);
            //Actual export
            JSONObject export = new JSONObject();

            String[] nodes = simConfig.getNodes();
            JSONArray jsNodes = new JSONArray(nodes);
            export.put("nodes", (Object) jsNodes);

            JSONArray jsEdges = new JSONArray();
            Map<String, Edge> mapEdges = ncgDone.getEdges();
            for (Map.Entry<String, Edge> entry : mapEdges.entrySet()) {
                String key = entry.getKey();
                Integer[] iNodes = Arrays.stream(key.split(" ", 2)).map(o -> Integer.parseInt(o)).toArray(Integer[]::new);
                Edge edge = entry.getValue();

                JSONObject jsTemp = new JSONObject();
                jsTemp.put("cost", edge.getCostFct().toString());
                jsTemp.put(nodes[iNodes[0]], nodes[iNodes[1]]);
                jsTemp.put("usage", edge.getAgents());
                jsEdges.put(jsTemp);
            }
            export.put("edges", jsEdges);

            String filePath = "./networks/finishedRuns/" + exportName + ".json";
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
        }
    }

    /**
     * This runs the whole simulation for one agent
     * @param simConfig the configuration of the simulation
     * @param agent the agent to use for this simulation
     * @return
     */
    private static NetworkCostGraph runSimulation(final SimConfig simConfig, NetworkAgent agent){
        NetworkCostGraph networkCostGraph = new NetworkCostGraph(simConfig.getNetworkGraph());
        networkCostGraph.calculateAllCosts();
        System.out.println("Start costMatrix:\n" + networkCostGraph.toString());
        for (int doneAgents = 0; doneAgents < simConfig.getAmountOfAgents(); doneAgents++) {
            //Run one agent
            LinkedList<Integer> agentPath = agent.agentDecide(networkCostGraph, networkCostGraph.getEdgeCosts(), doneAgents);
            for (int i = 0; i < agentPath.size()-1; i++){
                //Add the cost of this agent to the network
                networkCostGraph.addAgent(agentPath.get(i), agentPath.get(i+1));
            }
            System.out.println(doneAgents + " done and current costMatrix:\n" + networkCostGraph.toString());
        }
        return networkCostGraph;
    }

    private static NetworkCostGraph runSimulationForAgent(NetworkAgent networkAgent, final SimConfig simConfig){
        System.out.println("Starting the simulation of " + networkAgent.getClass().getName() + "!");
        NetworkCostGraph ncgDone = runSimulation(simConfig, networkAgent);
        System.out.println("Simulation of " + networkAgent.getClass().getName() + " is finished!");
        return ncgDone;
    }

    public static void main(String[] args) {
        //TODO figure out a better way of changing networks/do all of them after each other
        String simulation = "TestNetwork1";
        System.out.println("GameTheory simEngine started!");
        SimConfig simConfig = null;
        try {
            simConfig = importSimulationConfiguration(simulation);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Loading of " + simulation + " config was not successful, exiting!");
            System.exit(-1);
        }
        System.out.println("Loading of " + simulation +" config successful, running '" + simConfig.getNetTitle() + "'!");
        Map<String, NetworkCostGraph> ncgDones = new HashMap<>();
        ncgDones.put(SelfishRoutingAgent.class.getName(), runSimulationForAgent(new SelfishRoutingAgent(), simConfig));
        //ncgDones.put(TaxedSelfishRoutingAgent.class.getName(), runSimulationForAgent(new TaxedSelfishRoutingAgent(), simConfig));
        //ncgDones.put(CentralizedAgent.class.getName(), runSimulationForAgent(new CentralizedAgent(), simConfig));
        try {
            exportSimulation(simulation, simConfig, ncgDones);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-2);
        }
        System.out.println("Export successful!");
        System.out.println("GameTheory simEngine finished, exiting!");
    }
}
