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
            System.out.println(o.toString() + "\n");

            
            CostFct c = null;
            String costFctStr = edge.getString("cost");
            System.out.println(edge.toString() + "\n");
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

            JSONArray connections = edge.getJSONArray("connection");
            int i = nodesMapped.get(connections.get(0));
            int j = nodesMapped.get(connections.get(1));
            
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



    private static JSONObject finalExport;

    /**
     * This will export the simulation, s.t. the visualizer can use it
     */
<<<<<<< HEAD
    private static void exportSimulation(SimConfig simConfig, NetworkCostGraph ncgDone, String nameOfAgent) throws Exception {
        //json in java https://www.tutorialspoint.com/json/json_java_example.htm
        //TODO implement export Simulation
        JSONObject export = new JSONObject();
=======
    private static void exportSimulation(String simulationName, SimConfig simConfig, Map<String, NetworkCostGraph> ncgDones) throws Exception {
        for (String agentName: ncgDones.keySet()) {
            //json in java https://www.tutorialspoint.com/json/json_java_example.htm
            String exportName = simulationName + "_" + agentName;
            NetworkCostGraph ncgDone = ncgDones.get(agentName);
            //Actual export
            JSONObject export = new JSONObject();
>>>>>>> 591bcb64390c381f3940f4f90ec31c37c6f8012e

            String[] nodes = simConfig.getNodes();
            JSONArray jsNodes = new JSONArray(nodes);
            export.put("nodes", (Object) jsNodes);

            JSONArray jsEdges = new JSONArray();
            Map<String, Edge> mapEdges = ncgDone.getEdges();
            for (Map.Entry<String, Edge> entry : mapEdges.entrySet()) {
                String key = entry.getKey();
                Integer[] iNodes = Arrays.stream(key.split(" ", 2)).map(o -> Integer.parseInt(o)).toArray(Integer[]::new);
                Edge edge = entry.getValue();

<<<<<<< HEAD
            JSONObject jsTemp = new JSONObject();
            jsTemp.put("cost", edge.getCostFct().toString());
            String[] c = {nodes[iNodes[0]], nodes[iNodes[1]]};
            JSONArray connection = new JSONArray(c);
            jsTemp.put("connection", connection);
            jsTemp.put("usage", edge.getAgents());
            jsEdges.put(jsTemp);
        }
        export.put("edges", jsEdges);

        finalExport.put(nameOfAgent, export);
        System.out.println("Simulation of " + nameOfAgent + " is finished");

    }

    private static void exportSimulationsToFile(String exportName) throws Exception {
        String filePath = "./networks/"+exportName+".json";
        FileWriter file;
        file = new FileWriter(filePath);
        try {
            file.write(finalExport.toString());
        } catch (Exception e) {
            throw new Exception("Couldn't write to/create the following file: " + filePath);
        } finally {
=======
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
>>>>>>> 591bcb64390c381f3940f4f90ec31c37c6f8012e
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
<<<<<<< HEAD
        System.out.println("Simulation \"" + exportName + "\" is finished and saved");

=======
>>>>>>> 591bcb64390c381f3940f4f90ec31c37c6f8012e
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

<<<<<<< HEAD
    private static void runSimulationForAgent(NetworkAgent networkAgent, SimConfig simConfig){
        System.out.println("Starting the simulation of " + networkAgent.getClass().getName() + "!");
        NetworkCostGraph ncgDone = runSimulation(simConfig, networkAgent);
        String agentName = networkAgent.getClass().getName();
        //TODO actually somehow return simulation result to export
        try {
            exportSimulation(simConfig, ncgDone, agentName);
        } catch (Exception e) {
            e.printStackTrace();
        }
=======
    private static NetworkCostGraph runSimulationForAgent(NetworkAgent networkAgent, final SimConfig simConfig){
        System.out.println("Starting the simulation of " + networkAgent.getClass().getName() + "!");
        NetworkCostGraph ncgDone = runSimulation(simConfig, networkAgent);
        System.out.println("Simulation of " + networkAgent.getClass().getName() + " is finished!");
        return ncgDone;
>>>>>>> 591bcb64390c381f3940f4f90ec31c37c6f8012e
    }

    public static void main(String[] args) throws Exception {
        //TODO figure out a better way of changing networks/do all of them after each other
        String simulation = "BrassParadox1";
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
<<<<<<< HEAD

        finalExport = new JSONObject();
        runSimulationForAgent(new SelfishRoutingAgent(), simConfig);

        exportSimulationsToFile(simulation + "_out");
        //TODO set the correct agents here!
        //runSimulationForAgent(new Agent2(), simConfig, simulation);
        //runSimulationForAgent(new Agent3(), simConfig, simulation);
=======
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
>>>>>>> 591bcb64390c381f3940f4f90ec31c37c6f8012e
        System.out.println("GameTheory simEngine finished, exiting!");
    }
}
