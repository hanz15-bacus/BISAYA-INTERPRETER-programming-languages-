import java.util.*;
import java.io.*;
import java.util.regex.*;

public class Lexer {
    private String input;
    private int position;
    private List<Token> tokens;

    public Lexer(String input) {
        this.input = input;
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

            if (lookahead("OO")) {
                tokens.add(new Token(TokenType.TINUOD, "OO"));
                position += 2;
                continue;
            }

            if (lookahead("DILI")) {
                tokens.add(new Token(TokenType.TINUOD, "DILI"));
                position += 4;
                continue;
            }

            if ("()+-*/%<>=".indexOf(currentChar) != -1) {
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(currentChar)));
                position++;
                continue;
            }

            if (lookahead(">=") || lookahead("<=") || lookahead("==") || lookahead("<>") ) {
                tokens.add(new Token(TokenType.OPERATOR, input.substring(position, position + 2)));
                position += 2;
                continue;
            }

            throw new RuntimeException("Unexpected character: " + currentChar);
        }
        return tokens;
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
        position++; // Move past the closing quote
        return character.toString();
    }

    private boolean lookahead(String keyword) {
        return input.startsWith(keyword, position);
    }

    public static void main(String[] args) {
        try {
            File file = new File("test.txt");
            Scanner scanner = new Scanner(file);
            StringBuilder code = new StringBuilder();
            while (scanner.hasNextLine()) {
                code.append(scanner.nextLine()).append(" ");
            }
            scanner.close();

            Lexer lexer = new Lexer(code.toString());
            List<Token> tokens = lexer.tokenize();
            for (Token token : tokens) {
                System.out.println(token);
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        }
    }
}