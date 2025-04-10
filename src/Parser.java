import java.util.*;

public class Parser {
    private List<Token> tokens;
    private int position;
    public Map<String, Object> symbolTable;
    public Map<String, String> variableTypes;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
        this.symbolTable = new HashMap<>();
        this.variableTypes = new HashMap<>();
    }

    public void parse() {
        while (position < tokens.size()) {
            Token token = tokens.get(position);

            switch (token.type) {
                case KEYWORD:
                    switch (token.value) {
                        case "SUGOD":
                            position++; // Start of the program
                            break;
                        case "MUGNA":
                            parseVariableDeclaration();
                            break;
                        case "IPAKITA":
                            parsePrintStatement();
                            break;
                        case "KATAPUSAN":
                            return; // End execution
                        default:
                            ErrorHandler.handleUnexpectedKeyword(token.value);
                    }
                    break;
                case IDENTIFIER:
                    parseAssignment(); // Handle variable assignments
                    break;
                default:
                    ErrorHandler.handleUnexpectedToken(token.type, token.value);
            }
        }
    }

    private void parseVariableDeclaration() {
        position++; // Skip 'MUGNA'
        if (position >= tokens.size()) ErrorHandler.handleExpectedTypeAfterKeyword("MUGNA");

        Token typeToken = tokens.get(position);
        String varType = typeToken.value;
        position++;

        boolean moreVariables = true;
        while (moreVariables) {
            if (position >= tokens.size()) ErrorHandler.handleExpectedIdentifier();

            Token identifier = tokens.get(position);
            if (identifier.type != TokenType.IDENTIFIER) ErrorHandler.handleExpectedIdentifier();
            String varName = identifier.value;
            position++;

            // Set default value based on variable type
            Object value;
            if (varType.equals("NUMERO") || varType.equals("TIPIK")) {
                value = 0.0;
            } else if (varType.equals("TINUOD")) {
                value = false; // Default to false
            } else {
                value = "";
            }

            // If there's an assignment, parse the right-hand side
            if (position < tokens.size() && tokens.get(position).value.equals("=")) {
                position++; // Skip '='

                if (varType.equals("TINUOD")) {
                    value = parseBooleanExpression();
                } else if (varType.equals("LETRA")) {
                    value = parseCharacterExpression();
                } else { // NUMERO or TIPIK
                    value = parseNumericExpression();
                }
            }

            // Add the variable to the symbol table
            symbolTable.put(varName, value);
            variableTypes.put(varName, varType);

            // Check if there are more variables in the declaration
            moreVariables = position < tokens.size() && tokens.get(position).type == TokenType.COMMA;
            if (moreVariables) {
                position++;
            }
        }
    }

    private void parseAssignment() {
        List<String> varNames = new ArrayList<>();

        Token identifier = tokens.get(position);
        if (identifier.type != TokenType.IDENTIFIER) {
            ErrorHandler.handleExpectedIdentifier();
        }

        String varName = identifier.value;
        if (!variableTypes.containsKey(varName)) {
            ErrorHandler.handleUndefinedVariable(varName);
        }

        varNames.add(varName);
        position++;

        // Check for equals sign
        if (position >= tokens.size() || !tokens.get(position).value.equals("=")) {
            ErrorHandler.handleExpectedEqualsAfterIdentifier();
        }
        position++; // Skip '='

        // Check for chained assignment
        if (position < tokens.size() && tokens.get(position).type == TokenType.IDENTIFIER) {
            int tempPosition = position;
            String nextVarName = tokens.get(tempPosition).value;

            if (variableTypes.containsKey(nextVarName) &&
                    tempPosition + 1 < tokens.size() &&
                    tokens.get(tempPosition + 1).value.equals("=")) {
                parseAssignment();

                if (!symbolTable.containsKey(nextVarName)) {
                    ErrorHandler.handleVariableHasNoValue(nextVarName);
                }

                Object value = symbolTable.get(nextVarName);
                String currentType = variableTypes.get(varName);
                String nextType = variableTypes.get(nextVarName);

                if (!currentType.equals(nextType)) {
                    ErrorHandler.handleTypeMismatchInAssignment();
                }

                symbolTable.put(varName, value);
                return;
            }
        }

        String varType = variableTypes.get(varName);
        Object value;

        if (varType.equals("TINUOD")) {
            value = parseBooleanExpression();
        } else if (varType.equals("LETRA")) {
            value = parseCharacterExpression();
        } else {
            value = parseNumericExpression();
        }

        symbolTable.put(varName, value);
    }

    private Object parseBooleanExpression() {
        Stack<Object> values = new Stack<>();
        Stack<String> operators = new Stack<>();
        Stack<String> valueTypes = new Stack<>();
        boolean expectOperand = true;
        int parenthesisCount = 0;

        while (position < tokens.size()) {
            Token token = tokens.get(position);

            if ((token.type == TokenType.KEYWORD || token.type == TokenType.COLON ||
                    (token.type == TokenType.COMMA && operators.isEmpty())) && parenthesisCount == 0) {
                break;
            }

            if (expectOperand) {
                if (token.type == TokenType.TINUOD) {
                    if (token.value.equals("OO")) {
                        values.push(true);
                        valueTypes.push("TINUOD");
                    } else if (token.value.equals("DILI")) {
                        values.push(false);
                        valueTypes.push("TINUOD");
                    } else {
                        ErrorHandler.handleInvalidBooleanLiteral(token.value);
                    }
                    expectOperand = false;
                } else if (token.type == TokenType.LETRA) {
                    if (token.value.equals("OO")) {
                        values.push(true);
                        valueTypes.push("TINUOD");
                    } else if (token.value.equals("DILI")) {
                        values.push(false);
                        valueTypes.push("TINUOD");
                    } else {
                        ErrorHandler.handleInvalidBooleanLiteral(token.value);
                    }
                    expectOperand = false;
                } else if (token.type == TokenType.NUMERO || token.type == TokenType.TIPIK) {
                    values.push(Double.parseDouble(token.value));
                    valueTypes.push("NUMERO");
                    expectOperand = false;
                } else if (token.type == TokenType.IDENTIFIER) {
                    if (!symbolTable.containsKey(token.value)) {
                        ErrorHandler.handleUndefinedVariable(token.value);
                    }
                    Object value = symbolTable.get(token.value);
                    String type = variableTypes.get(token.value);

                    values.push(value);
                    valueTypes.push(type);
                    expectOperand = false;
                } else if (token.value.equals("DILI")) {
                    operators.push("DILI");
                    expectOperand = true;
                } else if (token.value.equals("(")) {
                    operators.push("(");
                    parenthesisCount++;
                    expectOperand = true;
                } else {
                    ErrorHandler.handleExpectedValue(token);
                }
            } else {
                if (token.value.equals(")")) {
                    while (!operators.isEmpty() && !operators.peek().equals("(")) {
                        processBooleanOperator(values, operators, valueTypes);
                    }

                    if (!operators.isEmpty() && operators.peek().equals("(")) {
                        operators.pop();
                        parenthesisCount--;
                    } else {
                        ErrorHandler.handleMismatchedParentheses();
                    }
                    expectOperand = false;
                } else if (token.value.equals("UG") || token.value.equals("O")) {
                    while (!operators.isEmpty() && !operators.peek().equals("(") &&
                            getBooleanPrecedence(operators.peek()) >= getBooleanPrecedence(token.value)) {
                        processBooleanOperator(values, operators, valueTypes);
                    }
                    operators.push(token.value);
                    expectOperand = true;
                } else if (token.type == TokenType.OPERATOR &&
                        (token.value.equals("<") || token.value.equals(">") ||
                                token.value.equals("<=") || token.value.equals(">=") ||
                                token.value.equals("==") || token.value.equals("<>"))) {
                    while (!operators.isEmpty() && !operators.peek().equals("(") &&
                            getBooleanPrecedence(operators.peek()) >= getBooleanPrecedence(token.value)) {
                        processBooleanOperator(values, operators, valueTypes);
                    }
                    operators.push(token.value);
                    expectOperand = true;
                } else {
                    break;
                }
            }
            position++;
        }

        while (!operators.isEmpty()) {
            if (operators.peek().equals("(")) {
                ErrorHandler.handleMismatchedParentheses();
            }
            processBooleanOperator(values, operators, valueTypes);
        }

        if (values.isEmpty()) {
            ErrorHandler.handleInvalidExpression("empty boolean expression");
        }

        Object result = values.pop();
        String resultType = valueTypes.pop();

        if (!resultType.equals("TINUOD")) {
            if (resultType.equals("NUMERO") || resultType.equals("TIPIK")) {
                return ((Number) result).doubleValue() != 0;
            } else {
                return !String.valueOf(result).isEmpty();
            }
        }

        return result;
    }

    private void processBooleanOperator(Stack<Object> values, Stack<String> operators, Stack<String> valueTypes) {
        String op = operators.pop();

        if (op.equals("DILI")) {
            if (values.isEmpty()) ErrorHandler.handleMissingOperandForNot();

            Object value = values.pop();
            String type = valueTypes.pop();
            boolean boolValue;

            if (type.equals("TINUOD")) {
                boolValue = (Boolean) value;
            } else if (type.equals("NUMERO") || type.equals("TIPIK")) {
                boolValue = ((Number) value).doubleValue() != 0;
            } else {
                boolValue = !String.valueOf(value).isEmpty();
            }

            values.push(!boolValue);
            valueTypes.push("TINUOD");
        }
        else if (op.equals("UG") || op.equals("O")) {
            if (values.size() < 2) ErrorHandler.handleInsufficientOperandsForOperator(op);

            Object rightVal = values.pop();
            String rightType = valueTypes.pop();
            Object leftVal = values.pop();
            String leftType = valueTypes.pop();

            boolean rightBool, leftBool;

            if (rightType.equals("TINUOD")) {
                rightBool = (Boolean) rightVal;
            } else if (rightType.equals("NUMERO") || rightType.equals("TIPIK")) {
                rightBool = ((Number) rightVal).doubleValue() != 0;
            } else {
                rightBool = !String.valueOf(rightVal).isEmpty();
            }

            if (leftType.equals("TINUOD")) {
                leftBool = (Boolean) leftVal;
            } else if (leftType.equals("NUMERO") || leftType.equals("TIPIK")) {
                leftBool = ((Number) leftVal).doubleValue() != 0;
            } else {
                leftBool = !String.valueOf(leftVal).isEmpty();
            }

            boolean result;
            if (op.equals("UG")) {
                result = leftBool && rightBool;
            } else {
                result = leftBool || rightBool;
            }

            values.push(result);
            valueTypes.push("TINUOD");
        }
        else if (op.equals("<") || op.equals(">") || op.equals("<=") || op.equals(">=") ||
                op.equals("==") || op.equals("<>")) {
            if (values.size() < 2) ErrorHandler.handleInsufficientOperandsForOperator(op);

            Object rightVal = values.pop();
            String rightType = valueTypes.pop();
            Object leftVal = values.pop();
            String leftType = valueTypes.pop();

            boolean result = false;

            if ((leftType.equals("NUMERO") || leftType.equals("TIPIK")) &&
                    (rightType.equals("NUMERO") || rightType.equals("TIPIK"))) {
                double leftNum = ((Number) leftVal).doubleValue();
                double rightNum = ((Number) rightVal).doubleValue();

                switch (op) {
                    case "<":  result = leftNum < rightNum; break;
                    case ">":  result = leftNum > rightNum; break;
                    case "<=": result = leftNum <= rightNum; break;
                    case ">=": result = leftNum >= rightNum; break;
                    case "==": result = leftNum == rightNum; break;
                    case "<>": result = leftNum != rightNum; break;
                }
            }
            else if (leftType.equals("TINUOD") && rightType.equals("TINUOD")) {
                boolean leftBool = (Boolean) leftVal;
                boolean rightBool = (Boolean) rightVal;

                if (op.equals("==")) {
                    result = leftBool == rightBool;
                } else if (op.equals("<>")) {
                    result = leftBool != rightBool;
                } else {
                    ErrorHandler.handleInvalidOperatorForBooleanComparison(op);
                }
            }
            else {
                try {
                    double leftNum, rightNum;

                    if (leftVal instanceof Number) {
                        leftNum = ((Number) leftVal).doubleValue();
                    } else if (leftVal instanceof Boolean) {
                        leftNum = ((Boolean) leftVal) ? 1.0 : 0.0;
                    } else {
                        leftNum = Double.parseDouble(String.valueOf(leftVal));
                    }

                    if (rightVal instanceof Number) {
                        rightNum = ((Number) rightVal).doubleValue();
                    } else if (rightVal instanceof Boolean) {
                        rightNum = ((Boolean) rightVal) ? 1.0 : 0.0;
                    } else {
                        rightNum = Double.parseDouble(String.valueOf(rightVal));
                    }

                    switch (op) {
                        case "<":  result = leftNum < rightNum; break;
                        case ">":  result = leftNum > rightNum; break;
                        case "<=": result = leftNum <= rightNum; break;
                        case ">=": result = leftNum >= rightNum; break;
                        case "==": result = leftNum == rightNum; break;
                        case "<>": result = leftNum != rightNum; break;
                    }
                } catch (NumberFormatException e) {
                    String leftStr = String.valueOf(leftVal);
                    String rightStr = String.valueOf(rightVal);

                    switch (op) {
                        case "<":  result = leftStr.compareTo(rightStr) < 0; break;
                        case ">":  result = leftStr.compareTo(rightStr) > 0; break;
                        case "<=": result = leftStr.compareTo(rightStr) <= 0; break;
                        case ">=": result = leftStr.compareTo(rightStr) >= 0; break;
                        case "==": result = leftStr.equals(rightStr); break;
                        case "<>": result = !leftStr.equals(rightStr); break;
                    }
                }
            }

            values.push(result);
            valueTypes.push("TINUOD");
        }
    }

    private Object parseCharacterExpression() {
        if (position >= tokens.size()) {
            ErrorHandler.handleInvalidCharacterExpression();
        }

        Token token = tokens.get(position);
        position++;

        if (token.type == TokenType.LETRA) {
            return token.value;
        } else if (token.type == TokenType.IDENTIFIER) {
            if (!symbolTable.containsKey(token.value)) {
                ErrorHandler.handleUndefinedVariable(token.value);
            }
            Object value = symbolTable.get(token.value);
            if (!(value instanceof String)) {
                ErrorHandler.handleTypeMismatchExpectedCharacter();
            }
            return value;
        }

        ErrorHandler.handleInvalidCharacterExpression();
        return ""; // This line will never be reached
    }

    private Object parseNumericExpression() {
        Stack<Double> values = new Stack<>();
        Stack<String> operators = new Stack<>();
        boolean expectOperand = true;
        int parenthesisCount = 0;

        while (position < tokens.size()) {
            Token token = tokens.get(position);

            if ((token.type == TokenType.KEYWORD || token.type == TokenType.COLON ||
                    token.type == TokenType.COMMA) && parenthesisCount == 0) {
                break;
            }

            if (expectOperand) {
                if (token.type == TokenType.NUMERO || token.type == TokenType.TIPIK) {
                    values.push(Double.parseDouble(token.value));
                    expectOperand = false;
                }
                else if (token.type == TokenType.IDENTIFIER) {
                    if (!symbolTable.containsKey(token.value)) {
                        ErrorHandler.handleUndefinedVariable(token.value);
                    }
                    Object val = symbolTable.get(token.value);
                    if (val instanceof Double) {
                        values.push((Double) val);
                    } else if (val instanceof Boolean) {
                        values.push(((Boolean) val) ? 1.0 : 0.0);
                    } else if (val instanceof String) {
                        try {
                            values.push(Double.parseDouble((String) val));
                        } catch (NumberFormatException e) {
                            ErrorHandler.handleCannotConvertStringToNumber(String.valueOf(val));
                        }
                    } else {
                        ErrorHandler.handleTypeMismatchExpectedNumber(val.getClass().getSimpleName());
                    }
                    expectOperand = false;
                }
                else if (token.value.equals("(") || token.value.equals("[")) {
                    operators.push(token.value);
                    parenthesisCount++;
                    expectOperand = true;
                }
                else if (token.value.equals("-") && expectOperand) {
                    operators.push("unary-");
                    expectOperand = true;
                }
                else {
                    ErrorHandler.handleExpectedNumberOrParenthesis(token);
                }
            }
            else {
                if (token.value.equals(")") || token.value.equals("]")) {
                    String matchingOpen = token.value.equals(")") ? "(" : "[";
                    while (!operators.isEmpty() && !operators.peek().equals(matchingOpen)) {
                        processOperator(values, operators);
                    }
                    if (!operators.isEmpty() && operators.peek().equals(matchingOpen)) {
                        operators.pop();
                        parenthesisCount--;
                    } else {
                        ErrorHandler.handleMismatchedParentheses();
                    }
                    expectOperand = false;
                }
                else if (isValidOperator(token.value)) {
                    while (!operators.isEmpty() &&
                            !(operators.peek().equals("(") || operators.peek().equals("[")) &&
                            getOperatorPrecedence(operators.peek()) >= getOperatorPrecedence(token.value)) {
                        processOperator(values, operators);
                    }
                    operators.push(token.value);
                    expectOperand = true;
                }
                else {
                    break;
                }
            }
            position++;
        }

        while (!operators.isEmpty()) {
            if (operators.peek().equals("(") || operators.peek().equals("[")) {
                ErrorHandler.handleMismatchedParentheses();
            }
            processOperator(values, operators);
        }

        if (values.isEmpty()) {
            return 0.0;
        } else if (values.size() > 1) {
            ErrorHandler.handleInvalidExpressionTooManyOperands();
        }

        return values.pop();
    }

    private void processOperator(Stack<Double> values, Stack<String> operators) {
        String op = operators.pop();

        if (op.equals("unary-")) {
            if (values.isEmpty()) {
                ErrorHandler.handleInvalidExpression("missing operand for unary minus");
            }
            double val = values.pop();
            values.push(-val);
        } else {
            if (values.size() < 2) {
                ErrorHandler.handleInsufficientOperandsForOperator(op);
            }
            double b = values.pop();
            double a = values.pop();
            values.push(applyOperator(a, b, op));
        }
    }

    private void parsePrintStatement() {
        position++; // Skip 'IPAKITA'
        if (position >= tokens.size() || !tokens.get(position).type.equals(TokenType.COLON)) {
            ErrorHandler.handleExpectedColonAfterKeyword("IPAKITA");
        }
        position++;

        StringBuilder output = new StringBuilder();

        while (position < tokens.size()) {
            Token token = tokens.get(position);

            if (token.type == TokenType.KEYWORD) break;

            if (token.type == TokenType.IDENTIFIER) {
                if (!symbolTable.containsKey(token.value)) {
                    ErrorHandler.handleUndefinedVariable(token.value);
                }
                Object value = symbolTable.get(token.value);
                String varType = variableTypes.get(token.value);

                if (varType.equals("NUMERO")) {
                    output.append(((Double) value).intValue());
                } else if (varType.equals("TIPIK")) {
                    output.append(value);
                } else if (varType.equals("TINUOD")) {
                    output.append((Boolean) value ? "OO" : "DILI");
                } else {
                    output.append(value);
                }
            } else if (token.type == TokenType.LETRA ||
                    token.type == TokenType.NUMERO ||
                    token.type == TokenType.TIPIK) {
                output.append(token.value);
            } else if (token.type == TokenType.OPERATOR) {
                if (token.value.equals("$")) {
                    System.out.println(output.toString());
                    output = new StringBuilder();
                } else if (token.value.equals("&")) {
                    // Do nothing for concatenation
                } else {
                    output.append(token.value);
                }
            } else {
                break;
            }
            position++;
        }

        String result = output.toString().trim();

        while(result.contains("[]")) {
            int index = result.indexOf("[]");
            if (index > 0) {
                result = result.substring(0, index) + result.substring(index + 2);
            }
        }

        if (!result.startsWith("[") && !result.endsWith("]") && result.length() == 1) {
            result = "[" + result + "]";
        }

        if (result.startsWith("[]-")) {
            result = "[-" + result.substring(3);
        }

        if (result.contains("[") && !result.contains("]")) {
            result += "]";
        }

        if (!result.isEmpty()) {
            System.out.println(result);
        }
    }

    // Helper methods remain unchanged
    private int getBooleanPrecedence(String operator) {
        if (operator.equals("DILI")) return 5;
        if (operator.equals("<") || operator.equals(">") ||
                operator.equals("<=") || operator.equals(">=") ||
                operator.equals("==") || operator.equals("<>")) return 4;
        if (operator.equals("UG")) return 3;
        if (operator.equals("OO")) return 2;
        return 0;
    }

    private int getOperatorPrecedence(String operator) {
        if (operator.equals("unary-")) return 3;
        if (operator.equals("*") || operator.equals("/")) return 2;
        if (operator.equals("+") || operator.equals("-")) return 1;
        return 0;
    }

    private boolean isValidOperator(String operator) {
        return operator.equals("+") || operator.equals("-") || operator.equals("*") || operator.equals("/");
    }

    private double applyOperator(double a, double b, String operator) {
        switch (operator) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/":
                if (b == 0) ErrorHandler.handleDivisionByZero();
                return a / b;
            default:
                ErrorHandler.handleUnknownOperator(operator);
                return 0; // This line will never be reached
        }
    }
}