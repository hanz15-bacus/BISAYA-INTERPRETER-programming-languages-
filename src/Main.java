import lexer.Lexer;
import lexer.Token;
import parser.Parser;

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
            for (lexer.Token token : tokens) {
               System.out.println(token);
            }
            */
           /* public lexer.Lexer(String input) {
                this.input = input
                        .replace("‘", "'")   // Left single quote
                        .replace("’", "'")   // Right single quote
                        .replace("“", "\"")  // Left double quote
                        .replace("”", "\""); // Right double quote
                this.position = 0;
                this.tokens = new ArrayList<>();
            }
            */
            System.out.println("\nExecuting Bisaya++ Code:\n");
            System.out.println();
            System.out.println("no error");
            Parser parser = new Parser(tokens);
            parser.parse();
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}