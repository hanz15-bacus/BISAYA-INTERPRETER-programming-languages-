import java.io.*;
import java.nio.file.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Define the file to read
        String fileName = "test.txt";

        try {
            // Read the entire content of the file
            String input = Files.readString(Paths.get(fileName));

            // Run the lexer
            Lexer lexer = new Lexer(input);
            List<Token> tokens = lexer.tokenize();

            // Print all tokens
            for (Token token : tokens) {
                System.out.println(token);
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}