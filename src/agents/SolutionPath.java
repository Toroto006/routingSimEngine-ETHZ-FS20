package agents;

import java.util.LinkedList;

public class SolutionPath {
    double percentage;
    LinkedList<Integer> path;

    public double getPercentage() {
        return percentage;
    }

    public SolutionPath(double percentage, LinkedList<Integer> path) {
        this.percentage = percentage;
        this.path = path;
    }

    public LinkedList<Integer> getPath() {
        return path;
    }
}
