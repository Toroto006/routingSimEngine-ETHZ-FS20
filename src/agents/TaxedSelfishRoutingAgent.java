package agents;


import com.microsoft.z3.*;
import simEngine.EdgeCosts;
import simEngine.LinearFct;
import simEngine.NetworkCostGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static java.lang.System.exit;


public class TaxedSelfishRoutingAgent implements NetworkAgent {

    private double cost[];
    private int predecessor[];
    private LinearFct fct;
    private String name = "TaxedSelfishRoutingAgent";
    private int numVertices;
    private LinkedList<Integer>[] adjList;
    private ArrayList<LinkedList<Integer>> uniquePaths;
    private LinkedList<SolutionPath> solutions;

    @Override
    public String getName() {
        return this.name;
    }


    public void test() {
        Context ctx = new Context();
        RealExpr l = ctx.mkReal(0);
        RealExpr u = ctx.mkReal((long) 0.125);

        RealExpr a = ctx.mkRealConst("a");
        BoolExpr a1 = ctx.mkLe(a, u);
        BoolExpr a2 = ctx.mkGe(a, l);
        BoolExpr fa = ctx.mkAnd(a1, a2);

        RealExpr b = ctx.mkRealConst("b");
        BoolExpr b1 = ctx.mkLe(b, u);
        BoolExpr b2 = ctx.mkGe(b, l);
        BoolExpr fb = ctx.mkAnd(b1, b2);

        BoolExpr bounds = ctx.mkAnd(fa, fb);

        BoolExpr f1 = ctx.mkEq(ctx.mkAdd(ctx.mkMul(a, ctx.mkReal(1)), ctx.mkMul(b, ctx.mkReal(4))), ctx.mkReal(1));
        BoolExpr f2 = ctx.mkEq(ctx.mkAdd(ctx.mkMul(a, ctx.mkReal(3)), ctx.mkMul(b, ctx.mkReal(4))), ctx.mkReal(2));
        BoolExpr funcs = ctx.mkAnd(f1, f2);
        Solver sol = ctx.mkSimpleSolver();
        sol.add(bounds, funcs);
        System.out.println(sol);
        System.out.println(sol.check());
        Model m = sol.getModel();
        RatNum ta = ((RatNum) m.getConstInterp(a));
        System.out.println("a = " + 1.0 / Integer.parseInt("" + ta.getDenominator()) * Integer.parseInt("" + ta.getNumerator()));
        System.out.println("b = " + m.getConstInterp(b));

    }

    private ArithExpr[] initVars(Context ctx, Solver sol, int amount, String name) {
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

    private static int gcd(int numerator, int denominator) {
        return denominator == 0 ? numerator : gcd(denominator, numerator % denominator);
    }

    private RatNum doubleToRatNum(Context ctx, double number) {

        String s = String.valueOf(number);
        int digitsDec = s.length() - 1 - s.indexOf('.');
        int denominator = 1;

        for (int i = 0; i < digitsDec; i++) {
            if(number == (int) number) break;
            number *= 10;
            denominator *= 10;
        }

        int numerator = (int) Math.round(number);
        int gcd = gcd(numerator, denominator);

        return ctx.mkReal((int) (numerator / gcd), (int) (denominator / gcd));
    }

    private double ratNumToDouble(RatNum number) {

        return 1.0 / Integer.parseInt("" + number.getDenominator()) * Integer.parseInt("" + number.getNumerator());
    }

    @Override
    public LinkedList<Integer> agentDecide(NetworkCostGraph ncg, EdgeCosts ec, int decidedAgents, int totalAgents) {


        if (decidedAgents == 0 || solutions.isEmpty()) {
            HashMap<String, ArithExpr> edges = new HashMap<>();
            ArrayList<LinkedList<Integer>> uniquePaths = getUniquePaths(ncg);
            int amountPaths = uniquePaths.size();

            Context ctx = new Context();
            Solver sol = ctx.mkSimpleSolver();
            ArithExpr[] vars = initVars(ctx, sol, amountPaths, "p");
            RealExpr cc = ctx.mkRealConst("cost");

            for (int p = 0; p < amountPaths; p++) {
                LinkedList<Integer> path = uniquePaths.get(p);
                Integer from = path.get(0);
                for (int e = 1; e < path.size(); e++) {
                    Integer to = path.get(e);
                    if (!edges.containsKey(from + " " + to))
                        edges.put(from + " " + to, doubleToRatNum(ctx, ec.getEdgeCostCustomAgents(from, to, 0)));

                    ArithExpr exp = edges.get(from + " " + to);

                    // the variable cost is defined here
                    ArithExpr cost = ctx.mkAdd(ctx.mkMul(vars[p], doubleToRatNum(ctx, ec.getDerivativeEdgeCost(from, to))), ctx.mkMul(vars[p], doubleToRatNum(ctx, ec.getDerivativeEdgeCost(from, to))));
                    //ArithExpr cost = ctx.mkMul(vars[p], doubleToRatNum(ctx, ec.getDerivativeEdgeCost(from, to)));

                    ArithExpr newExp = ctx.mkAdd(exp, cost);
                    edges.put(from + " " + to, newExp);

                    from = to;
                }
            }
            for (int p = 0; p < amountPaths; p++) {
                LinkedList<Integer> path = uniquePaths.get(p);
                ArithExpr sum = ctx.mkReal(0);
                Integer from = path.get(0);
                for (int e = 1; e < path.size(); e++) {
                    Integer to = path.get(e);
                    ArithExpr exp = edges.get(from + " " + to);
                    sum = ctx.mkAdd(sum, exp);
                    from = to;
                }
                BoolExpr eq = ctx.mkEq(sum, cc);
                sol.add(eq);
            }

            //System.out.println(sol);
            //System.out.println(sol.check());
            sol.check();
            Model m = sol.getModel();
            solutions = new LinkedList<>();
            for(int i = 0; i < amountPaths; i ++){
                double percentage = ratNumToDouble((RatNum) m.eval(vars[i], false));
                SolutionPath solPath = new SolutionPath(percentage, uniquePaths.get(i));
                boolean add = solutions.add(solPath);
                //System.out.println(vars[i].toString() + " = " + percentage);
            }

        }
        return chooseSolution(decidedAgents, totalAgents);

    }

    private LinkedList<Integer> chooseSolution(int decidedAgents, int totalAgents) {
        double rand = Math.random();
        for(SolutionPath sol: solutions) {
            if (rand < sol.getPercentage()){
                return sol.getPath();
            }
            rand -= sol.getPercentage();
        }
        return null;
    }

    private void updateCost(int u, int v, EdgeCosts ec) {
        double costs = cost[u] + (ec.getEdgeCostCustomAgents(u, v, ec.getAgentsOnEdge(u, v) + 1) + ec.getAgentsOnEdge(u, v) * ec.getDerivativeEdgeCost(u, v));
        if (costs < cost[v]) {
            cost[v] = costs;
            predecessor[v] = u;
        }
    }

    /**
     * This will create a list of all unique paths without loops from source to destination.
     *
     * @param ncg the adj. matrix of the current latency cost for the network to decide upon
     * @return List of all unique paths.
     */
    public ArrayList<LinkedList<Integer>> getUniquePaths(NetworkCostGraph ncg) {
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
    public void printAllPaths(int s, int d) {
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
    private void printAllPathsUtil(Integer u, Integer d, boolean[] isVisited, List<Integer> localPathList) {

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
}
