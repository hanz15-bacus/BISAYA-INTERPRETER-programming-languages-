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
        position++; // Move past 'MUGNA'
        if (position >= tokens.size()) throw new RuntimeException("Expected type after MUGNA");

        Token typeToken = tokens.get(position);
        String varType = typeToken.value;
        position++;

        // Process all variables of this type (can be comma-separated)
        boolean moreVariables = true;

        while (moreVariables) {
            if (position >= tokens.size()) throw new RuntimeException("Expected identifier after type");

            Token identifier = tokens.get(position);
            if (identifier.type != TokenType.IDENTIFIER) throw new RuntimeException("Expected identifier");
            String varName = identifier.value;
            position++;

            // Set default values based on type
            Object value = null;
            if (varType.equals("NUMERO")) value = 0.0;
            else if (varType.equals("TIPIK")) value = 0.0;
            else if (varType.equals("TINUOD")) value = false;
            else if (varType.equals("LETRA")) value = "";

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

            moreVariables = position < tokens.size() &&
                    tokens.get(position).type == TokenType.COMMA;
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

            // Stop parsing when a keyword or end token is reached
            if (token.type == TokenType.KEYWORD || token.type == TokenType.COLON) {
                break;
            }

            if (expectOperand) {
                if (token.type == TokenType.NUMERO || token.type == TokenType.TIPIK) {
                    values.push(Double.parseDouble(token.value));
                    expectOperand = false;
                } else if (token.type == TokenType.IDENTIFIER) {
                    if (!symbolTable.containsKey(token.value)) {
                        throw new RuntimeException("Undefined variable: " + token.value);
                    }
                    Object val = symbolTable.get(token.value);
                    if (!(val instanceof Double)) {
                        throw new RuntimeException("Type mismatch: expected number, got " + val.getClass().getSimpleName());
                    }
                    values.push((Double) val);
                    expectOperand = false;
                } else {
                    throw new RuntimeException("Expected number or variable, but found: " + token.value);
                }
            } else {
                if (token.type == TokenType.OPERATOR) {
                    if (isValidOperator(token.value)) {
                        operators.push(token.value);
                    } else {
                        throw new RuntimeException("Unsupported operator: " + token.value);
                    }
                    expectOperand = true;
                } else {
                    break;
                }
            }

            position++;
        }

        // Apply operators with precedence
        while (!operators.isEmpty()) {
            if (values.size() < 2) {
                throw new RuntimeException("Invalid expression: insufficient operands");
            }

            double b = values.pop();
            double a = values.pop();
            String op = operators.pop();
            values.push(applyOperator(a, b, op));
        }

        // Return the evaluated result
        if (values.isEmpty()) {
            return 0.0;
        }

        return values.pop();
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
                    // Display as whole number (integer)
                    System.out.print(((Double) value).intValue());
                } else if (varType.equals("TIPIK")) {
                    // Display as decimal
                    System.out.print(value);
                } else if (value instanceof Boolean) {
                    System.out.print((Boolean) value ? "OO" : "DILI");
                } else {
                    System.out.print(value);
                }
            } else if (token.type == TokenType.LETRA ||
                    token.type == TokenType.NUMERO ||
                    token.type == TokenType.TIPIK) {
                System.out.print(token.value);
            } else if (token.type == TokenType.OPERATOR) {
                if (token.value.equals("&")) {
                    // Do nothing, it's just a concatenation operator
                } else if (token.value.equals("$")) {
                    System.out.println();
                } else {
                    System.out.print(token.value);
                }
            } else {
                break;
            }
            position++;
        }
        System.out.println();
    }

}