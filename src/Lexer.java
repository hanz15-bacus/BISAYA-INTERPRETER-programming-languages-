import java.util.*;
import java.io.*;
import java.util.regex.*;

public class Lexer {
    private String input;
    private int position;
    private List<Token> tokens;

    public Lexer(String input) {
        this.input = input.replace("’", "'")
                .replace("‘", "'")
                .replace("“", "\"")
                .replace("”", "\"");
        this.position = 0;
        this.tokens = new ArrayList<>();
    }

    public List<Token> tokenize() {
        while (position < input.length()) {
            char currentChar = input.charAt(position);

            if ("’‘“”".indexOf(currentChar) != -1) {
                position++;
                continue;
            }

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

            if (currentChar == '"' || currentChar == '\'') {
                tokens.add(new Token(TokenType.LETRA, extractCharacter()));
                continue;
            }

            if ("()+-*/%<>=$&[]#,".indexOf(currentChar) != -1) {
                tokens.add(new Token(currentChar == ',' ? TokenType.COMMA : TokenType.OPERATOR, String.valueOf(currentChar)));
                position++;
                continue;
            }

            if (lookahead(">=") || lookahead("<=") || lookahead("==") || lookahead("<>") || lookahead("&")) {
                tokens.add(new Token(TokenType.OPERATOR, input.substring(position, position + 2)));
                position += 2;
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
        char quoteType = input.charAt(position);
        position++;
        StringBuilder character = new StringBuilder();
        while (position < input.length() && input.charAt(position) != quoteType) {
            character.append(input.charAt(position));
            position++;
        }
        position++;
        return character.toString();
    }

    private boolean lookahead(String keyword) {
        return input.startsWith(keyword, position);
    }
}