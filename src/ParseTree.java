import java.util.List;

public class ParseTree{
    private String input;
    private int position;
    private List<Token> tokens;

    public ParseTree(List<Token> tokens){
        this.tokens = tokens;
        this.position = 0;


    }
}