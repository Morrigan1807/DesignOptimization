package sample;

import exprTree.ExprNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.*;

public class Controller
{
    public Label leadTime11;
    private Random rnd = new Random();
    public TextField function;
    public TextField step;
    public TextField x1min;
    public TextField x1max;
    public TextField x2min;
    public TextField x2max;
    public TextField x1_res;
    public TextField x2_res;
    public TextField f_res;
    public TextField x1_mod_res;
    public TextField x2_mod_res;
    public TextField f_mod_res;
    public TextField iteration;
    ObservableList<String> methods = FXCollections.observableArrayList
            ("Grid Iteration Method", "Monte Carlo Method", "Hook-Jeeves Method", "Penalty function method");
    public ComboBox <String> chooseMethod;
    ObservableList<String> typesOfProblem = FXCollections.observableArrayList
            ("-> max", "-> min");
    public ComboBox <String> chooseTypeOfProblem;
    public VBox leftPartOfConstraint;
    public VBox centerPartOfConstraint;
    public VBox rightPartOfConstraint;
    public Label leadTime;
    public Label modLeadTime;
    public Label modIterationStep;
    public Label iterationStep;
    public TextField eps;
    private DoublePair dp_res;
    private ArrayList<DoublePair> pathDots = new ArrayList<>();

    public void initialize()
    {
        chooseMethod.setItems(methods);
        chooseMethod.setValue("Grid Iteration Method");
        chooseTypeOfProblem.setItems(typesOfProblem);
        chooseTypeOfProblem.setValue("-> max");
        function.setText("-x1^2-x2^2+2*x1+2*x2+2");
    }

    private double calc_function(DoublePair dot)
    {
        ExprNode exprNode = ExprNode.fromString(function.getText(), Set.of("x1", "x2"));
        return exprNode.evaluate(Map.of("x1", dot.x1, "x2", dot.x2));
    }

    private double calc_expr_function(ExprNode exprNode, DoublePair dot)
    {
        return exprNode.evaluate(Map.of("x1", dot.x1, "x2", dot.x2));
    }

    private ExprNode parse_penalty_function()
    {
        StringBuilder penalty_function = new StringBuilder(function.getText());
        String constraint;
        for (int i=0; i<leftPartOfConstraint.getChildren().size(); i++)
        {
            constraint = ExprNode.prepareString(((TextField)leftPartOfConstraint.getChildren().get(i)).getText());
            constraint += "-" + ((TextField)rightPartOfConstraint.getChildren().get(i)).getText();
            //noinspection rawtypes
            switch(((ComboBox)centerPartOfConstraint.getChildren().get(i)).getValue().toString()) {
                case "<=":
                    if((chooseTypeOfProblem.getValue().equals("-> min")))
                    {
                        constraint = "0-(" + ExprNode.prepareString(constraint) + ")";
                    }
                    break;
                case  "=":
                    break;
                case ">=":
                    if((chooseTypeOfProblem.getValue().equals("-> max")))
                    {
                        constraint = "0-(" + ExprNode.prepareString(constraint) + ")";
                    }
                    break;
            }
            switch (chooseTypeOfProblem.getValue())
            {
                case "-> max":
                    constraint = "0-" + Math.E + "^(" + ExprNode.prepareString(constraint) + ")";
                    break;
                case "-> min":
                    constraint = Math.E + "^(0-(" + ExprNode.prepareString(constraint) + "))";
                    break;
            }

            penalty_function.append("+(").append(constraint).append(")");

        }
        return ExprNode.fromString(penalty_function.toString(), Set.of("x1", "x2"));
    }

    public double doubleRand(double min, double max)
    {
        double res = rnd.nextDouble() * (max - min) +  min;
        return (double) Math.round(res * 100) / 100;
    }

