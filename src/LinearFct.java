public class LinearFct implements CostFct {
    private final Float b;
    private final Float a;

    public LinearFct(Float a, Float b){
        this.a = a;
        this.b = b;
    }

    @Override
    public int getCost(int t) {
        return Math.round(a*t + b);
    }

    @Override
    public String toString() {
        return a + "*t+" + b;
    }
}
