import java.util.HashMap;
import java.util.Map;

public class SwiftInterpreter {

    public static void main(String[] args) {
        SwiftInterpreter interpreter = new SwiftInterpreter();
        String input = """
               var a = 48
                var b = 18
                while b != 0 {
                var temp = b
                var modulo = a % b
                 b = modulo
                 a = temp
                 }
                print(a)
                             
                """;
        interpreter.eval(input);
    }

    private final Map<String, Integer> variables = new HashMap<>();

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void eval(String code) {
        String[] lines = code.split("\\r?\\n");
        int index = 0;
        while (index < lines.length) {
            String line = lines[index].trim();
            if (line.isEmpty() || line.equals("}")) {
                index++;
                continue;
            }
            if (line.endsWith("{")) {
                line = line.substring(0, line.length() - 1).trim();
            }
            if (line.contains("=") && !line.startsWith("for") && !line.startsWith("while")) {
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
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void handle_assignment(String line) {
         line = line.replaceFirst("var ", "").trim(); // Remove "var" if present
         String[] parts = line.split("=", 2);
         String var_name = parts[0].trim();
         String expression = parts[1].trim();

          // in case of boolean
         if (expression.equals("true")) {
             variables.put(var_name,1);
         }
         else if (expression.equals("false")){
             variables.put(var_name, 0);
         }
    // in case of a string
         else if (expression.startsWith("\"") && expression.endsWith("\"")) {
             String stringValue = expression.substring(1, expression.length() - 1); // Remove quotes
             variables.put(var_name, stringValue.hashCode()); // Store strings as hashCodes (for simplicity)
         }
    // for  integer
         else {
             int value = evaluate_expression(expression);
             variables.put(var_name, value);
    }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void handle_print(String line) {
        String var_name = line.substring(line.indexOf('(') + 1, line.indexOf(')')).trim();
        System.out.println(variables.getOrDefault(var_name, 0));
    }


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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






