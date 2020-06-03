import java.util.LinkedList;
import java.util.List;

public class simEngine {
    /**
     * import network json, every vertex except dest has to have one outgoing edge
     * @param simulation
     */
    private static SimConfig importSimulationConfiguration(String simulation) {
        //TODO implement the import and konversion of the json network and
        //we give the nodes numbers in lexicographic order for internal purpose
        //e.g. a graph with A B C as nodes will internally be a adj. matrix of size 3, where 0 is A
        return null;
    }

    /**
     * This will export the simulation, s.t. the visualizer can use it
     * @param exportSim
     */
    private static void exportSimulation(String exportSim){
        //TODO implement export Simulation
    }

    /**
     * This runs the whole simulation for one agent
     * @param simConfig the configuration of the simulation
     * @param agent the agent to use for this simulation
     */
    private static void runSimulation(SimConfig simConfig, NetworkAgent agent){
        NetworkCostGraph networkCostGraph = new NetworkCostGraph(simConfig.getNetworkGraph());
        for (int doneAgents = 0; doneAgents < simConfig.getAmountOfAgents(); doneAgents++) {
            //Run one agent
            LinkedList<Integer> agentPath = agent.agentDecide(networkCostGraph, networkCostGraph.getEdgeCosts(), doneAgents);
            for (int i = 0; i < agentPath.size()-1; i++){
                //Add the cost of this agent to the network
                networkCostGraph.addAgent(agentPath.get(i), agentPath.get(i+1));
            }
            //TODO somehow save the progress somewhere to then export simulation
        }
    }

    private static void runSimulationForAgent(NetworkAgent networkAgent, SimConfig simConfig, String simulationName){
        System.out.println("Starting the simulation of " + networkAgent.getClass().getName() + "!");
        runSimulation(simConfig, networkAgent);
        String exportSim = simulationName+"_" + networkAgent.getClass().getName();
        //TODO actually somehow return simulation result to export
        exportSimulation(exportSim);
        System.out.println("Simulation of " + networkAgent.getClass().getName() + " is finished and saved to " + exportSim + ".json");
    }

    public static void main(String[] args) {
        String simulation = "BrassParadox1";
        System.out.println("GameTheory simEngine started!");
        SimConfig simConfig = importSimulationConfiguration(simulation);
        if (simConfig.loadedSuccessful()) {
            System.out.println("Loading of " + simulation +" config successful!");
            //TODO set the correct agents here!
            //runSimulationForAgent(new Agent1(), simConfig, simulation);
            //runSimulationForAgent(new Agent2(), simConfig, simulation);
            //runSimulationForAgent(new Agent3(), simConfig, simulation);
        } else
            System.out.println("Loading of " + simulation + " config was not successful!");
        System.out.println("GameTheory simEngine finished, exiting!");
    }
}
