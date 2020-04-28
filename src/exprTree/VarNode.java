package exprTree;

import java.util.Map;

public class VarNode extends ExprNode {
    protected String token;

    public VarNode(String s) {
        this.token = s;
    }

    @Override
    public double evaluate(Map<String, Double> tokenMap) {
        return tokenMap.get(token);
    }


    public String toString() {
        return token;
    }
}
