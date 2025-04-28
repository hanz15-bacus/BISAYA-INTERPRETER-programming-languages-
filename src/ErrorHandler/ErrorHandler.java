package ErrorHandler;

import lexer.Token;
import lexer.TokenType;

public class ErrorHandler {

    public static void handleUnexpectedKeyword(String keyword) {
        System.out.println("Error: Unexpected keyword: " + keyword);
    }

    public static void handleUnexpectedToken(TokenType type, String value) {
        System.out.println("Error: Unexpected token: " + type + " with value: " + value);
    }

    public static void handleExpectedTypeAfterKeyword(String keyword) {
        System.out.println("Error: Expected type after " + keyword);
    }

    public static void handleExpectedIdentifier() {
        System.out.println("Error: Expected identifier.");
    }

    public static void handleUndefinedVariable(String varName) {
        System.out.println("Error: Undefined variable: " + varName + ". Declare it before use.");
    }

    public static void handleExpectedEqualsAfterIdentifier() {
        System.out.println("Error: Expected '=' after identifier.");
    }

    public static void handleTypeMismatchInAssignment() {
        System.out.println("Error: Type mismatch in assignment.");
    }

    public static void handleVariableHasNoValue(String varName) {
        System.out.println("Error: Variable has no value: " + varName);
    }

    public static void handleInvalidBooleanLiteral(String value) {
        System.out.println("Error: Invalid boolean literal: " + value);
    }

    public static void handleExpectedValue(Token token) {
        System.out.println("Error: Expected value, but got: " + token.value + " (Token type: " + token.type + ")");
    }

    public static void handleMismatchedParentheses() {
        System.out.println("Error: Mismatched parentheses.");
    }

    public static void handleMissingOperandForNot() {
        System.out.println("Error: Missing operand for 'DILI'.");
    }

    public static void handleInsufficientOperandsForOperator(String op) {
        System.out.println("Error: Insufficient operands for operator " + op + ".");
    }

    public static void handleInvalidOperatorForBooleanComparison(String op) {
        System.out.println("Error: Invalid operator for boolean comparison: " + op);
    }

    public static void handleInvalidCharacterExpression() {
        System.out.println("Error: Invalid character expression.");
    }

    public static void handleTypeMismatchExpectedCharacter() {
        System.out.println("Error: Type mismatch: expected character value.");
    }

    public static void handleCannotConvertStringToNumber(String val) {
        System.out.println("Error: Cannot convert string to number: " + val);
    }

    public static void handleTypeMismatchExpectedNumber(String className) {
        System.out.println("Error: Type mismatch: expected number, got " + className + ".");
    }

    public static void handleExpectedNumberOrParenthesis(Token token) {
        System.out.println("Error: Expected number, variable, or '(' or '[' but found: " + token.value);
    }

    public static void handleInvalidExpressionTooManyOperands() {
        System.out.println("Error: Invalid expression: too many operands.");
    }

    public static void handleDivisionByZero() {
        System.out.println("Error: Division by zero.");
    }

    public static void handleUnknownOperator(String operator) {
        System.out.println("Error: Unknown operator: " + operator);
    }

    public static void handleExpectedColonAfterKeyword(String keyword) {
        System.out.println("Error: Expected ':' after " + keyword);
    }

    public static void handleInvalidExpression(String message) {
        System.out.println("Error: Invalid expression: " + message);
    }

    public static void handleInvalidPrintStatement() {
        System.out.println("Error: Invalid print statement.");
    }

    public static void handleInvalidInputStatement() {
        System.out.println("Error: Syntax Error: Invalid DAWAT statement. Expected at least one variable identifier.");
    }

    public static void handleInsufficientInputValues(int expected, int actual) {
        System.out.println("Error: Input Error: Expected " + expected + " values but received only " + actual + ".");
    }

    public static void handleInvalidInputForType(String varName, String varType, String value) {
        System.out.println("Error: Type Error: Cannot convert '" + value + "' to type " + varType + " for variable '" + varName + "'.");
    }

    public static void handleInvalidInputFormat(String varName, String varType, String inputValue) {
        System.out.println("Error: Invalid input format for variable '" + varName + "' of type '" + varType + "'.");
    }

    public static void handleInvalidBooleanInput(String inputValue) {
        System.out.println("Error: Invalid boolean input: " + inputValue);
    }

    public static void handleExpectedClosingBrace() {
        System.out.println("Error: Expected closing brace '}'.");
    }

    public static void handleExpectedOpeningBrace() {
        System.out.println("Error: Expected opening brace '{'.");
    }

    public static void handleExpectedPundokAfterCondition() {
        System.out.println("Error: Expected keyword 'PUNDOK' after condition.");
    }

    public static void handleExpectedConditionAfterKung() {
        System.out.println("Error: Expected condition after keyword 'KUNG'.");
    }

    public static void handleInvalidConditionType() {
        System.out.println("Error: Invalid condition type: Conditions must evaluate to a boolean value (TINUOD).");
    }

    public static void handleExpectedParenthesisAfterKung() {
        System.out.println("Error: Expected '(' after 'KUNG' keyword.");
    }

    public static void handleNonBooleanCondition() {
        System.out.println("Error: Condition type error: The expression inside 'KUNG' must be a boolean.");
    }

    public static void handleExpectedPundokKeyword() {
        System.out.println("Error: Expected 'PUNDOK' keyword to start a code block after condition.");
    }

    public static void handleExpectedClosingParenthesis() {
        System.out.println("Error: Missing closing ')' in condition.");
    }

    public static void handleMissingClosingBrace() {
        System.out.println("Error: Syntax error: Missing closing '}' for code block.");
    }

    public static void handleUnexpectedToken(Token token) {
        System.out.println("Error: Unexpected token: '" + token.value + "' of type " + token.type + ".");
    }

    public static void handleExpectedParenthesisAfterKungDili() {
        System.out.println("Error: Expected opening parenthesis after 'KUNG DILI'.");
    }

    public static void handleExpectedKungKeyword() {
        System.out.println("Error: Expected 'KUNG' keyword.");
    }

    public static void handleExpectedKatapusan() {
        System.out.println("Error: Expected 'KATAPUSAN' keyword to close the block.");
    }

    public static void handleExpectedPundokBlock() {
        System.out.println("Error: Expected 'PUNDOK' block after condition.");
    }

    public static void handleUnexpectedTokenAfterKung() {
        System.out.println("Error: Unexpected token after 'KUNG' condition.");
    }
    public static void handleExpectedParenthesisAfterAlangSa() {
        System.err.println("Error: Expected '(' after 'ALANG SA'");
    }

    public static void handleExpectedInitialization() {
        System.err.println("Error: Expected initialization in for loop");
    }

    public static void handleExpectedCommaInForLoop() {
        System.err.println("Error: Expected comma in for loop parameters");
    }

    public static void handleInvalidUpdateExpression() {
        System.err.println("Error: Invalid update expression in for loop");
    }
    public static void handleExpectedEqualsOrIncrementOrDecrement() {
        System.err.println("Error: Expected '=', '++', or '--' after identifier.");
    }


    public static void handleError(String errorMessage, Object o) {
    }

    public static void handleExpectedIncrementOperator() {

    }
}