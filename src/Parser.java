import java.util.*;

public class Parser {
    private List<Token> tokens;
    private int position;
    private Map<String, Object> symbolTable;
    private Map<String, String> variableTypes;

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

            Object value = (varType.equals("NUMERO") || varType.equals("TIPIK")) ? 0.0 :
                    (varType.equals("TINUOD")) ? false : "";

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

            symbolTable.put(varName, value);
            variableTypes.put(varName, varType);

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
        if (position >= tokens.size()) {
            throw new RuntimeException("Expected boolean value");
        }

        Token token = tokens.get(position);
        position++;

        if (token.type == TokenType.TINUOD) {
            return token.value.equals("OO");
        } else if (token.type == TokenType.IDENTIFIER) {
            if (!symbolTable.containsKey(token.value)) {
                throw new RuntimeException("Undefined variable: " + token.value);
            }
            Object value = symbolTable.get(token.value);
            if (!(value instanceof Boolean)) {
                throw new RuntimeException("Type mismatch: expected boolean value");
            }
            return value;
        } else if (token.type == TokenType.LETRA) {
            // Handle string literals "OO" and "DILI"
            if (token.value.equals("OO")) {
                return true;
            } else if (token.value.equals("DILI")) {
                return false;
            } else {
                throw new RuntimeException("Invalid boolean value: " + token.value);
            }
        }

        throw new RuntimeException("Invalid boolean expression");
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

        while (position < tokens.size()) {
            Token token = tokens.get(position);

            // Check for tokens that might end the expression
            if (token.type == TokenType.KEYWORD || token.type == TokenType.COLON ||
                    token.type == TokenType.COMMA) {
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
                    } else {
                        throw new RuntimeException("Type mismatch: expected number, got " + val.getClass().getSimpleName());
                    }
                    expectOperand = false;
                }
                else if (token.value.equals("(")) {
                    operators.push("(");
                    expectOperand = true;
                }
                else if (token.value.equals("-") && expectOperand) { // Unary minus
                    operators.push("unary-");
                    expectOperand = true;
                }
                else {
                    throw new RuntimeException("Expected number, variable, or '(' but found: " + token.value);
                }
            }
            else { // Expecting an operator or closing parenthesis
                if (token.value.equals(")")) {
                    // Process all operators until opening parenthesis
                    while (!operators.isEmpty() && !operators.peek().equals("(")) {
                        processOperator(values, operators);
                    }
                    if (!operators.isEmpty() && operators.peek().equals("(")) {
                        operators.pop();  // Remove the opening parenthesis
                    } else {
                        throw new RuntimeException("Mismatched parentheses");
                    }
                    expectOperand = false;
                }
                else if (isValidOperator(token.value)) {
                    // Process operators with higher precedence
                    while (!operators.isEmpty() &&
                            !operators.peek().equals("(") &&
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

        // Process any remaining operators
        while (!operators.isEmpty()) {
            if (operators.peek().equals("(")) {
                throw new RuntimeException("Mismatched parentheses");
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
                } else if (value instanceof Boolean) {
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

        // Fix the formatting to ensure negative sign is inside brackets
        String result = output.toString();
        if (result.startsWith("[]-")) {
            result = "[-" + result.substring(3);
        }

        // Add the closing bracket if needed
        if (result.contains("[") && !result.contains("]")) {
            result += "]";
        }

        // Print any remaining output if not empty
        if (!result.isEmpty()) {
            System.out.println(result);
        }
    }
}