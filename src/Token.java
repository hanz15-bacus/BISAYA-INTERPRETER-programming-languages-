public class Token {
    public final TokenType type;
    public final String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }
    public TokenType getType() {
        return type;
    }
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return type + "(" + value + ")";
    }
}