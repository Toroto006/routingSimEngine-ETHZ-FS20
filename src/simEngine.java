import java.util.List;

public class simEngine {
    /**
     * import network json, every vertex except dest has to have one outgoing edge
     */
    private void importNetwork() {
        //TODO implement the import and konversion of the json network
        //we give the nodes numbers in lexicographic order for internal purpose
        //e.g. a graph with A B C as nodes will internally be a adj. matrix of size 3, where 0 is A
    }

    /**
     * This will export the simulation, s.t. the visualizer can use it
     */
    private void exportSimulation(){
        //TODO implement export Simulation
    }

    /**
     * This should probably also get the network itself :D
     * @param agents the agents to run this sim
     */
    private void runSimulation(List<NetworkAgent> agents){
        //TODO implement runSim
    }

    public static void main(String[] args) {
        System.out.println("Hello world from simEngine!");
        //TODO import Network and then run simulation!
    }
}
