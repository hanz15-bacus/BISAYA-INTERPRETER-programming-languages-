package ErrorHandler;

import lexer.Token;
import lexer.TokenType;

public class ErrorHandler {
    public static void handleUnexpectedKeyword(String keyword) {
        System.out.println("Error: Unexpected keyword '" + keyword + "' encountered.");
    }

    public static void handleExpectedKeyword(String keyword) {
        System.out.println("Error: Expected keyword '" + keyword + "' was not found.");
    }


    public static void handleUnexpectedToken(TokenType type, String value) {
        System.out.println("Error: Unexpected token of type '" + type + "' with value '" + value + "' encountered.");
    }

    public static void handleExpectedTypeAfterKeyword(String keyword) {
        System.out.println("Error: Expected a data type specification after the keyword '" + keyword + "'.");
    }

    public static void handleExpectedIdentifier() {
        System.out.println("Error: Expected an identifier (variable name) at this location.");
    }

    public static void handleUndefinedVariable(String varName) {
        System.out.println("Error: Undefined variable '" + varName + "'. Please declare it before using it.");
    }

    public static void handleExpectedEqualsAfterIdentifier() {
        System.out.println("Error: Expected the assignment operator '=' after the identifier.");
    }

    public static void handleTypeMismatchInAssignment() {
        System.out.println("Error: Type mismatch encountered during assignment. The value being assigned is not compatible with the variable's type.");
    }

    public static void handleVariableHasNoValue(String varName) {
        System.out.println("Error: Variable '" + varName + "' has not been assigned a value before being used.");
    }

    public static void handleInvalidBooleanLiteral(String value) {
        System.out.println("Error: Invalid boolean literal '" + value + "'. Expected 'TINUOD' or 'BAW'");
    }

    public static void handleExpectedValue(Token token) {
        System.out.println("Error: Expected a value but encountered '" + token.value + "' (Token type: " + token.type + ").");
    }

    public static void handleMismatchedParentheses() {
        System.out.println("Error: Mismatched parentheses found in the expression.");
    }

    public static void handleMissingOperandForNot() {
        System.out.println("Error: The unary operator 'DILI' requires an operand.");
    }

    public static void handleInsufficientOperandsForOperator(String op) {
        System.out.println("Error: Insufficient operands provided for the operator '" + op + "'.");
    }

    public static void handleInvalidOperatorForBooleanComparison(String op) {
        System.out.println("Error: Invalid operator '" + op + "' used for boolean comparison. Please use valid boolean operators.");
    }

    public static void handleInvalidCharacterExpression() {
        System.out.println("Error: Invalid character expression. Ensure it is correctly formatted.");
    }

    public static void handleTypeMismatchExpectedCharacter() {
        System.out.println("Error: Type mismatch. Expected a character value at this location.");
    }

    public static void handleCannotConvertStringToNumber(String val) {
        System.out.println("Error: Cannot convert the string value '" + val + "' to a numerical type.");
    }

    public static void handleTypeMismatchExpectedNumber(String className) {
        System.out.println("Error: Type mismatch. Expected a numerical value but found a value of type '" + className + "'.");
    }

    public static void handleExpectedNumberOrParenthesis(Token token) {
        System.out.println("Error: Expected a number, variable, opening parenthesis '(', or opening square bracket '[' but found '" + token.value + "'.");
    }

    public static void handleInvalidExpressionTooManyOperands() {
        System.out.println("Error: Invalid expression. Too many operands provided, resulting in an ambiguous evaluation.");
    }

    public static void handleDivisionByZero() {
        System.out.println("Error: Attempted division by zero. This operation is mathematically undefined.");
    }

    public static void handleUnknownOperator(String operator) {
        System.out.println("Error: Unknown operator '" + operator + "' encountered.");
    }

    public static void handleExpectedColonAfterKeyword(String keyword) {
        System.out.println("Error: Expected a colon ':' after the keyword '" + keyword + "'.");
    }

    public static void handleInvalidExpression(String message) {
        System.out.println("Error: Invalid expression: " + message);
    }

    public static void handleInvalidPrintStatement() {
        System.out.println("Error: Syntax error in the 'IMPRINT' statement. Ensure the correct format is used.");
    }
    public static void handleInvalidInputStatement() {
        System.out.println("Syntax Error: Invalid 'DAWAT' statement. Expected at least one variable identifier to receive input.");
    }
    public static void handleInsufficientInputValues(int expected, int actual) {
        System.out.println("Input Error: Expected " + expected + " input values but received only " + actual + ".");
    }

    public static void handleInvalidInputForType(String varName, String varType, String value) {
        System.out.println("Type Error: Cannot convert the input value '" + value + "' to the expected type '" + varType + "' for the variable '" + varName + "'.");
    }

    public static void handleInvalidInputFormat(String varName, String varType, String inputValue) {
        System.out.println("Syntax Error: Invalid input format provided for variable '" + varName + "' of type '" + varType + "'. The input '" + inputValue + "' does not match the expected format.");
    }

    public static void handleInvalidBooleanInput(String inputValue) {
        System.out.println("Syntax Error: Invalid boolean input '" + inputValue + "'. Please enter 'TINUOD' or 'BAW'.");
    }
    public static void handleExpectedClosingBrace() {
        System.out.println("Syntax Error: Expected closing brace '}' to complete the code block.");
    }

    public static void handleExpectedOpeningBrace() {
        System.out.println("Syntax Error: Expected opening brace '{' to begin a code block.");
    }

    public static void handleExpectedPundokAfterCondition() {
        System.out.println("Syntax Error: Expected the keyword 'PUNDOK' to introduce the code block after the conditional statement.");
    }

    public static void handleExpectedConditionAfterKung() {
        System.out.println("Syntax Error: Expected a condition (an expression that evaluates to 'TINUOD' or 'BAW') after the keyword 'KUNG'.");
    }

    public static void handleInvalidConditionType() {
        System.out.println("Type Error: Invalid condition type. Conditions must evaluate to a boolean value ('TINUOD' or 'BAW').");
    }

    public static void handleExpectedParenthesisAfterKung() {
        System.out.println("Syntax error: Expected an opening parenthesis '(' after the 'KUNG' keyword to enclose the condition.");
    }

    public static void handleExpectedSymbolAfterToken(String paren, String keyword) {
        System.out.println("Syntax error: Expected '" + paren + "' after the keyword '" + keyword + "'.");
    }

    public static void handleNonBooleanCondition() {
        System.out.println("Type error: The expression inside the 'KUNG' statement must evaluate to a boolean ('TINUOD' or 'BAW').");
    }

    public static void handleExpectedPundokKeyword() {
        System.out.println("Syntax error: Expected the keyword 'PUNDOK' to start a code block following a conditional statement.");
    }

    public static void handleExpectedClosingParenthesis() {
        System.out.println("Syntax error: Missing closing parenthesis ')' at the end of the condition.");
    }

    public static void handleMissingClosingBrace() {
        System.out.println("Syntax error: Missing closing brace '}' to terminate the current code block.");
    }

    public static void handleUnexpectedToken(Token token) {
        System.out.println("Syntax error: Unexpected token '" + token.value + "' of type " + token.type + " encountered.");
    }
}