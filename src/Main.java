import java.io.*;
import java.nio.file.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        String fileName = "test.txt";

        try {
            String input = Files.readString(Paths.get(fileName));

            Lexer lexer = new Lexer(input);
            List<Token> tokens = lexer.tokenize();

            /*
           System.out.println("Tokens:");
            for (Token token : tokens) {
               System.out.println(token);
            }
            */
            System.out.println("\nExecuting Bisaya++ Code:\n");
            Parser parser = new Parser(tokens);
            parser.parse();
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
