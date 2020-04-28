package exprTree;

import java.util.Map;
import java.util.Set;

public abstract class ExprNode {
    protected ExprNode leftTree;
    protected ExprNode rightTree;


    public static ExprNode fromString(String str, Set<String> varSet) {
        String s = prepareString(str);
        if (!areBracketsCorrect(s)) {
            throw new NumberFormatException("Incorrect bracket sequence encountered: " + s);
        }
        while (s.charAt(0) == '(' && s.charAt(s.length() - 1) == ')' && areBracketsCorrect(s.substring(1, s.length() - 1))) {
            s = s.substring(1, s.length() - 1);
        }
        try {
            double d = Double.parseDouble(s);
            return new NumNode(d);
        }
        catch (NumberFormatException ignored) {}
        if (varSet.contains(s)) {
            return new VarNode(s);
        }
        int countBracket = 0;
        for (int i = 0; i < s.length(); i++) {
            String temp = s.substring(i + 1);
            switch (s.charAt(i)) {
                case '(':
                    countBracket++;
                    break;
                case ')':
                    countBracket--;
                    if (countBracket < 0 ) throw new NumberFormatException("Incorrect bracket sequence encountered");
                    break;
                case '^':
                    if (countBracket!=0 || (temp.contains("*")  && (!temp.contains("(") || temp.indexOf("*") < temp.indexOf("(")))
                            || (temp.contains("/")  && (!temp.contains("(") || temp.indexOf("/") < temp.indexOf("(")))) {
                        break;
                    }
                case '*':
                case '/':
                    if (countBracket!=0 || (temp.contains("+")  && (!temp.contains("(") || temp.indexOf("+") < temp.indexOf("(")))
                            || (temp.contains("-")  && (!temp.contains("(") || temp.indexOf("-") < temp.indexOf("(")))) {
                        break;
                    }
                case '-':
                    if (countBracket != 0) break;
                    int tempCountBracket = 0;
                    for (int j = 0; j < temp.length(); j++) {
                        switch (temp.charAt(j)) {
                            case '(':
                                tempCountBracket++;
                                break;
                            case ')':
                                tempCountBracket--;
                                break;
                            case '+':
                                if (tempCountBracket == 0) {
                                    temp = temp.substring(0, j) + '-' + temp.substring(j + 1);
                                }
                                break;
                            case '-':
                                if (tempCountBracket == 0) {
                                    temp = temp.substring(0, j) + '+' + temp.substring(j + 1);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                case '+':
                    if (countBracket != 0) break;
                    ExprNode op = new OpNode(s.charAt(i));
                    op.leftTree = ExprNode.fromString(s.substring(0, i), varSet);
                    op.rightTree = ExprNode.fromString(temp, varSet);
                    return op;
            }
        }
        throw new NumberFormatException("Unexpected end of string, arithmetic operation not found: " + s);
    }

    private static boolean areBracketsCorrect(String s) {
        int countBracket = 0;
        for (int i = 0; i < s.length(); i++) {
            switch (s.charAt(i)) {
                case '(':
                    countBracket++;
                    break;
                case ')':
                    countBracket--;
                    if (countBracket < 0) return false;
                    break;
                default:
                    break;
            }
        }
        return (countBracket==0);
    }

    public static String prepareString(String s)
    {
        String res;
        res = (s.replaceAll(" ", "")).replace(',', '.');

        if(res.startsWith("-"))
        {
            res = "0" + res;
        }
        return res;
    }

    public abstract double evaluate(Map<String, Double> tokenMap);
}
