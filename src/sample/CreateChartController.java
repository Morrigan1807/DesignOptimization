package sample;

import exprTree.ExprNode;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleFunction;

public class CreateChartController
{
    public ExprNode exprNode;
    public Group chart;
    private final Circle START = new Circle(500, 500, 2);

    public void draw(double x1, double x2)
    {
        buildField();
        printDot(x1,x2, Color.PURPLE);
    }

    private void buildField()
    {
        for(int i = -10; i < 11; i++)
        {
            if(i != 0) {
                printLine(i, -10, i, 10, Color.LIGHTGRAY, 1);
                printText(i, -0.3, String.valueOf(i));
                printLine(-10, i, 10, i, Color.LIGHTGREY, 1);
                printText(-0.3, i, String.valueOf(i));
            }
            else
            {
                printText(-0.2, -0.3, String.valueOf(i));
            }
        }
        printLine(-10, 0, 10, 0, Color.BLACK, 1);
        printLine(0, -10, 0, 10, Color.BLACK, 1);
    }

    public void buildLevelLines(double x1, double x2, double f, boolean isMax)
    {
        Color[] colorLines = {Color.RED, Color.ORANGE, Color.YELLOW, Color.rgb(15, 255, 15), Color.BLUE};
        double temp_f = Math.floor(f);
        double[] func  = {isMax ? temp_f : temp_f + 1, isMax ? temp_f - 1: temp_f + 2,
                isMax ? temp_f - 2: temp_f + 3, isMax ? temp_f - 3: temp_f + 4,
                isMax ? temp_f - 4: temp_f + 5};
        double[] radii = new double[5];

        for(int i = 4; i >= 0; i--)
        {
            radii[i] = calcRadius(x1, x2, func[i], isMax);
            printCircle(x1, x2, radii[i], colorLines[i]);
        }
    }

    public void solutionPath(ArrayList<DoublePair> pathDots)
    {
        for(int i = 0; i < pathDots.size() - 1; i++)
        {
            printLine(pathDots.get(i).x1, pathDots.get(i).x2, pathDots.get(i+1).x1, pathDots.get(i+1).x2, Color.TEAL, 2);
        }
    }