    private boolean constraintsPassed(DoublePair dot)
    {
        boolean res = true;
        for (int i=0; i<leftPartOfConstraint.getChildren().size() && res; i++)
        {
            //noinspection rawtypes
            switch(((ComboBox)centerPartOfConstraint.getChildren().get(i)).getValue().toString())
            {
                case "<=":
                    if (!(ExprNode.fromString(((TextField)leftPartOfConstraint.getChildren().get(i)).getText(),
                            Set.of("x1","x2")).evaluate(Map.of("x1",dot.x1,"x2",dot.x2))
                            <= Double.parseDouble(((TextField)rightPartOfConstraint.getChildren().get(i)).
                            getText())))
                        res=false;
                    break;
                case "=":
                    if (!(ExprNode.fromString(((TextField)leftPartOfConstraint.getChildren().get(i)).getText(),
                            Set.of("x1","x2")).evaluate(Map.of("x1",dot.x1,"x2",dot.x2))
                            == Double.parseDouble(((TextField)rightPartOfConstraint.getChildren().get(i)).
                            getText())))
                        res=false;
                    break;
                case ">=":
                    if (!(ExprNode.fromString(((TextField)leftPartOfConstraint.getChildren().get(i)).getText(),
                            Set.of("x1","x2")).evaluate(Map.of("x1",dot.x1,"x2",dot.x2))
                            >= Double.parseDouble(((TextField)rightPartOfConstraint.getChildren().get(i)).
                            getText())))
                        res=false;
                    break;
            }
        }
        return res;
    }

    private void outResults(DoublePair res)
    {
        x1_res.setText(String.valueOf(res.x1));
        x2_res.setText(String.valueOf(res.x2));
        f_res.setText(String.valueOf(res.func));
    }

    private void gridIterationMethod(double h, double x1_min, double x1_max, double x2_min, double x2_max, boolean constraint_flag)
    {
        boolean flag = false;
        int iteration = -1, iter = 0;
        DoublePair res = new DoublePair(0,0);
        double temp_f;

        long start_time = System.nanoTime();

        for(double x1 = x1_min; x1 <= x1_max; x1+=h)
        {
            for(double x2 = x2_min; x2 <= x2_max; x2+=h)
            {
                iter++;
                if(!flag) {
                    if (constraint_flag || constraintsPassed(new DoublePair(x1, x2))) {
                        res = new DoublePair(x1, x2);
                        res.func = calc_function(res);
                        flag = true;
                        iteration = iter;
                    }
                }
                else
                {
                    temp_f = calc_function(new DoublePair(x1, x2));
                    if ((constraint_flag || constraintsPassed(new DoublePair(x1,x2))) && ((chooseTypeOfProblem.getValue().equals("-> max"))?
                            (res.func < temp_f):(res.func > temp_f))) {
                        res = new DoublePair(x1, x2);
                        res.func = temp_f;
                        iteration = iter;
                    }
                }
            }
        }

        if(constraint_flag)
        {
            dp_res = res;
        }
        else
        {
            resultOfCalc(flag, res, iteration, start_time);
        }
    }

    public void monteCarloMethod(ArrayList<DoublePair> random_x)
    {
        boolean flag = false;
        DoublePair res = new DoublePair(0, 0);
        int iteration = -1, iter = 0;
        double temp_f;
        long start_time = System.nanoTime();

        for(DoublePair dp: random_x)
        {
            iter++;
            if(!flag) {
                if (constraintsPassed(dp)) {
                    res = dp;
                    res.func = calc_function(dp);
                    iteration = iter;
                    flag = true;
                }
            }
            else {
                temp_f = calc_function(dp);
                if (constraintsPassed(dp) && ((chooseTypeOfProblem.getValue().equals("-> max"))?
                        (res.func < temp_f):(res.func > temp_f))) {
                    res = dp;
                    res.func = temp_f;
                    iteration = iter;
                }
            }
        }

        resultOfCalc(flag, res, iteration, start_time);
    }

    private void resultOfCalc(boolean flag, DoublePair res, int iteration, long start_time) {
        if(!flag)
        {
            new Alert(Alert.AlertType.INFORMATION, "No solution found.").showAndWait();
            return;
        }

        long end_time = System.nanoTime();
        outResults(res);
        leadTime.setText(((end_time - start_time) / Math.pow(10, 9)) + " s.");
        iterationStep.setText(String.valueOf(iteration));
    }

