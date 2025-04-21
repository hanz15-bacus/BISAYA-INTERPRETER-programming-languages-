package parser;

import java.util.ArrayList;
import java.util.List;

public class ConditionalStatement {
    private String condition;
    private List<String> trueBlock;
    private List<String> falseBlock;
    private List<String> elseIfCondition;
    private List<String> elseIfBlock;

    public ConditionalStatement(String condition) {
        this.condition = condition;
        this.trueBlock = new ArrayList<>();
        this.falseBlock = new ArrayList<>();
        this.elseIfCondition = new ArrayList<>();
        this.elseIfBlock = new ArrayList<>();
    }

    public void setTrueBlock(List<String> trueBlock) {
        this.trueBlock = trueBlock;
    }

    public void setFalseBlock(List<String> falseBlock) {
        this.falseBlock = falseBlock;
    }

    public void setElseIfCondition(List<String> elseIfCondition) {
        this.elseIfCondition = elseIfCondition;
    }

    public void setElseIfBlock(List<String> elseIfBlock) {
        this.elseIfBlock = elseIfBlock;
    }

    public void execute() {
        if (evaluateCondition(condition)) {
            executeBlock(trueBlock);
        } else if (!elseIfCondition.isEmpty() && evaluateCondition(String.join(" ", elseIfCondition))) {
            executeBlock(elseIfBlock);
        } else {
            executeBlock(falseBlock);
        }
    }

    private boolean evaluateCondition(String condition) {
        // Add logic to evaluate the condition (e.g., "TRUE" or "FALSE")
        return condition.equals("TRUE"); // Simple example, can be expanded
    }

    private void executeBlock(List<String> block) {
        for (String statement : block) {
            System.out.println("Executing: " + statement);
            // Logic to execute each statement in the block
        }
    }
}

