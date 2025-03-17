import java.util.*;

public class Lexer {
    private final String input;
    private int position = 0;
    private static final Map<String, TokenType> KEYWORDS = Map.of(
            "sagdii", TokenType.SAGDII,
            "imprinta", TokenType.IMPRINTA
    );

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (position < input.length()) {
            char current = input.charAt(position);

            if (Character.isWhitespace(current)) {
                position++; // Skip whitespace
            } else if (Character.isDigit(current)) {
                tokens.add(lexNumber());
            } else if (Character.isLetter(current)) {
                tokens.add(lexIdentifier());
            } else if (current == '=') {
                tokens.add(new Token(TokenType.PAREHAS, "="));
                position++;
            } else if (current == '+') {
                tokens.add(new Token(TokenType.DUGANG, "+"));
                position++;
            } else if (current == '-') {
                tokens.add(new Token(TokenType.KUHA, "-"));
                position++;
            }else if (current == '*') {
                tokens.add(new Token(TokenType.PADAGHAN, "*"));
                position++;
            }else if (current == '/') {
                tokens.add(new Token(TokenType.BAHIN, "/"));
                position++;
            }
            else if (current == ';') {
                tokens.add(new Token(TokenType.TULDOK_KUWIT, ";"));
                position++;
            } else if (current == '(') {
                tokens.add(new Token(TokenType.ABLI_PANAKLONG, "("));
                position++;
            } else if (current == ')') {
                tokens.add(new Token(TokenType.SIRADO_PANAKLONG, ")"));
                position++;
            } else {
                throw new RuntimeException("Unexpected character: " + current);
            }
        }

        tokens.add(new Token(TokenType.KINALASAN, ""));
        return tokens;
    }

    private Token lexNumber() {
        StringBuilder number = new StringBuilder();
        while (position < input.length() && Character.isDigit(input.charAt(position))) {
            number.append(input.charAt(position));
            position++;
        }
        return new Token(TokenType.GIDAGHANON, number.toString());
    }

    private Token lexIdentifier() {
        StringBuilder identifier = new StringBuilder();
        while (position < input.length() && Character.isLetter(input.charAt(position))) {
            identifier.append(input.charAt(position));
            position++;
        }

        String text = identifier.toString();
        TokenType type = KEYWORDS.getOrDefault(text, TokenType.ILHANAN);
        return new Token(type, text);
    }
}