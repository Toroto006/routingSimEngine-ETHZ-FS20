package agents;

import java.util.LinkedList;

public class SolutionPath {
    private double percentage;
    private LinkedList<Integer> path;


    public SolutionPath(double percentage, LinkedList<Integer> path) {
        this.percentage = percentage;
        this.path = path;
    }

    public double getPercentage() {
        return percentage;
    }

    public LinkedList<Integer> getPath() {
        return path;
    }
}
