import java.util.HashMap;
import java.util.Map;


public class SwiftInterpreter {
    public static void main(String[] args) {
        SwiftInterpreter interpreter = new SwiftInterpreter();
        String input = """
    var num = 23
    var sum = 0
    while num > 0 {
        var digit = num % 10
        sum = sum + digit
        num = num / 10
    }
    print(sum)
""";
        interpreter.eval(input);
    }

    private final Map<String, Integer> variables = new HashMap<>();


    public void eval(String code) {
        String[] lines = code.split("\\r?\\n");
        int index = 0;
        while (index < lines.length) {
            String line = lines[index].trim();
            if (line.isEmpty() || line.equals("}")) {
                index++;
                continue;
            }
            if (line.contains("=") && !line.startsWith("for")) {
                handle_assignment(line);
            } else if (line.startsWith("print")) {
                handle_print(line);
            } else if (line.startsWith("while")) {
                index = handleWhileLoop(lines, index);
            }
            else {
                throw new IllegalArgumentException("unrecognized statement: " + line);
            }
            index++;
        }
    }

    private void handle_assignment(String line) {
        line = line.replaceFirst("var ", "").trim();
        String[] parts = line.split("=", 2);
        String var_name = parts[0].trim();
        String expression = parts[1].trim();
        int value = evaluate_expression(expression);
        variables.put(var_name, value);
    }

    private void handle_print(String line) {
        String var_name = line.substring(line.indexOf('(') + 1, line.indexOf(')')).trim();
        System.out.println(variables.getOrDefault(var_name, 0));
    }

    private int evaluate_expression(String expression) {
        if (expression.contains("+")) {
            String[] parts = expression.split("\\+");
            return evaluate_expression(parts[0].trim()) + evaluate_expression(parts[1].trim());
        }else if (expression.contains("-")) {
            String[] parts = expression.split("\\-");
            return evaluate_expression(parts[0].trim()) - evaluate_expression(parts[1].trim());
        }else if (expression.contains("*")) {
            String[] parts = expression.split("\\*");
            return evaluate_expression(parts[0].trim()) * evaluate_expression(parts[1].trim());
        }else if (expression.contains("/")) {
            String[] parts = expression.split("\\/");
            int denominator = evaluate_expression(parts[1].trim());
            // Handling division by zero
            if (denominator == 0) {
                throw new ArithmeticException("Division by zero");
            }
            return evaluate_expression(parts[0].trim()) / denominator;
        } else if (expression.contains("%")) {
            String[] parts = expression.split("\\%");
            return evaluate_expression(parts[0].trim()) % evaluate_expression(parts[1].trim());
        }else {
            if (variables.containsKey(expression)) {
                return variables.get(expression);
            } else {
                return Integer.parseInt(expression);
            }
        }
    }



    private boolean evaluate_condition(String condition) {
        if (condition.contains("<=")) {
            String[] parts = condition.split("<=");
            return evaluate_expression(parts[0].trim()) <= evaluate_expression(parts[1].trim());
        } else if (condition.contains(">=")) {
            String[] parts = condition.split(">=");
            return evaluate_expression(parts[0].trim()) >= evaluate_expression(parts[1].trim());
        } else if (condition.contains("==")) {
            String[] parts = condition.split("==");
            return evaluate_expression(parts[0].trim()) == evaluate_expression(parts[1].trim());
        } else if (condition.contains("!=")) {
            String[] parts = condition.split("!=");
            return evaluate_expression(parts[0].trim()) != evaluate_expression(parts[1].trim());
        } else if (condition.contains("<")) {
            String[] parts = condition.split("<");
            return evaluate_expression(parts[0].trim()) < evaluate_expression(parts[1].trim());
        } else if (condition.contains(">")) {
            String[] parts = condition.split(">");
            return evaluate_expression(parts[0].trim()) > evaluate_expression(parts[1].trim());
        } else {
            throw new IllegalArgumentException("Unsupported condition: " + condition);
        }
    }


    private int handleWhileLoop (String [] lines, int index){
        String line = lines[index];
        String loop_head = line.substring(line.indexOf("while") + 5, line.indexOf("{")).trim();


        StringBuilder loop_body = new StringBuilder();
        int braceCount = 1; // Count opening and closing braces
        for (index = index + 1; index < lines.length; index++) {
            String currentLine = lines[index].trim();

            if (currentLine.contains("{")) {
                braceCount++;
            }
            if (currentLine.contains("}")) {
                braceCount--;
            }

            if (braceCount == 0) {
                break;
            }

            loop_body.append(currentLine).append("\n");
        }

        // Execute the loop while the condition is true
        while (evaluate_condition(loop_head)) {
            eval(loop_body.toString());
        }

        return index; // Return the updated index
    }

}






