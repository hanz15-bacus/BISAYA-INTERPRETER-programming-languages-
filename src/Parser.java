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
                            throw new RuntimeException("Unexpected keyword: " + token.value);
                    }
                    break;
                case IDENTIFIER:
                    parseAssignment(); // Handle variable assignments
                    break;
                default:
                    throw new RuntimeException("Unexpected token: " + token.type + " with value: " + token.value);
            }
        }
    }

    private void parseVariableDeclaration() {
        position++; // Skip 'MUGNA'
        if (position >= tokens.size()) throw new RuntimeException("Expected type after MUGNA");

        Token typeToken = tokens.get(position);
        String varType = typeToken.value;
        position++;

        boolean moreVariables = true;
        while (moreVariables) {
            if (position >= tokens.size()) throw new RuntimeException("Expected identifier after type");

            Token identifier = tokens.get(position);
            if (identifier.type != TokenType.IDENTIFIER) throw new RuntimeException("Expected identifier");
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
            throw new RuntimeException("Expected identifier");
        }

        String varName = identifier.value;
        if (!variableTypes.containsKey(varName)) {
            throw new RuntimeException("Undefined variable: " + varName);
        }

        varNames.add(varName);
        position++;

        // Check for equals sign
        if (position >= tokens.size() || !tokens.get(position).value.equals("=")) {
            throw new RuntimeException("Expected '=' after identifier");
        }
        position++; // Skip '='

        // Check for chained assignment (another identifier followed by equals)
        if (position < tokens.size() && tokens.get(position).type == TokenType.IDENTIFIER) {
            int tempPosition = position;
            String nextVarName = tokens.get(tempPosition).value;

            // Check if this is a valid variable and has an equals sign after it
            if (variableTypes.containsKey(nextVarName) &&
                    tempPosition + 1 < tokens.size() &&
                    tokens.get(tempPosition + 1).value.equals("=")) {
                parseAssignment();

                if (!symbolTable.containsKey(nextVarName)) {
                    throw new RuntimeException("Variable has no value: " + nextVarName);
                }

                Object value = symbolTable.get(nextVarName);
                String currentType = variableTypes.get(varName);
                String nextType = variableTypes.get(nextVarName);

                if (!currentType.equals(nextType)) {
                    throw new RuntimeException("Type mismatch in assignment");
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

            // Stop parsing at keywords, colon, or comma (unless inside parentheses)
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
                        throw new RuntimeException("Invalid boolean literal: " + token.value);
                    }
                    expectOperand = false;
                } else if (token.type == TokenType.LETRA) {
                    // Handle quoted boolean literals
                    if (token.value.equals("OO")) {
                        values.push(true);
                        valueTypes.push("TINUOD");
                    } else if (token.value.equals("DILI")) {
                        values.push(false);
                        valueTypes.push("TINUOD");
                    } else {
                        throw new RuntimeException("Invalid boolean literal: " + token.value);
                    }
                    expectOperand = false;
                } else if (token.type == TokenType.NUMERO || token.type == TokenType.TIPIK) {
                    // Handle numeric literals in comparisons
                    values.push(Double.parseDouble(token.value));
                    valueTypes.push("NUMERO");
                    expectOperand = false;
                } else if (token.type == TokenType.IDENTIFIER) {
                    // Check if variable exists in the symbol table
                    if (!symbolTable.containsKey(token.value)) {
                        throw new RuntimeException("Undefined variable: " + token.value + ". Declare it before use.");
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
                    // Handle unexpected tokens
                    throw new RuntimeException("Expected value, but got: " + token.value +
                            " (Token type: " + token.type + ")");
                }
            } else {
                // Here we handle operators when we're not expecting an operand
                if (token.value.equals(")")) {
                    // Process all operators until we find the matching opening parenthesis
                    while (!operators.isEmpty() && !operators.peek().equals("(")) {
                        processBooleanOperator(values, operators, valueTypes);
                    }

                    // Remove the opening parenthesis
                    if (!operators.isEmpty() && operators.peek().equals("(")) {
                        operators.pop();
                        parenthesisCount--;
                    } else {
                        throw new RuntimeException("Mismatched parentheses");
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
                    // Handle comparison operators
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

        // Process any remaining operators
        while (!operators.isEmpty()) {
            if (operators.peek().equals("(")) {
                throw new RuntimeException("Mismatched parentheses");
            }
            processBooleanOperator(values, operators, valueTypes);
        }

        if (values.isEmpty()) {
            throw new RuntimeException("Invalid boolean expression");
        }

        // The final result should be a boolean value
        Object result = values.pop();
        String resultType = valueTypes.pop();

        // Convert the result to a boolean if it's not already
        if (!resultType.equals("TINUOD")) {
            if (resultType.equals("NUMERO") || resultType.equals("TIPIK")) {
                // Convert number to boolean (0 = false, anything else = true)
                return ((Number) result).doubleValue() != 0;
            } else {
                // Convert string to boolean (empty = false, non-empty = true)
                return !String.valueOf(result).isEmpty();
            }
        }

        return result;
    }

    private void processBooleanOperator(Stack<Object> values, Stack<String> operators, Stack<String> valueTypes) {
        String op = operators.pop();

        if (op.equals("DILI")) { // NOT operator
            if (values.isEmpty()) throw new RuntimeException("Missing operand for 'DILI'");

            Object value = values.pop();
            String type = valueTypes.pop();
            boolean boolValue;

            // Convert value to boolean based on type
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
        else if (op.equals("UG") || op.equals("O")) { // AND, OR operators
            if (values.size() < 2) throw new RuntimeException("Insufficient operands for operator " + op);

            Object rightVal = values.pop();
            String rightType = valueTypes.pop();
            Object leftVal = values.pop();
            String leftType = valueTypes.pop();

            // Convert both values to boolean
            boolean rightBool, leftBool;

            // Convert right operand to boolean
            if (rightType.equals("TINUOD")) {
                rightBool = (Boolean) rightVal;
            } else if (rightType.equals("NUMERO") || rightType.equals("TIPIK")) {
                rightBool = ((Number) rightVal).doubleValue() != 0;
            } else {
                rightBool = !String.valueOf(rightVal).isEmpty();
            }

            // Convert left operand to boolean
            if (leftType.equals("TINUOD")) {
                leftBool = (Boolean) leftVal;
            } else if (leftType.equals("NUMERO") || leftType.equals("TIPIK")) {
                leftBool = ((Number) leftVal).doubleValue() != 0;
            } else {
                leftBool = !String.valueOf(leftVal).isEmpty();
            }

            // Apply the logical operator
            boolean result;
            if (op.equals("UG")) {
                result = leftBool && rightBool;
            } else {  // op.equals("O")
                result = leftBool || rightBool;
            }

            values.push(result);
            valueTypes.push("TINUOD");
        }
        else if (op.equals("<") || op.equals(">") || op.equals("<=") || op.equals(">=") ||
                op.equals("==") || op.equals("<>")) {
            // Handle comparison operators
            if (values.size() < 2) throw new RuntimeException("Insufficient operands for comparison operator " + op);

            Object rightVal = values.pop();
            String rightType = valueTypes.pop();
            Object leftVal = values.pop();
            String leftType = valueTypes.pop();

            boolean result = false;

            // Numeric comparison
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
            // Boolean comparison
            else if (leftType.equals("TINUOD") && rightType.equals("TINUOD")) {
                boolean leftBool = (Boolean) leftVal;
                boolean rightBool = (Boolean) rightVal;

                if (op.equals("==")) {
                    result = leftBool == rightBool;
                } else if (op.equals("<>")) {
                    result = leftBool != rightBool;
                } else {
                    throw new RuntimeException("Invalid operator for boolean comparison: " + op);
                }
            }
            // Mixed or string comparison
            else {
                // First try to convert to numbers if possible
                try {
                    double leftNum, rightNum;

                    // Convert left value to number
                    if (leftVal instanceof Number) {
                        leftNum = ((Number) leftVal).doubleValue();
                    } else if (leftVal instanceof Boolean) {
                        leftNum = ((Boolean) leftVal) ? 1.0 : 0.0;
                    } else {
                        leftNum = Double.parseDouble(String.valueOf(leftVal));
                    }

                    // Convert right value to number
                    if (rightVal instanceof Number) {
                        rightNum = ((Number) rightVal).doubleValue();
                    } else if (rightVal instanceof Boolean) {
                        rightNum = ((Boolean) rightVal) ? 1.0 : 0.0;
                    } else {
                        rightNum = Double.parseDouble(String.valueOf(rightVal));
                    }

                    // Perform numeric comparison
                    switch (op) {
                        case "<":  result = leftNum < rightNum; break;
                        case ">":  result = leftNum > rightNum; break;
                        case "<=": result = leftNum <= rightNum; break;
                        case ">=": result = leftNum >= rightNum; break;
                        case "==": result = leftNum == rightNum; break;
                        case "<>": result = leftNum != rightNum; break;
                    }
                } catch (NumberFormatException e) {
                    // If numeric conversion fails, compare as strings
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


    private int getBooleanPrecedence(String operator) {
        if (operator.equals("DILI")) return 5;  // NOT (Highest precedence)

        // Comparison operators - higher precedence than logical operators
        if (operator.equals("<") || operator.equals(">") ||
                operator.equals("<=") || operator.equals(">=") ||
                operator.equals("==") || operator.equals("<>")) return 4;

        if (operator.equals("UG")) return 3;    // AND
        if (operator.equals("O")) return 2;     // OR
        return 0;
    }


    private Object parseCharacterExpression() {
        if (position >= tokens.size()) {
            throw new RuntimeException("Expected character value");
        }

        Token token = tokens.get(position);
        position++;

        if (token.type == TokenType.LETRA) {
            return token.value;
        } else if (token.type == TokenType.IDENTIFIER) {
            if (!symbolTable.containsKey(token.value)) {
                throw new RuntimeException("Undefined variable: " + token.value);
            }
            Object value = symbolTable.get(token.value);
            if (!(value instanceof String)) {
                throw new RuntimeException("Type mismatch: expected character value");
            }
            return value;
        }

        throw new RuntimeException("Invalid character expression");
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
                        throw new RuntimeException("Undefined variable in expression: " + token.value);
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
                            throw new RuntimeException("Cannot convert string to number: " + val);
                        }
                    } else {
                        throw new RuntimeException("Type mismatch: expected number, got " + val.getClass().getSimpleName());
                    }
                    expectOperand = false;
                }
                else if (token.value.equals("(") || token.value.equals("[")) {
                    operators.push(token.value);
                    parenthesisCount++;
                    expectOperand = true;
                }
                else if (token.value.equals("-") && expectOperand) { // Unary minus
                    operators.push("unary-");
                    expectOperand = true;
                }
                else {
                    throw new RuntimeException("Expected number, variable, or '(' or '[' but found: " + token.value);
                }
            }
            else { // Expecting an operator or closing parenthesis/bracket
                if (token.value.equals(")") || token.value.equals("]")) {
                    String matchingOpen = token.value.equals(")") ? "(" : "[";
                    while (!operators.isEmpty() && !operators.peek().equals(matchingOpen)) {
                        processOperator(values, operators);
                    }
                    if (!operators.isEmpty() && operators.peek().equals(matchingOpen)) {
                        operators.pop();
                        parenthesisCount--;
                    } else {
                        throw new RuntimeException("Mismatched parentheses or brackets");
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
                    break;  // End of numeric expression
                }
            }
            position++;
        }

        while (!operators.isEmpty()) {
            if (operators.peek().equals("(") || operators.peek().equals("[")) {
                throw new RuntimeException("Mismatched parentheses or brackets");
            }
            processOperator(values, operators);
        }

        if (values.isEmpty()) {
            return 0.0;
        } else if (values.size() > 1) {
            throw new RuntimeException("Invalid expression: too many operands");
        }

        return values.pop();
    }


    private void processOperator(Stack<Double> values, Stack<String> operators) {
        String op = operators.pop();

        if (op.equals("unary-")) {
            if (values.isEmpty()) {
                throw new RuntimeException("Invalid expression: missing operand for unary minus");
            }
            double val = values.pop();
            values.push(-val);
        } else {
            if (values.size() < 2) {
                throw new RuntimeException("Invalid expression: insufficient operands for operator " + op);
            }
            double b = values.pop();
            double a = values.pop();
            values.push(applyOperator(a, b, op));
        }
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
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
                if (b == 0) throw new RuntimeException("Division by zero");
                return a / b;
            default:
                throw new RuntimeException("Unknown operator: " + operator);
        }
    }

    private void parsePrintStatement() {
        position++; // Skip 'IPAKITA'
        if (position >= tokens.size() || !tokens.get(position).type.equals(TokenType.COLON)) {
            throw new RuntimeException("Expected ':' after IPAKITA");
        }
        position++;

        StringBuilder output = new StringBuilder();

        while (position < tokens.size()) {
            Token token = tokens.get(position);

            if (token.type == TokenType.KEYWORD) break; // End of print statement

            if (token.type == TokenType.IDENTIFIER) {
                if (!symbolTable.containsKey(token.value)) {
                    throw new RuntimeException("Undefined variable: " + token.value);
                }
                Object value = symbolTable.get(token.value);
                String varType = variableTypes.get(token.value);

                if (varType.equals("NUMERO")) {
                    output.append(((Double) value).intValue());
                } else if (varType.equals("TIPIK")) {
                    output.append(value);
                } else if (varType.equals("TINUOD")) {
                    // Make sure we output "OO" for true and "DILI" for false
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
                    // Print the current buffer and create a new line
                    System.out.println(output.toString());
                    output = new StringBuilder();
                } else if (token.value.equals("&")) {
                    // & is concatenation, but doesn't add any visible characters
                    // Do nothing
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
                // Remove the first occurrence of "[]" if it's NOT at the start
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
}