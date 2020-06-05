package simEngine;

public class LinearFct implements CostFct {
    private final Float b;
    private final Float a;

    public LinearFct(Float a, Float b){
        this.a = a;
        this.b = b;
    }

    @Override
    public Float getCost(int t) {
        return a*t + b;
    }

    @Override
    public Float getDerivativeCost() {
        return a;
    }

    @Override
    public String toString() {
        return a + "*t+" + b;
    }
}
