package agents;

import com.microsoft.z3.*;
import simEngine.NetworkCostGraph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AgentUtils {

    private static int numVertices;
    private static LinkedList<Integer>[] adjList;
    private static ArrayList<LinkedList<Integer>> uniquePaths;

    /**
     * This will create a list of all unique paths without loops from source to destination.
     *
     * @param ncg the adj. matrix of the current latency cost for the network to decide upon
     * @return List of all unique paths.
     */
    public static ArrayList<LinkedList<Integer>> getUniquePaths(NetworkCostGraph ncg) {
        int source = 0;
        int dest = ncg.getAdjMatrix().length - 1;
        numVertices = ncg.getNumVertices();

        //create adjacency list
        adjList = new LinkedList[numVertices];
        for (int i = 0; i < adjList.length; i++) {
            adjList[i] = new LinkedList<Integer>();
            for (int j = 0; j < numVertices; j++) {
                if (ncg.getAdjMatrix()[i][j] != Float.MAX_VALUE) {
                    adjList[i].add(j);
                }
            }
        }
        uniquePaths = new ArrayList<>();
        printAllPaths(source, dest);

        return uniquePaths;
    }

    /*
    Thanks to https://www.geeksforgeeks.org/find-paths-given-source-destination/, Himanshu Shekhar
    for printAllPaths and printAllPathsUtil
     */

    /**
     * Finds all paths from s to d
     *
     * @param s source node
     * @param d destination node
     */
    private static void printAllPaths(int s, int d) {
        boolean[] isVisited = new boolean[numVertices];
        ArrayList<Integer> pathList = new ArrayList<>();

        //add source to path[]
        pathList.add(s);

        //Call recursive utility
        printAllPathsUtil(s, d, isVisited, pathList);
    }

    // A recursive function to print
    // all paths from 'u' to 'd'.
    // isVisited[] keeps track of
    // vertices in current path.
    // localPathList<> stores actual
    // vertices in the current path

    /**
     * A recursive function to print all paths from 'u' to 'd'.
     *
     * @param u             current node to find path from to d
     * @param d             destination node
     * @param isVisited     keeps track of visited nodes
     * @param localPathList current found path
     */
    private static void printAllPathsUtil(Integer u, Integer d, boolean[] isVisited, List<Integer> localPathList) {

        // Mark the current node
        isVisited[u] = true;

        if (u.equals(d)) {
            //System.out.println(localPathList);
            LinkedList<Integer> temp = new LinkedList<Integer>(localPathList);
            uniquePaths.add(temp);
            // if match found then no need to traverse more till depth
            isVisited[u] = false;
            return;
        }

        // Recur for all the vertices adjacent to current vertex
        for (Integer i : adjList[u]) {
            if (!isVisited[i]) {
                // store current node
                // in path[]
                localPathList.add(i);
                printAllPathsUtil(i, d, isVisited, localPathList);

                // remove current node
                // in path[]
                localPathList.remove(i);
            }
        }

        // Mark the current node
        isVisited[u] = false;
    }

    /**
     * Finds the gcd of two numbers
     *
     * @param a first number
     * @param b second number
     */
    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    /**
     * Initialises an array of variables: p_0, p_1, ..., 1-p_0-p_1-...
     *
     * @param ctx Context to initialise variables
     * @param sol Solver to add constraints
     * @param amount An Integer that tells how many variables should be initialised
     * @param name what name the variables should take in the format of name_0, name_1, ...
     */
    static ArithExpr[] initVars(Context ctx, Solver sol, int amount, String name) {
        ArithExpr[] out = new ArithExpr[amount];
        RealExpr l = ctx.mkReal(0);
        RealExpr u = ctx.mkReal(1);
        ArithExpr last = ctx.mkReal(1);
        for (int i = 0; i < amount - 1; i++) {
            RealExpr ra = ctx.mkRealConst(name + "_" + (i));
            BoolExpr ba1 = ctx.mkGe(ra, l);
            BoolExpr ba2 = ctx.mkLe(ra, u);
            BoolExpr aa = ctx.mkAnd(ba1, ba2);
            out[i] = ra;
            sol.add(aa);
            ArithExpr next = ctx.mkSub(last, ra);
            last = next;
        }

        BoolExpr l1 = ctx.mkGe(last, l);
        BoolExpr l2 = ctx.mkLe(last, u);
        BoolExpr al = ctx.mkAnd(l1, l2);
        out[amount - 1] = last;
        sol.add(al);
        return out;
    }

    /**
     * Creates a RatNum (Rational number) from a double
     *
     * @param ctx Context to create the RatNum object
     * @param number what value the fraction should represent
     */
    public static RatNum doubleToRatNum(Context ctx, double number) {

        String s = String.valueOf(number);
        int digitsDec = s.length() - 1 - s.indexOf('.');
        int denominator = 1;

        for (int i = 0; i < digitsDec; i++) {
            if (number == (int) number) break;
            number *= 10;
            denominator *= 10;
        }

        int numerator = (int) Math.round(number);
        int gcd = gcd(numerator, denominator);

        return ctx.mkReal(numerator / gcd, denominator / gcd);
    }

    /**
     * Converts a RatNum to a double
     *
     * @param number the Rational number which is converted to a double
     */
    public static double ratNumToDouble(RatNum number) {

        return 1.0 / Integer.parseInt("" + number.getDenominator()) * Integer.parseInt("" + number.getNumerator());
    }

}