    private boolean isAcceptable(double x, double y, double resFunc, boolean isMax)
    {
        return isMax ? exprNode.evaluate(Map.of("x1", x, "x2", y)) > resFunc :
                exprNode.evaluate(Map.of("x1", x, "x2", y)) < resFunc;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private double calcRadius(double startX, double startY, double resFunc, boolean isMax)
    {
        double res, xLeft = startX, xRight;

        for(xRight = startX + 1; isAcceptable(xRight, startY, resFunc, isMax); xRight +=1, xLeft += 1);
        double x = (xLeft + xRight) / 2;
        double temp_f = exprNode.evaluate(Map.of("x1", x, "x2", startY));
        while (Math.abs(temp_f - resFunc) > 0.01)
        {
            if(isAcceptable(x, startY, resFunc, isMax))
            {
                xLeft = x;
            }
            else
            {
                xRight = x;
            }
            x = (xLeft + xRight) / 2;
            temp_f = exprNode.evaluate(Map.of("x1", x, "x2", startY));
        }

        res = Math.abs(startX - x);
        return res;
    }

    public void printDot(double x1, double x2, Color color)
    {
        chart.getChildren().add(new Circle(START.getCenterX() + (x1 * 50),
                START.getCenterY() - (x2 * 50), 2, color));
    }

    public void printCircle(double x1, double x2, double radius, Color color)
    {
        chart.getChildren().add(new Circle(START.getCenterX() + (x1 * 50),
                START.getCenterY() - (x2 * 50), radius * 50, color));
    }

    public void printLine(double x1_start, double x2_start, double x1_end, double x2_end, Color color, double strokeWidth)
    {
        Line line = new Line(START.getCenterX() + (x1_start * 50),
                START.getCenterY() - (x2_start * 50), START.getCenterX() + (x1_end * 50),
                START.getCenterY() - (x2_end * 50));
        line.setStroke(color);
        line.setStrokeWidth(strokeWidth);
        chart.getChildren().add(line);
    }

    public void printText(double x1, double x2, String text)
    {
        chart.getChildren().add(new Text(START.getCenterX() + (x1 * 50), START.getCenterY() - (x2 * 50), text));
    }

    private boolean areDoublesEqual(double expected, double actual) {
        return Math.abs(actual - expected) <= 0.01;
    }

    private boolean isArgumentSolutionInInterval(DoubleFunction<Double> function,
                                                 double expectedResult,
                                                 double leftBound,
                                                 double rightBound) {
        double leftFuncValue = function.apply(leftBound);
        double rightFuncValue = function.apply(rightBound);
        return (leftFuncValue < rightFuncValue && leftFuncValue < expectedResult && expectedResult < rightFuncValue)
                ||(leftFuncValue > rightFuncValue && leftFuncValue > expectedResult && expectedResult > rightFuncValue);
    }
    private Double findArgumentSolutionInInterval(DoubleFunction<Double> function,
                                                  double expectedResult,
                                                  double leftBound,
                                                  double rightBound) {
        if (!isArgumentSolutionInInterval(function, expectedResult, leftBound, rightBound))
            return null;
        double leftX = leftBound, rightX=rightBound, x=(leftX + rightX)/2;
        double leftFuncValue = function.apply(leftX), rightFuncValue = function.apply(rightX);
        boolean asc = (leftFuncValue < rightFuncValue);
        double temp_f = function.apply(x);

        while (!areDoublesEqual(expectedResult, temp_f)) {
            if ((asc)?temp_f<expectedResult:temp_f>expectedResult) {
                leftX = x;
            }
            else {
                rightX = x;
            }
            x = (leftX + rightX) / 2;
            temp_f = function.apply(x);
        }

        return x;
    }
    private List<DoublePair> findBoundsIntersection(ExprNode constraint) {
        DoublePair[] corners = {new DoublePair(-10, -10), new DoublePair(-10, 10), new DoublePair(10, 10),
                new DoublePair(10, -10)};
        List<DoublePair> resultList = new ArrayList<>();
        for (DoublePair corner: corners) {
            if (areDoublesEqual(0, calc(constraint, corner.x1, corner.x2)))
                resultList.add(corner);
        }
        int i = 0;
        DoublePair currentCorner, nextCorner;
        while (resultList.size() < 2 || i != 4) {
            currentCorner = corners[i];
            if (i==3) nextCorner = corners[0];
            else nextCorner = corners[i+1];
            if (currentCorner.x1 == nextCorner.x1) {
                double xFixed = currentCorner.x1;
                Double xToFind = null;
                if (currentCorner.x2 < nextCorner.x2)
                    xToFind = findArgumentSolutionInInterval(v->calc(constraint, xFixed, v), 0, currentCorner.x2, nextCorner.x2);
                else
                    xToFind = findArgumentSolutionInInterval(v->calc(constraint, xFixed, v), 0, nextCorner.x2, currentCorner.x2);
                if (xToFind != null) {
                    DoublePair pointToBeReturned = new DoublePair(xFixed, xToFind);
                    if (!(pointToBeReturned == currentCorner || pointToBeReturned == nextCorner)) {
                        resultList.add(pointToBeReturned);
                    }
                }
            }
            else {
                double xFixed = currentCorner.x2;
                Double xToFind = null;
                if (currentCorner.x1 < nextCorner.x1)
                    xToFind = findArgumentSolutionInInterval(v->calc(constraint, v, xFixed), 0, currentCorner.x1, nextCorner.x1);
                else
                    xToFind = findArgumentSolutionInInterval(v->calc(constraint, v, xFixed), 0, nextCorner.x1, currentCorner.x1);
                if (xToFind != null) {
                    DoublePair pointToBeReturned = new DoublePair(xToFind, xFixed);
                    if (!(pointToBeReturned == currentCorner || pointToBeReturned == nextCorner)) {
                        resultList.add(pointToBeReturned);
                    }
                }
            }
            i++;
        }
        if (resultList.size() < 2) {
            throw new RuntimeException("Could not find intersection for the constraint: " + constraint.toString());
        }
        else {
            return resultList;
        }
    }
    //this method will print the constraint from it's left and part parts
    public void drawConstraint(String constraintLeftPart, String constraintRightPart) {
        constraintLeftPart = ExprNode.prepareString(constraintLeftPart);
        constraintRightPart = ExprNode.prepareString(constraintRightPart);
        ExprNode constraint = ExprNode.fromString("("+constraintLeftPart+")-("+constraintRightPart+")", Set.of("x1","x2"));
        List<DoublePair> points = findBoundsIntersection(constraint);
        DoublePair firstPoint = points.get(0),
                secondPoint = points.get(1);
        printLine(firstPoint.x1, firstPoint.x2, secondPoint.x1, secondPoint.x2, Color.BROWN, 1);
    }

    private double calc(ExprNode exprNode, double x1, double x2) {
        return exprNode.evaluate(Map.of("x1",x1,"x2",x2));
    }
}