    public void modificationMonteCarloMethod(Set<DoublePair> random_x)
    {
        DoublePair res = new DoublePair(0,0);
        double temp_f;
        boolean flag = false;
        int iteration = -1, iter = 0;
        long start_time = System.nanoTime();

        for(DoublePair dot: random_x)
        {
            iter++;
            if(!flag) {
                if (constraintsPassed(dot)) {
                    res = dot;
                    res.func = calc_function(dot);
                    iteration = iter;
                    flag = true;
                }
            }
            else {
                temp_f = calc_function(dot);

                if (constraintsPassed(dot) && ((chooseTypeOfProblem.getValue().equals("-> max"))?(res.func < temp_f):(res.func > temp_f))) {
                    res = dot;
                    res.func = calc_function(dot);
                    iteration = iter;
                }
            }
        }
        if(!flag)
        {
            new Alert(Alert.AlertType.INFORMATION, "No solution found.").showAndWait();
            return;
        }

        long end_time = System.nanoTime();

        x1_mod_res.setText(String.valueOf(res.x1));
        x2_mod_res.setText(String.valueOf(res.x2));
        f_mod_res.setText(String.valueOf(res.func));

        modLeadTime.setText(((end_time - start_time) / Math.pow(10, 9)) + " s.");
        modIterationStep.setText(String.valueOf(iteration));
    }

    private void clearBoard()
    {
        x1_mod_res.setText("");
        x2_mod_res.setText("");
        f_mod_res.setText("");
        x1_res.setText("");
        x2_res.setText("");
        f_res.setText("");

        modLeadTime.setText("");
        modIterationStep.setText("");
        leadTime.setText("");
        iterationStep.setText("");
    }

    private DoublePair leftStep(DoublePair dot, double h)
    {
        return new DoublePair(dot.x1 - h, dot.x2);
    }

    private DoublePair rightStep(DoublePair dot, double h)
    {
        return new DoublePair(dot.x1 + h, dot.x2);
    }

    private DoublePair upStep(DoublePair dot, double h)
    {
        return new DoublePair(dot.x1 , dot.x2 + h);
    }

    private DoublePair downStep(DoublePair dot, double h)
    {
        return new DoublePair(dot.x1, dot.x2 - h);
    }

    private DoublePair generateDot()
    {
        DoublePair res = new DoublePair(doubleRand(Double.parseDouble(x1min.getText()), Double.parseDouble(x1max.getText())),
                doubleRand(Double.parseDouble(x2min.getText()), Double.parseDouble(x2max.getText())));

        if(constraintsPassed(res))
            return res;
        else
            return generateDot();
    }

    public void hookJeevesMethod(ExprNode exprNode, DoublePair res)
    {
        pathDots.clear();
        pathDots.add(res);
        res.func = calc_expr_function(exprNode, res);
        double h = Double.parseDouble(step.getText());
        DoublePair temp_dot = new DoublePair(0, 0);
        double epsilon = Double.parseDouble(eps.getText());
        int i = 0;

        long start_time = System.nanoTime();

        while (h > epsilon)
        {
            switch (i)
            {
                case 0:
                    temp_dot = leftStep(res, h);
                    break;
                case 1:
                    temp_dot = upStep(res, h);
                    break;
                case 2:
                    temp_dot = rightStep(res, h);
                    break;
                case 3:
                    temp_dot = downStep(res, h);
                    break;
                case 4:
                    h /= 2;
                    i = 0;
                    break;
                default:
                    break;
            }

            temp_dot.func = calc_expr_function(exprNode, temp_dot);

            if((chooseTypeOfProblem.getValue().equals("-> max"))?(res.func < temp_dot.func):(res.func > temp_dot.func))
            {
                res = temp_dot;
                pathDots.add(res);
                res.func = temp_dot.func;
                i = 0;
            }
            else
            {
                i++;
            }
        }
        long end_time = System.nanoTime();
        outResults(res);
        leadTime.setText(((end_time - start_time) / Math.pow(10, 9)) + " s.");
    }

