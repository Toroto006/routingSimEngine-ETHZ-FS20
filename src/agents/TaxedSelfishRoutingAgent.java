package agents;


import com.microsoft.z3.*;
import simEngine.EdgeCosts;
import simEngine.NetworkCostGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;


public class TaxedSelfishRoutingAgent implements NetworkAgent {

    private LinkedList<SolutionPath> solutions;

    @Override
    public String getName() {
        return "TaxedSelfishRoutingAgent";
    }

    @Override
    public LinkedList<Integer> agentDecide(NetworkCostGraph ncg, EdgeCosts ec, int decidedAgents, int totalAgents) {

        if (decidedAgents == 0 || solutions.isEmpty()) {
            ArrayList<LinkedList<Integer>> uniquePaths = AgentUtils.getUniquePaths(ncg);
            calculateSolution(uniquePaths, ec);
        }
        return chooseSolution(decidedAgents, totalAgents);
    }

    /**
     * This method determine the distribution of agent over all unique paths based on mixed strategies and the help of the z3 solver library
     *
     * @param uniquePaths A list of all unique paths
     * @param ec          The object so that the method can look up the cost of edges
     */
    private void calculateSolution(ArrayList<LinkedList<Integer>> uniquePaths, EdgeCosts ec) {
        HashMap<String, ArithExpr> edges = new HashMap<>();
        int amountPaths = uniquePaths.size();

        Context ctx = new Context();
        Solver sol = ctx.mkSimpleSolver();
        ArithExpr[] vars = AgentUtils.initVars(ctx, sol, amountPaths, "p");
        RealExpr cc = ctx.mkRealConst("cost");

        for (int p = 0; p < amountPaths; p++) {
            LinkedList<Integer> path = uniquePaths.get(p);
            Integer from = path.get(0);
            for (int e = 1; e < path.size(); e++) {
                Integer to = path.get(e);
                if (!edges.containsKey(from + " " + to))
                    edges.put(from + " " + to, AgentUtils.doubleToRatNum(ctx, ec.getEdgeCostCustomAgents(from, to, 0)));

                ArithExpr exp = edges.get(from + " " + to);

                // the variable cost is defined here
                ArithExpr cost = ctx.mkAdd(ctx.mkMul(vars[p], AgentUtils.doubleToRatNum(ctx, ec.getDerivativeEdgeCost(from, to))), ctx.mkMul(vars[p], AgentUtils.doubleToRatNum(ctx, ec.getDerivativeEdgeCost(from, to))));
                //ArithExpr cost = ctx.mkMul(vars[p], AgentUtils.doubleToRatNum(ctx, ec.getDerivativeEdgeCost(from, to)));

                ArithExpr newExp = ctx.mkAdd(exp, cost);
                edges.put(from + " " + to, newExp);

                from = to;
            }
        }
        for (LinkedList<Integer> path : uniquePaths) {
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
        for (int i = 0; i < amountPaths; i++) {
            double percentage = AgentUtils.ratNumToDouble((RatNum) m.eval(vars[i], false));
            SolutionPath solPath = new SolutionPath(percentage, uniquePaths.get(i));
            solutions.add(solPath);
            //System.out.println(vars[i].toString() + " = " + percentage);
        }
    }

    /**
     * Chooses a path based on the calculated probability in calculateSolution()
     * An Agent can choose a path based on how many already choose their path
     *
     * @param decidedAgents how many agents choose before him
     * @param totalAgents   how many agents there totally are
     */
    private LinkedList<Integer> chooseSolution(int decidedAgents, int totalAgents) {
        double rand = Math.random() * 1.0 / totalAgents * (totalAgents - decidedAgents);

        for (SolutionPath sol : solutions) {
            if (rand < sol.getPercentage()) {
                sol.changePercentage(-1.0 / totalAgents);
                return sol.getPath();
            }
            rand -= sol.getPercentage();
        }
        return null;
    }
}
