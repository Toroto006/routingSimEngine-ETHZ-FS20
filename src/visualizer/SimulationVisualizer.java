package visualizer;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerPipe;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.graphstream.algorithm.Toolkit.randomNode;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.nodePosition;
import static visualizer.visualizerUtils.createGraphFromJson;
import static visualizer.visualizerUtils.getSimulationsFromJson;

public class SimulationVisualizer {


    public static void main(String[] args) {
        System.out.println("Starting SimulationVisualizer!");
        ArrayList<JSONObject> sims = getSimulationsFromJson("Simulation_out");
        System.out.println("Read simulations from json successfully!");
        ArrayList<runAnimations> graphs = new ArrayList<>();
        for (JSONObject sim: sims) {
            //Create animation in viewer
            Graph g = createGraphFromJson(sim);
            JLabel currentAgent = new JLabel("");
            List<String> agents = new LinkedList<>(((JSONObject) g.getEdge(0).getAttribute("usage")).keySet());
            graphs.add(new runAnimations(g, sim.getInt("amountOfAgents"), agents, currentAgent, 5000));
            //Make everything around the graph and it's animation
            JPanel graphPanel = new JPanel(new GridLayout()){
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(640, 480);
                }
            };
            Viewer viewer = new Viewer(g, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
            viewer.enableAutoLayout();
            ViewPanel viewPanel = viewer.addDefaultView(false);
            graphPanel.add(viewPanel);
            currentAgent.setBounds(6, 6, 400, 40);
            //simFrame.add(currentAgent);
            JFrame simFrame = new JFrame();
            simFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            simFrame.add(graphPanel);
            simFrame.setTitle(sim.getString("networkTitle"));
            simFrame.pack();
            simFrame.setLocationRelativeTo(null);
            simFrame.setVisible(true);
        }
        for (Thread g: graphs)
            g.start();
        System.out.println("Started all simulations!");
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

}
