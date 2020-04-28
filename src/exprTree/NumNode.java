package exprTree;

import java.util.Map;

public class NumNode extends ExprNode {
    protected double token;

    public NumNode(double d) {
        this.token = d;
    }

    @Override
    public double evaluate(Map<String, Double> tokenMap) {
        return token;
    }

    public String toString() {
        return String.valueOf(token);
    }
}
