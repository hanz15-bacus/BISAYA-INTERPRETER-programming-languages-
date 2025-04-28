package parser;

import lexer.Token;
import lexer.TokenType;

import java.util.*;
import java.util.Scanner;
import ErrorHandler.ErrorHandler;

public class Parser {
    private List<Token> tokens;
    private int position;
    public Map<String, Object> symbolTable;
    public Map<String, String> variableTypes;
    private Scanner scanner;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
        this.symbolTable = new HashMap<>();
        this.variableTypes = new HashMap<>();
        this.scanner = new Scanner(System.in);
    }

    public void parse() {
        while (position < tokens.size()) {
            Token token = tokens.get(position);

            switch (token.type) {
                case KEYWORD:
                    switch (token.value) {
                        case "SUGOD":
                            position++;
                            break;
                        case "MUGNA":
                            parseVariableDeclaration();
                            break;
                        case "IPAKITA":
                            parsePrintStatement();
                            break;
                        case "DAWAT":
                            parseInputStatement();
                            break;
                        case "KUNG":
                            parseConditionalStatement();
                            break;
                        case "ALANG SA":
                            parseForLoop();
                            break;
                        case "KATAPUSAN":
                            position++; // Skip KATAPUSAN and proceed
                            return;     // Or return to end parsing
                        default:
                            ErrorHandler.handleUnexpectedKeyword(token.value);
                    }
                    break;
                case IDENTIFIER:
                    parseAssignment();
                    break;
                default:
                    ErrorHandler.handleUnexpectedToken(token.type, token.value);
            }
        }
    }



    private boolean matchAndConsume(String expected) {
        if (position < tokens.size() && tokens.get(position).value.equals(expected)) {
            position++;
            return true;
        }
        return false;
    }

    private boolean matchAndConsumeParen(char paren) {
        if (position < tokens.size()) {
            Token token = tokens.get(position);
            // More robust check for different types of parentheses
            if (paren == '(' && (token.type == TokenType.LPAREN ||
                    token.value.equals("(") ||
                    token.value.equals("\u0028"))) {
                position++;
                return true;
            } else if (paren == ')' && (token.type == TokenType.RPAREN ||
                    token.value.equals(")") ||
                    token.value.equals("\u0029"))) {
                position++;
                return true;
            }
        }
        return false;
    }




    private boolean matchAndConsumeBrace(char brace) {
        if (position < tokens.size()) {
            Token token = tokens.get(position);
            if ((brace == '{' && token.type == TokenType.LEFTBRACE) ||
                    (brace == '}' && token.type == TokenType.RIGHTBRACE)) {
                position++;
                return true;
            }
        }
        return false;
    }


    private void parseConditionalStatement() {
        boolean conditionExecuted = false;

        // Expect 'KUNG'
        if (!matchAndConsume("KUNG")) {
            ErrorHandler.handleExpectedKungKeyword();
            return;
        }

        // Check for opening parenthesis
        if (position < tokens.size() &&
                (tokens.get(position).type == TokenType.LPAREN || tokens.get(position).value.equals("("))) {
            position++; // Manually consume the opening parenthesis
        } else {
            ErrorHandler.handleExpectedParenthesisAfterKung();
            return;
        }

        // Parse the condition
        Object condition = parseBooleanExpression();

        // Check for closing parenthesis
        if (position < tokens.size() &&
                (tokens.get(position).type == TokenType.RPAREN || tokens.get(position).value.equals(")"))) {
            position++; // Manually consume the closing parenthesis
        } else {
            ErrorHandler.handleExpectedClosingParenthesis();
            return;
        }

        if (!matchAndConsume("PUNDOK") || !matchAndConsumeBrace('{')) {
            ErrorHandler.handleExpectedPundokBlock();
            return;
        }

        boolean conditionValue = convertToBoolean(condition);

        if (conditionValue) {
            executeBlock();
            conditionExecuted = true;
        } else {
            skipBlock();
        }

        // Handle zero or more 'KUNG DILI'
        while (position < tokens.size()) {
            if (matchAndConsume("KUNG")) {
                if (matchAndConsume("DILI")) {
                    if (!matchAndConsumeParen('(')) {
                        ErrorHandler.handleExpectedParenthesisAfterKungDili();
                        return;
                    }

                    Object nextCondition = parseBooleanExpression();

                    if (!matchAndConsumeParen(')')) {
                        ErrorHandler.handleExpectedClosingParenthesis();
                        return;
                    }

                    if (!matchAndConsume("PUNDOK") || !matchAndConsumeBrace('{')) {
                        ErrorHandler.handleExpectedPundokBlock();
                        return;
                    }

                    boolean nextConditionValue = convertToBoolean(nextCondition);

                    if (!conditionExecuted && nextConditionValue) {
                        executeBlock();
                        conditionExecuted = true;
                    } else {
                        skipBlock();
                    }
                } else if (matchAndConsume("WALA")) {
                    if (!matchAndConsume("PUNDOK") || !matchAndConsumeBrace('{')) {
                        ErrorHandler.handleExpectedPundokBlock();
                        return;
                    }

                    if (!conditionExecuted) {
                        executeBlock();
                    } else {
                        skipBlock();
                    }
                    break; // after KUNG WALA, no more branches
                } else {
                    ErrorHandler.handleUnexpectedTokenAfterKung();
                    return;
                }
            } else {
                break;
            }
        }
    }


    private void executeBlock() {
        int braceCount = 1;
        while (position < tokens.size() && braceCount > 0) {
            Token token = tokens.get(position);

            if (token.type == TokenType.LEFTBRACE || token.value.equals("{") || token.value.equals("\u007B")) {
                braceCount++;
                position++;
            } else if (token.type == TokenType.RIGHTBRACE || token.value.equals("}") || token.value.equals("\u007D")) {
                braceCount--;
                position++;
                if (braceCount == 0) break; // Exit when reaching the closing brace of this block
            } else {
                // Only parse if we're still inside the block
                if (braceCount > 0) {
                    if (token.type == TokenType.KEYWORD) {
                        switch (token.value) {
                            case "MUGNA":
                                parseVariableDeclaration();
                                break;
                            case "IPAKITA":
                                parsePrintStatement();
                                break;
                            case "DAWAT":
                                parseInputStatement();
                                break;
                            case "KUNG":
                                parseConditionalStatement();
                                break;
                            case "ALANG SA":
                                parseForLoop();
                                break;
                            default:
                                position++; // Skip unrecognized keywords
                        }
                    } else if (token.type == TokenType.IDENTIFIER) {
                        parseAssignment();
                    } else {
                        position++; // Skip other token types
                    }
                } else {
                    position++;
                }
            }
        }
    }

    // Helper to skip a block if condition is false
    private void skipBlock() {
        int braceCount = 1;
        while (position < tokens.size() && braceCount > 0) {
            Token token = tokens.get(position);

            if (token.type == TokenType.LEFTBRACE || token.value.equals("{") || token.value.equals("\u007B")) {
                braceCount++;
            } else if (token.type == TokenType.RIGHTBRACE || token.value.equals("}") || token.value.equals("\u007D")) {
                braceCount--;
            }
            position++;
        }
    }

    private boolean convertToBoolean(Object value) {
        if (value == null) return false;

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0;
        }

        if (value instanceof String) {
            String strValue = (String) value;
            if (strValue.equalsIgnoreCase("OO")) return true;
            if (strValue.equalsIgnoreCase("DILI")) return false;
            return !strValue.isEmpty();
        }

        return true; // Non-null objects default to true
    }

    private void parseStatement(Token token) {
        if (token == null) {
            position++; // Skip null tokens
            return;
        }

        if (token.type == TokenType.KEYWORD) {
            switch (token.value) {
                case "MUGNA":
                    parseVariableDeclaration();
                    break;
                case "IPAKITA":
                    parsePrintStatement();
                    break;
                case "DAWAT":
                    parseInputStatement();
                    break;
                case "KUNG":
                    parseConditionalStatement();
                    break;
                case "ALANG SA":
                    parseForLoop();
                    break;
                default:
                    position++; // Skip unrecognized keywords
                    break;
            }
        } else if (token.type == TokenType.IDENTIFIER) {
            parseAssignment();
        } else {
            position++; // Skip other token types
        }
    }


    private void skipElseBlock() {
        if (position >= tokens.size() || !tokens.get(position).value.equals("PUNDOK")) {
            ErrorHandler.handleExpectedPundokKeyword();
        }
        position++;

        if (position >= tokens.size() || tokens.get(position).type != TokenType.LEFTBRACE) {
            ErrorHandler.handleExpectedOpeningBrace();
        }
        position++;

        int braceCount = 1;
        while (position < tokens.size() && braceCount > 0) {
            Token token = tokens.get(position);
            if (token.type == TokenType.LEFTBRACE) {
                braceCount++;
            } else if (token.type == TokenType.RIGHTBRACE) {
                braceCount--;
            }
            position++;
        }

        if (braceCount > 0) {
            ErrorHandler.handleMissingClosingBrace();
        }
    }



    private void parseInputStatement() {
        position++;

        if (position >= tokens.size() || !tokens.get(position).type.equals(TokenType.COLON)) {
            ErrorHandler.handleExpectedColonAfterKeyword("DAWAT");
        }
        position++;

        List<String> variableNames = new ArrayList<>();

        // Parse the list of variable names
        while (position < tokens.size()) {
            Token token = tokens.get(position);

            if (token.type == TokenType.IDENTIFIER) {
                String varName = token.value;

                if (!variableTypes.containsKey(varName)) {
                    ErrorHandler.handleUndefinedVariable(varName);
                }

                variableNames.add(varName);
                position++;

                // Check if there are more variables to read
                if (position < tokens.size() && tokens.get(position).type == TokenType.COMMA) {
                    position++; // Skip the comma
                } else {
                    break; // End of variable list
                }
            } else {
                ErrorHandler.handleExpectedIdentifier();
            }
        }

        // Now process the input for each variable
        //System.out.print("Enter values: ");
        String input = scanner.nextLine();
        String[] values = input.split(",");

        if (values.length < variableNames.size()) {
            ErrorHandler.handleInsufficientInputValues(variableNames.size(), values.length);
        }

        for (int i = 0; i < variableNames.size(); i++) {
            String varName = variableNames.get(i);
            String varType = variableTypes.get(varName);
            String inputValue = values[i].trim();

            try {
                if (varType.equals("NUMERO")) {
                    int numValue = Integer.parseInt(inputValue);
                    symbolTable.put(varName, (double) numValue);
                } else if (varType.equals("TIPIK")) {
                    double floatValue = Double.parseDouble(inputValue);
                    symbolTable.put(varName, floatValue);
                } else if (varType.equals("TINUOD")) {
                    if (inputValue.equalsIgnoreCase("OO")) {
                        symbolTable.put(varName, true);
                    } else if (inputValue.equalsIgnoreCase("DILI")) {
                        symbolTable.put(varName, false);
                    } else {
                        ErrorHandler.handleInvalidBooleanInput(inputValue);
                    }
                } else {
                    // Assume LETRA (string) type
                    symbolTable.put(varName, inputValue);
                }
            } catch (NumberFormatException e) {
                ErrorHandler.handleInvalidInputFormat(varName, varType, inputValue);
            }
        }
    }

    private void parseVariableDeclaration() {
        position++;
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

            Object value;
            if (varType.equals("NUMERO") || varType.equals("TIPIK")) {
                value = 0.0;
            } else if (varType.equals("TINUOD")) {
                value = false;
            } else {
                value = "";
            }

            if (position < tokens.size() && tokens.get(position).value.equals("=")) {
                position++;

                if (varType.equals("TINUOD")) {
                    value = parseBooleanExpression();
                } else if (varType.equals("LETRA")) {
                    value = parseCharacterExpression();
                } else {
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
        Token identifier = tokens.get(position);
        if (identifier.type != TokenType.IDENTIFIER) {
            ErrorHandler.handleExpectedIdentifier();
            return; // Add a return here to prevent further processing
        }

        String varName = identifier.value;
        if (!variableTypes.containsKey(varName)) {
            ErrorHandler.handleUndefinedVariable(varName);
            return; // Add a return here as well
        }

        position++; // Move past the identifier

        // Check if the next token is an equals sign
        if (position >= tokens.size() || !tokens.get(position).value.equals("=")) {
            ErrorHandler.handleExpectedEqualsAfterIdentifier();
            return; // Add a return here to prevent the method from continuing after error
        }
        position++; // Move past the equals sign

        // Check for chained assignment (x = y = ...)
        if (position < tokens.size() && tokens.get(position).type == TokenType.IDENTIFIER) {
            int tempPosition = position;
            String nextVarName = tokens.get(tempPosition).value;

            if (variableTypes.containsKey(nextVarName) &&
                    tempPosition + 1 < tokens.size() &&
                    tokens.get(tempPosition + 1).value.equals("=")) {
                parseAssignment();

                if (!symbolTable.containsKey(nextVarName)) {
                    ErrorHandler.handleVariableHasNoValue(nextVarName);
                    return;
                }

                Object value = symbolTable.get(nextVarName);
                String currentType = variableTypes.get(varName);
                String nextType = variableTypes.get(nextVarName);

                if (!currentType.equals(nextType)) {
                    ErrorHandler.handleTypeMismatchInAssignment();
                    return;
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

            // Check for end of expression with parenthesis count
            if ((token.type == TokenType.RPAREN || token.value.equals(")")) && parenthesisCount == 0) {
                // Don't consume the token, just exit
                break;
            }

            if (parenthesisCount == 0 && (
                    token.type == TokenType.KEYWORD ||
                            token.type == TokenType.COLON ||
                            (token.type == TokenType.COMMA && operators.isEmpty()) ||
                            token.type == TokenType.RIGHTBRACE)) {
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
                } else if (token.type == TokenType.LPAREN || token.value.equals("(")) {
                    operators.push("(");
                    parenthesisCount++;
                    expectOperand = true;
                } else {
                    ErrorHandler.handleExpectedValue(token);
                }
            } else {
                if (token.type == TokenType.RPAREN || token.value.equals(")")) {
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

        // Process remaining operators
        while (!operators.isEmpty()) {
            if (operators.peek().equals("(")) {
                operators.pop(); // Skip remaining parenthesis
                continue;
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
        return "";
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
        position++;

        if (position >= tokens.size() || !tokens.get(position).type.equals(TokenType.COLON)) {
            ErrorHandler.handleExpectedColonAfterKeyword("IPAKITA");
        }
        position++;

        StringBuilder output = new StringBuilder();
        boolean inEscapeBracket = false;
        boolean hasExpressionToEvaluate = false;
        int expressionStart = position;
        boolean useNewLine = true;  // Default to using newline

        while (position < tokens.size()) {
            Token token = tokens.get(position);

            if (token.type == TokenType.KEYWORD || token.type == TokenType.RIGHTBRACE) {
                // If we have a pending expression, evaluate it before breaking
                if (hasExpressionToEvaluate) {
                    // Save current position
                    int currentPos = position;
                    // Restore position to start of expression
                    position = expressionStart;
                    // Evaluate the expression
                    Object result = parseNumericExpression();
                    // Add result to output
                    if (result instanceof Double) {
                        if (((Double) result) == Math.floor((Double) result)) {
                            // If it's a whole number, display as integer
                            output.append(((Double) result).intValue());
                        } else {
                            output.append(result);
                        }
                    } else {
                        output.append(result);
                    }
                    // Restore position to where we left off
                    position = currentPos;
                }
                break;
            }

            // Handle escape bracket start
            if (token.type == TokenType.LEFTESCAPEBRACKET) {
                if (!inEscapeBracket) {
                    inEscapeBracket = true;
                } else {
                    output.append("[");
                }
                position++;
                continue;
            }

            // Handle escape bracket end
            if (token.type == TokenType.RIGHTESCAPEBRACKET) {
                if (inEscapeBracket) {
                    inEscapeBracket = false;
                } else {
                    output.append("]");
                }
                position++;
                continue;
            }

            if (inEscapeBracket) {
                // Inside escape mode - this part stays mostly the same
                if (token.type == TokenType.LEFTESCAPEBRACKET) {
                    output.append("[");
                } else if (token.type == TokenType.RIGHTESCAPEBRACKET) {
                    output.append("]");
                } else if (token.type == TokenType.IDENTIFIER) {
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
                } else if (token.type == TokenType.OPERATOR && token.value.equals("&")) {
                    // Check for & operator inside escape bracket
                    useNewLine = false;
                } else {
                    output.append(token.value);
                }
                position++;
            } else {
                // Check if we have an expression to evaluate
                if ((token.type == TokenType.IDENTIFIER || token.type == TokenType.NUMERO ||
                        token.type == TokenType.TIPIK || token.value.equals("(")) &&
                        position + 1 < tokens.size() &&
                        (tokens.get(position + 1).value.equals("+") ||
                                tokens.get(position + 1).value.equals("-") ||
                                tokens.get(position + 1).value.equals("*") ||
                                tokens.get(position + 1).value.equals("/"))) {

                    // We have an expression to evaluate
                    expressionStart = position;
                    hasExpressionToEvaluate = true;

                    // Save current position
                    int currentPos = position;
                    // Evaluate the expression
                    Object result = parseNumericExpression();
                    // Add result to output
                    if (result instanceof Double) {
                        if (((Double) result) == Math.floor((Double) result)) {
                            // If it's a whole number, display as integer
                            output.append(((Double) result).intValue());
                        } else {
                            output.append(result);
                        }
                    } else {
                        output.append(result);
                    }

                    // Continue from where parseNumericExpression() left off
                    hasExpressionToEvaluate = false;
                } else {
                    switch (token.type) {
                        case IDENTIFIER:
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
                            position++;
                            break;

                        case LETRA:
                        case NUMERO:
                        case TIPIK:
                        case TINUOD:
                            output.append(token.value);
                            position++;
                            break;

                        case OPERATOR:
                            if (token.value.equals("$")) {
                                // Print with newline regardless of & operator
                                System.out.println(output.toString());
                                output = new StringBuilder();
                                useNewLine = true; // Reset to default
                            } else if (token.value.equals("&")) {
                                // Concatenation operator - disable newline
                                useNewLine = false;
                            } else {
                                output.append(token.value);
                            }
                            position++;
                            break;

                        default:
                            output.append(token.value);
                            position++;
                            break;
                    }
                }
            }
        }

        String finalOutput = output.toString();
        if (!finalOutput.isEmpty()) {
            if (useNewLine) {
                System.out.println(finalOutput);
            } else {
                System.out.print(finalOutput);
            }
        }
    }


    private int getBooleanPrecedence(String operator) {
        if (operator.equals("DILI")) return 5;
        if (operator.equals("<") || operator.equals(">") ||
                operator.equals("<=") || operator.equals(">=") ||
                operator.equals("==") || operator.equals("<>")) return 4;
        if (operator.equals("UG")) return 3;
        if (operator.equals("O")) return 2; //
        return 0;
    }

    private int getOperatorPrecedence(String operator) {
        if (operator.equals("unary-")) return 3;
        if (operator.equals("*") || operator.equals("/") || operator.equals("%")) return 2;
        if (operator.equals("+") || operator.equals("-")) return 1;
        return 0;
    }

    private boolean isValidOperator(String operator) {
        return operator.equals("+") || operator.equals("-") ||
                operator.equals("*") || operator.equals("/") ||
                operator.equals("%");
    }

    private double applyOperator(double a, double b, String operator) {
        switch (operator) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/":
                if (b == 0) ErrorHandler.handleDivisionByZero();
                return a / b;
            case "%":  // Add modulo implementation
                if (b == 0) ErrorHandler.handleDivisionByZero();
                return a % b;
            default:
                ErrorHandler.handleUnknownOperator(operator);
                return 0;
        }
    }
    // Updated parseForLoop method to handle nested structures properly
    private void parseForLoop() {
        position++; // Skip "ALANG SA"

        consume(TokenType.LPAREN, "Expecting '(' after ALANG SA.");

        // Handle initializer
        Token initVarToken = null;
        if (position < tokens.size() && tokens.get(position).type == TokenType.IDENTIFIER) {
            initVarToken = tokens.get(position);
            String varName = initVarToken.value;
            position++; // Move past the identifier

            consume(TokenType.OPERATOR, "Expect '=' in initializer.");

            // Parse the initialization value
            Token valueToken = tokens.get(position);
            if (valueToken.type == TokenType.NUMERO || valueToken.type == TokenType.TIPIK) {
                // Set the variable to this numeric value
                Double value = Double.parseDouble(valueToken.value);
                symbolTable.put(varName, value);
                // Ensure we have a type for the variable
                if (!variableTypes.containsKey(varName)) {
                    variableTypes.put(varName, "NUMERO");
                }
                position++; // Move past the value
            } else if (valueToken.type == TokenType.IDENTIFIER) {
                // Set to another variable's value
                if (!symbolTable.containsKey(valueToken.value)) {
                    ErrorHandler.handleUndefinedVariable(valueToken.value);
                    return;
                }
                symbolTable.put(varName, symbolTable.get(valueToken.value));
                // Copy the variable type as well
                if (!variableTypes.containsKey(varName)) {
                    variableTypes.put(varName, variableTypes.get(valueToken.value));
                }
                position++; // Move past the identifier
            } else {
                ErrorHandler.handleExpectedValue(valueToken);
                return;
            }
        } else {
            ErrorHandler.handleExpectedInitialization();
            return;
        }

        consume(TokenType.COMMA, "Expect ',' after initializer.");

        // Save the condition expression start position
        int conditionStart = position;
        // Skip parsing condition for now, just record the tokens
        List<Token> conditionTokens = new ArrayList<>();
        int parenCount = 0;
        while (position < tokens.size()) {
            Token token = tokens.get(position);
            if (token.type == TokenType.COMMA && parenCount == 0) {
                break;
            }
            if (token.type == TokenType.LPAREN || token.value.equals("(")) {
                parenCount++;
            }
            if (token.type == TokenType.RPAREN || token.value.equals(")")) {
                parenCount--;
                if (parenCount < 0) break; // End of outer parenthesis
            }
            conditionTokens.add(token);
            position++;
        }

        consume(TokenType.COMMA, "Expect ',' after condition.");

        // Handle the increment part
        Token incrVarToken = null;
        String incrementOp = null;
        if (position >= tokens.size() || tokens.get(position).type != TokenType.IDENTIFIER) {
            ErrorHandler.handleExpectedIdentifier();
            return;
        }
        incrVarToken = tokens.get(position);
        String incrementVarName = incrVarToken.value;
        position++; // Move past the variable name

        // Check for increment operator
        if (position >= tokens.size()) {
            ErrorHandler.handleExpectedIncrementOperator();
            return;
        }

        Token opToken = tokens.get(position);
        incrementOp = opToken.value;
        if (!incrementOp.equals("++") && !incrementOp.equals("--")) {
            ErrorHandler.handleExpectedIncrementOperator();
            return;
        }
        position++; // Move past the operator

        consume(TokenType.RPAREN, "Expect ')' after ALANG SA clauses.");
        consume(TokenType.PUNDOK, "Expecting PUNDOK after ALANG SA.");
        consume(TokenType.LEFTBRACE, "Expecting '{' after PUNDOK.");

        // Save the loop body
        List<Token> bodyTokens = new ArrayList<>();
        int braceCount = 1;
        int bodyStart = position;

        while (position < tokens.size() && braceCount > 0) {
            Token token = tokens.get(position);
            if (token.type == TokenType.LEFTBRACE || token.value.equals("{")) {
                braceCount++;
            } else if (token.type == TokenType.RIGHTBRACE || token.value.equals("}")) {
                braceCount--;
                if (braceCount == 0) break; // Found the end of the loop body
            }
            bodyTokens.add(token);
            position++;
        }

        consume(TokenType.RIGHTBRACE, "Expecting '}' at the end of PUNDOK block.");

        // Loop execution
        String initVarName = initVarToken.value;

        while (true) {
            // Evaluate condition
            int savedPos = position;
            position = conditionStart;
            Object conditionResult = parseBooleanExpression();
            position = savedPos;

            if (!convertToBoolean(conditionResult)) {
                break; // Exit loop if condition is false
            }

            // Execute loop body
            int loopBodyPosition = bodyStart;
            for (Token token : bodyTokens) {
                tokens.set(loopBodyPosition, token);
                loopBodyPosition++;
            }

            savedPos = position;
            position = bodyStart;

            // Parse the body as a block
            int innerBraceCount = 1;
            while (position < bodyStart + bodyTokens.size() && innerBraceCount > 0) {
                Token token = tokens.get(position);

                // Process each statement in the body
                if (token.type == TokenType.KEYWORD) {
                    switch (token.value) {
                        case "MUGNA":
                            parseVariableDeclaration();
                            break;
                        case "IPAKITA":
                            parsePrintStatement();
                            break;
                        case "DAWAT":
                            parseInputStatement();
                            break;
                        case "KUNG":
                            parseConditionalStatement();
                            break;
                        case "ALANG SA":
                            parseForLoop(); // Support nested loops
                            break;
                        default:
                            position++;
                            break;
                    }
                } else if (token.type == TokenType.IDENTIFIER) {
                    parseAssignment();
                } else if (token.type == TokenType.LEFTBRACE) {
                    innerBraceCount++;
                    position++;
                } else if (token.type == TokenType.RIGHTBRACE) {
                    innerBraceCount--;
                    position++;
                } else {
                    position++;
                }
            }

            // Increment the loop variable
            if (incrementOp.equals("++")) {
                Object currentValue = symbolTable.get(incrementVarName);
                if (currentValue instanceof Double) {
                    symbolTable.put(incrementVarName, (Double)currentValue + 1);
                } else {
                    ErrorHandler.handleTypeMismatchExpectedNumber(incrementVarName);
                }
            } else if (incrementOp.equals("--")) {
                Object currentValue = symbolTable.get(incrementVarName);
                if (currentValue instanceof Double) {
                    symbolTable.put(incrementVarName, (Double)currentValue - 1);
                } else {
                    ErrorHandler.handleTypeMismatchExpectedNumber(incrementVarName);
                }
            }

            position = savedPos;
        }
    }

    // Helper method to consume expected tokens
    private void consume(TokenType type, String errorMessage) {
        if (position >= tokens.size() || tokens.get(position).type != type) {
            if (position < tokens.size()) {
                ErrorHandler.handleError(errorMessage, tokens.get(position));
            } else {
                ErrorHandler.handleError(errorMessage, null);
            }
            return;
        }
        position++; // Advance past the token
    }

    // For string-based token consumption
    private void consume(String value, String errorMessage) {
        if (position >= tokens.size() || !tokens.get(position).value.equals(value)) {
            if (position < tokens.size()) {
                ErrorHandler.handleError(errorMessage, tokens.get(position));
            } else {
                ErrorHandler.handleError(errorMessage, null);
            }
            return;
        }
        position++; // Advance past the token
    }


    private void parseIncrementDecrement(String varName) {
        Token token = tokens.get(position);
        // Check if it's ++ or --
        if (token.value.equals("++")) {
            // Increment operation
            if (!variableTypes.containsKey(varName)) {
                ErrorHandler.handleUndefinedVariable(varName);
            }

            Object currentValue = symbolTable.get(varName);
            if (currentValue instanceof Double) {
                symbolTable.put(varName, (Double)currentValue + 1);
            } else {
                ErrorHandler.handleTypeMismatchExpectedNumber(varName);
            }
            position++;
        } else if (token.value.equals("--")) {
            // Decrement operation
            if (!variableTypes.containsKey(varName)) {
                ErrorHandler.handleUndefinedVariable(varName);
            }

            Object currentValue = symbolTable.get(varName);
            if (currentValue instanceof Double) {
                symbolTable.put(varName, (Double)currentValue - 1);
            } else {
                ErrorHandler.handleTypeMismatchExpectedNumber(varName);
            }
            position++;
        } else {
            ErrorHandler.handleInvalidUpdateExpression();
        }
    }
}