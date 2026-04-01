package nl.han.ica.icss.transforms;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Evaluator implements Transform {

    private LinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new LinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        variableValues = new LinkedList<>();
        applyStylesheet(ast.root);
    }

    private void applyStylesheet(Stylesheet stylesheet) {
        variableValues.addFirst(new HashMap<>());
        for (int i = 0; i < stylesheet.getChildren().size(); i++) {
            ASTNode child = stylesheet.getChildren().get(i);
            if (child instanceof VariableAssignment) {
                applyVariableAssignment((VariableAssignment) child);
                stylesheet.removeChild(child);
                i--;
            } else if (child instanceof Stylerule) {
                applyStylerule((Stylerule) child);
            }
        }
        variableValues.removeFirst();
    }

    private void applyStylerule(Stylerule stylerule) {
        variableValues.addFirst(new HashMap<>());
        applyBodyNodes(stylerule.body);
        variableValues.removeFirst();
    }

    private void applyBodyNodes(ArrayList<ASTNode> body) {
        for (int i = 0; i < body.size(); i++) {
            ASTNode node = body.get(i);
            if (node instanceof VariableAssignment) {
                applyVariableAssignment((VariableAssignment) node);
                body.remove(i);
                i--;
            } else if (node instanceof Declaration) {
                applyDeclaration((Declaration) node);
            } else if (node instanceof IfClause) {
                List<ASTNode> replacement = applyIfClause((IfClause) node);
                body.remove(i);
                body.addAll(i, replacement);
                i--;
            }
        }
    }

    private List<ASTNode> applyIfClause(IfClause ifClause) {
        Literal condition = evaluateExpression(ifClause.conditionalExpression);
        boolean isTrue = condition instanceof BoolLiteral && ((BoolLiteral) condition).value;

        if (isTrue) {
            applyBodyNodes(ifClause.body);
            return ifClause.body;
        } else if (ifClause.elseClause != null) {
            applyBodyNodes(ifClause.elseClause.body);
            return ifClause.elseClause.body;
        } else {
            return new ArrayList<>();
        }
    }

    private void applyDeclaration(Declaration declaration) {
        declaration.expression = evaluateExpression(declaration.expression);
    }

    private void applyVariableAssignment(VariableAssignment assignment) {
        Literal literal = evaluateExpression(assignment.expression);
        variableValues.getFirst().put(assignment.name.name, literal);
    }

    private Literal evaluateExpression(Expression expression) {
        if (expression instanceof Literal) {
            return (Literal) expression;
        } else if (expression instanceof VariableReference) {
            return evaluateVariableReference((VariableReference) expression);
        } else if (expression instanceof MultiplyOperation) {
            return evaluateMultiplyOperation((MultiplyOperation) expression);
        } else if (expression instanceof AddOperation) {
            return evaluateAddSubtract((AddOperation) expression, true);
        } else if (expression instanceof SubtractOperation) {
            return evaluateAddSubtract((SubtractOperation) expression, false);
        }
        return null;
    }

    private Literal evaluateMultiplyOperation(MultiplyOperation operation) {
        Literal left  = evaluateExpression(operation.lhs);
        Literal right = evaluateExpression(operation.rhs);

        // Zorg dat scalar altijd rechts staat voor makkelijkere afhandeling
        if (left instanceof ScalarLiteral && !(right instanceof ScalarLiteral)) {
            Literal temp = left;
            left = right;
            right = temp;
        }

        if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
            return new ScalarLiteral(((ScalarLiteral) left).value * ((ScalarLiteral) right).value);
        } else if (left instanceof PixelLiteral && right instanceof ScalarLiteral) {
            return new PixelLiteral(((PixelLiteral) left).value * ((ScalarLiteral) right).value);
        } else if (left instanceof PercentageLiteral && right instanceof ScalarLiteral) {
            return new PercentageLiteral(((PercentageLiteral) left).value * ((ScalarLiteral) right).value);
        }
        return null;
    }

    private Literal evaluateAddSubtract(Operation operation, boolean isAdd) {
        Literal left  = evaluateExpression(operation.lhs);
        Literal right = evaluateExpression(operation.rhs);
        int sign = isAdd ? 1 : -1;

        if (left instanceof PixelLiteral && right instanceof PixelLiteral) {
            return new PixelLiteral(((PixelLiteral) left).value + sign * ((PixelLiteral) right).value);
        } else if (left instanceof PercentageLiteral && right instanceof PercentageLiteral) {
            return new PercentageLiteral(((PercentageLiteral) left).value + sign * ((PercentageLiteral) right).value);
        } else if (left instanceof ScalarLiteral && right instanceof ScalarLiteral) {
            return new ScalarLiteral(((ScalarLiteral) left).value + sign * ((ScalarLiteral) right).value);
        }
        return null;
    }

    private Literal evaluateVariableReference(VariableReference reference) {
        for (HashMap<String, Literal> scope : variableValues) {
            if (scope.containsKey(reference.name)) {
                return scope.get(reference.name);
            }
        }
        return null;
    }
}