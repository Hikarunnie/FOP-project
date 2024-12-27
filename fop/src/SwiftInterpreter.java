import java.util.HashMap;
import java.util.Map;


public class SwiftInterpreter{
    public static void main(String[] args) {
        SwiftInterpreter interpreter = new SwiftInterpreter();
        String input = """
                var n = 5
                var m = 10 + 5
                var sum = n + m
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
            }
            else if (line.startsWith("print")) {
                handle_print(line);
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
        }else {
            if (variables.containsKey(expression)) {
                return variables.get(expression);
            } else {
                return Integer.parseInt(expression);
            }
        }
    }

}