    public void penaltyFunctionMethod()
    {
        DoublePair dot = new DoublePair(doubleRand(Double.parseDouble(x1min.getText()), Double.parseDouble(x1max.getText())),
        doubleRand(Double.parseDouble(x2min.getText()), Double.parseDouble(x2max.getText())));

        ExprNode exprNode = parse_penalty_function();
        hookJeevesMethod(exprNode, dot);
    }

    public void calculateAnswer()
    {
        switch (chooseMethod.getValue())
        {
            case "Grid Iteration Method":
            {
                clearBoard();
                gridIterationMethod(Double.parseDouble(step.getText()), Double.parseDouble(x1min.getText()),
                        Double.parseDouble(x1max.getText()), Double.parseDouble(x2min.getText()),
                        Double.parseDouble(x2max.getText()), false);
                break;
            }

            case "Monte Carlo Method":
            {
                clearBoard();
                Set<DoublePair> random_x = new HashSet<>();
                ArrayList<DoublePair> randomX = new ArrayList<>();
                double x1, x2;

                for(int i = 0; i < Integer.parseInt(iteration.getText()); i++)
                {
                    x1 = doubleRand(Double.parseDouble(x1min.getText()), Double.parseDouble(x1max.getText()));
                    x2 = doubleRand(Double.parseDouble(x2min.getText()), Double.parseDouble(x2max.getText()));
                    random_x.add(new DoublePair(x1, x2));
                    randomX.add(new DoublePair(x1,x2));
                }
                monteCarloMethod(randomX);
                modificationMonteCarloMethod(random_x);
                break;
            }

            case "Hook-Jeeves Method":
            {
                clearBoard();
                ExprNode exprNode = ExprNode.fromString(function.getText(), Set.of("x1", "x2"));
                DoublePair res = generateDot();
                hookJeevesMethod(exprNode, res);
                break;
            }

            case "Penalty function method":
            {
                clearBoard();
                penaltyFunctionMethod();
                break;
            }
        }
    }

    public void addConstraint()
    {
        ObservableList<String> centerParts = FXCollections.observableArrayList
                (">=", "=", "<=");
        leftPartOfConstraint.getChildren().add(new TextField());
        centerPartOfConstraint.getChildren().add(new ComboBox<>(centerParts));
        rightPartOfConstraint.getChildren().add(new TextField());
    }

    public void deleteConstraint()
    {
        leftPartOfConstraint.getChildren().remove(leftPartOfConstraint.getChildren().size() - 1);
        rightPartOfConstraint.getChildren().remove(rightPartOfConstraint.getChildren().size() - 1);
        centerPartOfConstraint.getChildren().remove(centerPartOfConstraint.getChildren().size() - 1);
    }

    public void clearConstraint()
    {
        leftPartOfConstraint.getChildren().clear();
        centerPartOfConstraint.getChildren().clear();
        rightPartOfConstraint.getChildren().clear();
    }

    public void drawChart() throws IOException
    {
        Stage secondStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("createChart.fxml"));
        Parent root = fxmlLoader.load();
        CreateChartController ccc = fxmlLoader.getController();
        ccc.exprNode = ExprNode.fromString(function.getText(), Set.of("x1", "x2"));

        gridIterationMethod(0.1, -10, 10, -10, 10, true);
        ccc.buildLevelLines(dp_res.x1, dp_res.x2, dp_res.func,
                chooseTypeOfProblem.getValue().equals("-> max"));
        for(int i = 0; i < leftPartOfConstraint.getChildren().size(); i++) {
            ccc.drawConstraint(((TextField)leftPartOfConstraint.getChildren().get(i)).getText(),
                    ((TextField)rightPartOfConstraint.getChildren().get(i)).getText());
        }
        ccc.solutionPath(pathDots);
        ccc.draw(Double.parseDouble(x1_res.getText()), Double.parseDouble(x2_res.getText()));
        secondStage.setTitle("Level lines");
        secondStage.setScene(new Scene(root, 1000, 500));
        secondStage.showAndWait();
    }
}
