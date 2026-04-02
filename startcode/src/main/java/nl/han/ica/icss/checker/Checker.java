package nl.han.ica.icss.checker;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.HashMap;
import java.util.LinkedList;

public class Checker {

    private LinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new LinkedList<>();
        checkStylesheet(ast.root);
    }

    private void checkStylesheet(Stylesheet stylesheet) {
        variableTypes.push(new HashMap<>());
        for (ASTNode child : stylesheet.getChildren()) {
            if (child instanceof Stylerule) {
                checkStylerule((Stylerule) child);
            } else if (child instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) child);
            } else {
                child.setError("Unknown type");
            }
        }
        variableTypes.pop();
    }

    private void checkStylerule(Stylerule stylerule) {
        variableTypes.push(new HashMap<>());
        for (ASTNode child : stylerule.getChildren()) {
            checkBody(child);
        }
        variableTypes.pop();
    }


    private void checkBody(ASTNode body) {
        if (body instanceof VariableAssignment) {
            checkVariableAssignment((VariableAssignment) body);
        } else if (body instanceof Declaration) {
            checkDeclaration((Declaration) body);
        } else if (body instanceof IfClause) {
            checkIfClause((IfClause) body);
        }
    }

    private void checkVariableAssignment(VariableAssignment assignment) {
        ExpressionType type = getExpressionType(assignment.expression);
        variableTypes.getFirst().put(assignment.name.name, type);
    }

    private void checkDeclaration(Declaration declaration) {
        ExpressionType type = getExpressionType(declaration.expression);

        String prop = declaration.property.name;
        // Dit zorgt dat het type van de waarde klopt met de property
        if (prop.equals("width") || prop.equals("height") || prop.equals("font-size")) {
            if (type != ExpressionType.PIXEL && type != ExpressionType.PERCENTAGE) {
                declaration.setError("Property expected pixel of percentage value");
            }
        } else if (prop.equals("color") || prop.equals("background-color")) {
            if (type != ExpressionType.COLOR) {
                declaration.setError("Property expected color value");
            }
        }
    }

    private void checkIfClause(IfClause ifClause) {
        ExpressionType conditionType = getExpressionType(ifClause.conditionalExpression);
        // Dit zorgt dat de conditie van een if-statement altijd een boolean is
        if (conditionType != ExpressionType.BOOL) {
            ifClause.setError("If-condition must be of boolean type");
        }

        variableTypes.push(new HashMap<>());
        for (ASTNode child : ifClause.body) {
            checkBody(child);
        }
        variableTypes.pop();

        if (ifClause.elseClause != null) {
            checkElseClause(ifClause.elseClause);
        }
    }

    private void checkElseClause(ElseClause elseClause) {
        variableTypes.push(new HashMap<>());
        for (ASTNode child : elseClause.body) {
            checkBody(child);
        }
        variableTypes.pop();
    }

    private ExpressionType getExpressionType(Expression expression) {
        if (expression instanceof Literal) {
            return getLiteralType((Literal) expression);
        }

        if (expression instanceof VariableReference) {
            VariableReference ref = (VariableReference) expression;
            ExpressionType type = lookupVariable(ref.name);
            if (type == null) {
                ref.setError("Variable not defined");
                return ExpressionType.UNDEFINED;
            }
            return type;
        }

        if (expression instanceof Operation) {
            return checkOperation((Operation) expression);
        }

        return ExpressionType.UNDEFINED;
    }

    private ExpressionType checkOperation(Operation operation) {
        ExpressionType left  = getExpressionType(operation.lhs);
        ExpressionType right = getExpressionType(operation.rhs);

        // Checkt of er ene kleur is gebruikt in een operation, dat mag niet
        if (left == ExpressionType.COLOR || right == ExpressionType.COLOR) {
            operation.setError("Color literals are not allowed in operations");
            return ExpressionType.UNDEFINED;
        }
        // Checkt of er een boolean is gebruikt in een operation, dat mag niet
        if (left == ExpressionType.BOOL || right == ExpressionType.BOOL) {
            operation.setError("Boolean literals are not allowed in operations");
            return ExpressionType.UNDEFINED;
        }

        if (operation instanceof MultiplyOperation) {
            // Checkt of er minimaal één SCALAR is in een multiply operation
            if (left != ExpressionType.SCALAR && right != ExpressionType.SCALAR) {
                operation.setError("Multiply operation requires at least one scalar operand");
                return ExpressionType.UNDEFINED;
            }
            return left == ExpressionType.SCALAR ? right : left;
        }

        if (operation instanceof AddOperation || operation instanceof SubtractOperation) {
            // Checkt of de types hetzelfde zijn bij een plus/min som
            if (left != right) {
                operation.setError("Operands of add/subtract must be of the same type");
                return ExpressionType.UNDEFINED;
            }
            return left;
        }

        operation.setError("Unknown operation");
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType getLiteralType(Literal literal) {
        if (literal instanceof BoolLiteral)       return ExpressionType.BOOL;
        if (literal instanceof ColorLiteral)      return ExpressionType.COLOR;
        if (literal instanceof ScalarLiteral)     return ExpressionType.SCALAR;
        if (literal instanceof PixelLiteral)      return ExpressionType.PIXEL;
        if (literal instanceof PercentageLiteral) return ExpressionType.PERCENTAGE;
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType lookupVariable(String name) {
        for (int i = 0; i < variableTypes.size(); i++) {
            HashMap<String, ExpressionType> scope = variableTypes.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null;
    }
}