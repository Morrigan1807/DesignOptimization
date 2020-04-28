package exprTree;

import java.util.Map;

public class OpNode extends ExprNode {
    protected char token;
    public OpNode(char token) {
        this.token = token;
    }

    @Override
    public double evaluate(Map<String, Double> tokenMap) {
        switch (token) {
            case '+':
                return leftTree.evaluate(tokenMap) + rightTree.evaluate(tokenMap);
            case '-':
                return leftTree.evaluate(tokenMap) - rightTree.evaluate(tokenMap);
            case '*':
                return leftTree.evaluate(tokenMap) * rightTree.evaluate(tokenMap);
            case '/':
                return leftTree.evaluate(tokenMap) / rightTree.evaluate(tokenMap);
            case '^':
                return Math.pow(leftTree.evaluate(tokenMap), rightTree.evaluate(tokenMap));
            default:
                throw new IllegalStateException("Token for operation node is incorrect: " + token);
        }
    }


    public String toString() {
        return "(" + leftTree.toString() + token + rightTree.toString() + ")";
    }
}
