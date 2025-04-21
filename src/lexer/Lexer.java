package lexer;

import java.util.*;

public class Lexer {
    private String input;
    private int position;
    public List<Token> tokens;

    public Lexer(String input) {
        this.input = input
                .replace("‘", "'")   // Left single quote
                .replace("’", "'")   // Right single quote
                .replace("“", "\"")  // Left double quote
                .replace("”", "\""); // Right double quote
        this.position = 0;
        this.tokens = new ArrayList<>();
    }

    public List<Token> tokenize() {
        while (position < input.length()) {
            char currentChar = input.charAt(position);

            if (Character.isWhitespace(currentChar)) {
                position++;
                continue;
            }

            if (currentChar == '-' && lookahead("--")) {
                skipComment();
                continue;
            }

            if (lookahead("SUGOD")) {
                tokens.add(new Token(TokenType.KEYWORD, "SUGOD"));
                position += 5;
                continue;
            }

            if (lookahead("KATAPUSAN")) {
                tokens.add(new Token(TokenType.KEYWORD, "KATAPUSAN"));
                position += 9;
                continue;
            }

            if (lookahead("MUGNA")) {
                tokens.add(new Token(TokenType.KEYWORD, "MUGNA"));
                position += 5;
                continue;
            }

            if (lookahead("IPAKITA")) {
                tokens.add(new Token(TokenType.KEYWORD, "IPAKITA"));
                position += 7;
                continue;
            }

            if (lookahead("DAWAT")) {
                tokens.add(new Token(TokenType.KEYWORD, "DAWAT"));
                position += 5;
                continue;
            }

            if (lookahead("OO") || lookahead("DILI")) {
                tokens.add(new Token(TokenType.TINUOD, lookahead("OO") ? "OO" : "DILI"));
                position += lookahead("OO") ? 2 : 4;
                continue;
            }

            if (Character.isLetter(currentChar) || currentChar == '_') {
                String identifier = extractIdentifier();
                tokens.add(new Token(TokenType.IDENTIFIER, identifier));
                continue;
            }

            if (Character.isDigit(currentChar)) {
                String number = extractNumber();
                if (position < input.length() && input.charAt(position) == '.') {
                    position++;
                    number += "." + extractNumber();
                    tokens.add(new Token(TokenType.TIPIK, number));
                } else {
                    tokens.add(new Token(TokenType.NUMERO, number));
                }
                continue;
            }

            if (currentChar == '"') {
                tokens.add(new Token(TokenType.LETRA, extractString()));
                continue;
            }

            if (currentChar == '\'') {
                tokens.add(new Token(TokenType.LETRA, extractCharacter()));
                continue;
            }

            if(currentChar == '['){
                tokens.add(new Token(TokenType.LEFTESCAPEBRACKET, String.valueOf(currentChar)));
                position++;
                continue;
            }
            if(currentChar == ']'){
                tokens.add(new Token(TokenType.RIGHTESCAPEBRACKET, String.valueOf(currentChar)));
                position++;
                continue;
            }

            if (position + 1 < input.length()) {
                String twoChars = input.substring(position, position + 2);
                if (twoChars.equals("<=") || twoChars.equals(">=") ||
                        twoChars.equals("==") || twoChars.equals("<>") ||
                        twoChars.equals("&&") || twoChars.equals("&")) {
                    tokens.add(new Token(TokenType.OPERATOR, twoChars));
                    position += 2;
                    continue;
                }
            }

            if ("()+-*/%$&#,.=<>[]".indexOf(currentChar) != -1) {
                tokens.add(new Token(currentChar == ',' ? TokenType.COMMA : TokenType.OPERATOR, String.valueOf(currentChar)));
                position++;
                continue;
            }

            if (currentChar == ':') {
                if (position + 1 < input.length() && input.charAt(position + 1) == '=') {
                    tokens.add(new Token(TokenType.OPERATOR, ":="));
                    position += 2;
                } else {
                    tokens.add(new Token(TokenType.COLON, ":"));
                    position++;
                }
                continue;
            }

            throw new RuntimeException("Unexpected character: " + currentChar);
        }
        return tokens;
    }

    private String extractIdentifier() {
        StringBuilder identifier = new StringBuilder();
        while (position < input.length() && (Character.isLetterOrDigit(input.charAt(position)) || input.charAt(position) == '_')) {
            identifier.append(input.charAt(position));
            position++;
        }
        return identifier.toString();
    }

    private void skipComment() {
        while (position < input.length() && input.charAt(position) != '\n') {
            position++;
        }
    }

    private String extractNumber() {
        StringBuilder number = new StringBuilder();
        while (position < input.length() && Character.isDigit(input.charAt(position))) {
            number.append(input.charAt(position));
            position++;
        }
        return number.toString();
    }

    private String extractCharacter() {
        char delimiter = input.charAt(position);
        position++;

        if (position >= input.length()) {
            throw new RuntimeException("Unterminated character literal");
        }

        char value = input.charAt(position);
        position++;

        if (position >= input.length() || input.charAt(position) != delimiter) {
            throw new RuntimeException("Unterminated character literal");
        }

        position++;
        return String.valueOf(value);
    }

    private boolean lookahead(String keyword) {
        return input.startsWith(keyword, position);
    }

    private String extractString() {
        char delimiter = input.charAt(position);
        StringBuilder sb = new StringBuilder();
        position++;

        while (position < input.length() && input.charAt(position) != delimiter) {
            sb.append(input.charAt(position));
            position++;
        }

        if (position >= input.length()) {
            throw new RuntimeException("Unterminated string literal");
        }

        position++;
        return sb.toString();
    }
}