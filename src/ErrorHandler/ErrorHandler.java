package ErrorHandler;

import lexer.Token;
import lexer.TokenType;

public class ErrorHandler {
    public static void handleUnexpectedKeyword(String keyword) {
        throw new RuntimeException("Unexpected keyword: " + keyword);
    }


    public static void handleUnexpectedToken(TokenType type, String value) {
        throw new RuntimeException("Unexpected token: " + type + " with value: " + value);
    }

    public static void handleExpectedTypeAfterKeyword(String keyword) {
        throw new RuntimeException("Expected type after " + keyword);
    }

    public static void handleExpectedIdentifier() {
        throw new RuntimeException("Expected identifier");
    }

    public static void handleUndefinedVariable(String varName) {
        throw new RuntimeException("Undefined variable: " + varName + ". Declare it before use.");
    }

    public static void handleExpectedEqualsAfterIdentifier() {
        throw new RuntimeException("Expected '=' after identifier");
    }

    public static void handleTypeMismatchInAssignment() {
        throw new RuntimeException("Type mismatch in assignment");
    }

    public static void handleVariableHasNoValue(String varName) {
        throw new RuntimeException("Variable has no value: " + varName);
    }

    public static void handleInvalidBooleanLiteral(String value) {
        throw new RuntimeException("Invalid boolean literal: " + value);
    }

    public static void handleExpectedValue(Token token) {
        throw new RuntimeException("Expected value, but got: " + token.value +
                " (lexer.Token type: " + token.type + ")");
    }

    public static void handleMismatchedParentheses() {
        throw new RuntimeException("Mismatched parentheses");
    }

    public static void handleMissingOperandForNot() {
        throw new RuntimeException("Missing operand for 'DILI'");
    }

    public static void handleInsufficientOperandsForOperator(String op) {
        throw new RuntimeException("Insufficient operands for operator " + op);
    }

    public static void handleInvalidOperatorForBooleanComparison(String op) {
        throw new RuntimeException("Invalid operator for boolean comparison: " + op);
    }

    public static void handleInvalidCharacterExpression() {
        throw new RuntimeException("Invalid character expression");
    }

    public static void handleTypeMismatchExpectedCharacter() {
        throw new RuntimeException("Type mismatch: expected character value");
    }

    public static void handleCannotConvertStringToNumber(String val) {
        throw new RuntimeException("Cannot convert string to number: " + val);
    }

    public static void handleTypeMismatchExpectedNumber(String className) {
        throw new RuntimeException("Type mismatch: expected number, got " + className);
    }

    public static void handleExpectedNumberOrParenthesis(Token token) {
        throw new RuntimeException("Expected number, variable, or '(' or '[' but found: " + token.value);
    }

    public static void handleInvalidExpressionTooManyOperands() {
        throw new RuntimeException("Invalid expression: too many operands");
    }

    public static void handleDivisionByZero() {
        throw new RuntimeException("Division by zero");
    }

    public static void handleUnknownOperator(String operator) {
        throw new RuntimeException("Unknown operator: " + operator);
    }

    public static void handleExpectedColonAfterKeyword(String keyword) {
        throw new RuntimeException("Expected ':' after " + keyword);
    }

    public static void handleInvalidExpression(String message) {
        throw new RuntimeException("Invalid expression: " + message);
    }

    public static void handleInvalidPrintStatement() {
        throw new RuntimeException("Invalid print statement");
    }
    public static void handleInvalidInputStatement() {
        throw new RuntimeException("Syntax Error: Invalid DAWAT statement. Expected at least one variable identifier");
    }
    public static void handleInsufficientInputValues(int expected, int actual) {
        throw new RuntimeException("Input Error: Expected " + expected + " values but received only " + actual);
    }

    public static void handleInvalidInputForType(String varName, String varType, String value) {
        throw new RuntimeException("Type Error: Cannot convert '" + value + "' to type " + varType + " for variable '" + varName + "'");
    }

}