import java.util.HashMap;
import java.util.Map;

public class SwiftInterpreter {
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
            if (line.endsWith("{")) {
                line = line.substring(0, line.length() - 1).trim();
            }
            if (line.contains("=") && !line.startsWith("for") && !line.startsWith("while") && !line.startsWith("if"))  {
                handle_assignment(line);
            }else if (line.startsWith("print")) {
                handle_print(line);
            } else if (line.startsWith("if")) {
                index = handle_if_else(lines, index);
            } else if (line.startsWith("while")) {
                index = handleWhileLoop(lines, index);
            } else if (line.startsWith("for")) {
                index = handleForLoop(lines, index);
            }
            else {
                throw new IllegalArgumentException("unrecognized statement: " + line);
            }
            index++;
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // handling variable assignment
    private void handle_assignment(String line) {
         line = line.replaceFirst("var ", "").trim(); // Remove "var" if present
         String[] parts = line.split("=", 2);
         String var_name = parts[0].trim();
         String expression = parts[1].trim();

          // for boolean
         if (expression.equals("true")) {
             variables.put(var_name,1);
         }
         else if (expression.equals("false")){
             variables.put(var_name, 0);
         }
    // for string
         else if (expression.startsWith("\"") && expression.endsWith("\"")) {
             String stringValue = expression.substring(1, expression.length() - 1);
             variables.put(var_name, stringValue.hashCode()); // Store strings as hashCodes
         }
    // for  integer
         else {
             int value = evaluate_expression(expression);
             variables.put(var_name, value);
    }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void handle_print(String line) {
    String content = line.substring(line.indexOf('(') + 1, line.lastIndexOf(')')).trim();
    //for string
    if (content.startsWith("\"") && content.endsWith("\"")) {
        System.out.println(content.substring(1, content.length() - 1));
    } else {
    // for integer
        int value = evaluate_expression(content);
        System.out.println(value);
    }
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
    //handling if-else
    private int handle_if_else(String[] lines, int index) {
        String if_line = lines[index].trim();
        String condition = if_line.substring(if_line.indexOf("if") + 2, if_line.indexOf("{")).trim();
        boolean condition_result = evaluate_condition(condition);

        index++;
        int if_end = find_matching_braces(lines, index);

        int elseif_start = if_end + 1;
        int elseif_end = -1;


        while (elseif_start < lines.length && lines[elseif_start].trim().startsWith("else if")) {
            String elseif_condition = lines[elseif_start].trim().substring("else if".length()).trim();
            if (evaluate_condition(elseif_condition)) {
                elseif_end = find_matching_braces(lines, elseif_start + 1);
                interpret_block(lines, elseif_start + 1, elseif_end);
                return elseif_end;
            }
            elseif_start = find_matching_braces(lines, elseif_start + 1) + 1;
        }

        int else_start = elseif_start;
        int else_end = -1;


        if (else_start < lines.length && lines[else_start].trim().startsWith("else")) {
            else_end = find_matching_braces(lines, else_start + 1);
            if (!condition_result) {
                interpret_block(lines, else_start + 1, else_end);
                return else_end;
            }
        }

        if (condition_result) {
            interpret_block(lines, index, if_end);
        }
        return Math.max(if_end, Math.max(elseif_end, else_end));
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // necessary helper methods for if-else
    private int find_matching_braces(String[] lines, int startIndex) {
        int braceCount = 1;
        int index = startIndex;
        while (index < lines.length) {
            String line = lines[index].trim();
            if (line.contains("{")) {
                braceCount++;
            }
            if (line.contains("}")) {
                braceCount--;
                if (braceCount == 0) {
                    return index;
                }
            }
            index++;
        }
        throw new IllegalArgumentException("Unmatched brace");
    }
///////////////////////////
    private void interpret_block(String[] lines, int startIndex, int endIndex) {
        for (int i = startIndex; i <= endIndex; i++) {
            String line = lines[i].trim();
            if (line.contains("=") && !line.startsWith("for") && !line.startsWith("if") && !line.startsWith("while")) {
                handle_assignment(line);
            } else if (line.startsWith("if")) {
                i = handle_if_else(lines, i);
            } else if (line.startsWith("for")) {
                i = handleForLoop(lines, i);
            } else if (line.startsWith("print")) {
                handle_print(line);
            }
        }
    }
/////////////////////////////////////////////////////////////////////////////////////////////////
    private int handleForLoop(String[] lines, int index) {
        String line = lines[index];
        String loop_head = line.substring(line.indexOf("for") + 3, line.indexOf("{")).trim();
        String[] loop_parts = loop_head.split("in");
        String loop_var = loop_parts[0].trim();
        String range_expr = loop_parts[1].trim().replace("...", ","); // Handle Swift's `...`


        String[] range_bound = range_expr.split(",");
        int start = evaluate_expression(range_bound[0].trim());
        int end = evaluate_expression(range_bound[1].trim());


        int loop_start = index + 1;
        StringBuilder loop_body = new StringBuilder();
        while (++index < lines.length && lines[index].startsWith("    ")) {
            loop_body.append(lines[index].trim()).append("\n");
        }
        index--;


        for (int i = start; i <= end; i++) {
            variables.put(loop_var, i);
            eval(loop_body.toString());
        }
        return index;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private int handleWhileLoop (String [] lines, int index){
        String line = lines[index];
        String loop_head = line.substring(line.indexOf("while") + 5, line.indexOf("{")).trim();


        StringBuilder loop_body = new StringBuilder();
        int braceCount = 1;
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

        while (evaluate_condition(loop_head)) {
            eval(loop_body.toString());
        }

        return index;
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


// Tested and proven to work with all algorithms
// Here are all algorithms in swift code for you to test as well ( + 1 additional just for fun :)) ):


    public static void main(String[] args) {
        SwiftInterpreter interpreter = new SwiftInterpreter();
// 1. Sum of First N Numbers ✔
//  Input: n = 10
//  Output: 55

        String input = """
                var sum = 0
                var n = 10
                for i in 1...n {
                    sum = sum + i
                }
                print(sum)

                                """;


//2. Factorial of N ✔
//  Input: n = 5
//  Output: 120

//           String input = """
//                   var fact = 1
//                   var n = 5
//                   for i in 1...n {
//                       fact = fact * i
//                   }
//                   print(fact)
//
//                   """;


//3. GCD of Two Numbers  ✔
//  Input: a = 48, b = 18
//  Output: 6

//        String input = """
//                var a = 48
//                var b = 18
//                while b != 0 {
//                    var temp = b
//                    var modulo = a % b
//                    b = modulo
//                    a = temp
//                }
//                print(a)
//
//                   """;


//4. Reverse a Number  ✔
//  Input: 1234
//  Output: 4321

//        String input = """
//                var num = 1234
//                var reversed = 0
//                while num > 0 {
//                    var digit = num % 10
//                    reversed = reversed * 10 + digit
//                    num = num / 10
//                }
//                print(reversed)
//
//                   """;


//5. Check if a Number is Prime ✔
//  Input: n = 13
//  Output: True ( 1 for true in our case, 0 - false)

//        String input = """
//                var n = 13
//                var isPrime = true
//                if n <= 1 {
//                   isPrime = false
//                }
//                else {
//                   for i in 2...n-1 {
//                       if n % i == 0 {
//                           isPrime = false
//                           break
//                       }
//                   }
//                }
//                print(isPrime)
//                   """;


//6. Check if a Number is Palindrome ✔
//  Input: 121
//  Output: True

//        String input = """
//                var num = 112211
//                var orig = num
//                var reversed = 0
//                while num > 0 {
//                     var digit = num % 10
//                     reversed = reversed * 10 + digit
//                     num = num / 10
//                }
//                if reversed == orig {
//                       print("true")
//                }
//                else{
//                       print("false")
//                }
//
//                   """;


//7. Find the Largest Digit in a Number ✔
//  Input: n = 3947
//  Output: 9

//        String input = """
//                var n = 3947
//                var largest = 0
//                while n > 0 {
//                    var digit = n % 10
//                    if digit > largest {
//                        largest = digit
//                    }
//                    n = n / 10
//                }
//                print(largest)
//                   """;


//8. Sum of Digits ✔
//  Input: 1234
//  Output: 10

//        String input = """
//                var num = 1234
//                var sum = 0
//                while num > 0 {
//                    var digit = num % 10
//                    sum = sum + digit
//                    num = num / 10
//                }
//                print(sum)
//
//                   """;


//9. Multiplication Table ✔
//  Input: 5
//  Output:
//          5
//          10
//          15
//          20
//          25
//          30
//          35
//          40
//          45
//          50

//        String input = """
//                var num = 5
//                for i in 1...10 {
//                    var product = num * i
//                    print(product)
//                }
//                   """;


//10. Nth Fibonacci Number ✔
//  Input:n = 10
//  Output: 34

//        String input = """
//                var n = 10
//                var a = 0
//                var b = 1
//                var fib = 0
//                if n == 1 {
//                  fib = a
//                }
//                if n == 2 {
//                  fib = b
//                }
//                else {
//                  for i in 3...n {
//                      fib = a + b
//                      a = b
//                      b = fib
//                  }
//                }
//                print(fib)
//                   """;

// 11.additional algorithm to test:  Checking if first n numbers are odd or even:

//        String input = """
//                var n = 10
//                for number in 1...n {
//                    if number % 2 == 0 {
//                        print(number )
//                        print("is even")
//                    }
//                    else {
//                         print(number)
//                         print("is odd")
//                    }
//                }
//                """;

        interpreter.eval(input);
    }


}






