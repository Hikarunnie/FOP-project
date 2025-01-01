import java.util.HashMap;
import java.util.Map;


public class SwiftInterpreter {
    private final Map<String, Integer> variables = new HashMap<>();       // Stored variables and their values in a map for easy access
    public void eval(String code) {                                       // evaluating the provided code line by line
        String[] lines = code.split("\\r?\\n");                     // splitting the code into individual lines
        int index = 0;
        while (index < lines.length) {                                    // looping through each line of code
            String line = lines[index].trim();
            if (line.isEmpty() || line.equals("}")) {                     // skipping empty lines or closing braces
                index++;
                continue;
            }
            if (line.endsWith("{")) {                                     // removing braces from block start
                line = line.substring(0, line.length() - 1).trim();
            }
            if (line.contains("=") && !line.startsWith("for") && !line.startsWith("while") && !line.startsWith("if"))  {
                handle_assignment(line);                                 // handling assignment statements (excluding loops and conditionals)
            }else if (line.startsWith("print")) {                        // handling print statements
                handle_print(line);
            } else if (line.startsWith("if")) {                          // handling if-else statements
                index = handle_if_else(lines, index);
            } else if (line.startsWith("while")) {                       // handling while loop
                index = handleWhileLoop(lines, index);
            } else if (line.startsWith("for")) {                         // handling for loop
                index = handleForLoop(lines, index);
            }
            else {                                                        // throwing an error for unrecognized statements
                throw new IllegalArgumentException("unrecognized statement: " + line);
            }
            index++;                                                      // moving to the next line
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // handling variable assignment
    private void handle_assignment(String line) {
        line = line.replaceFirst("var ", "").replaceFirst("let ", "").trim(); // Remove "var" or "let" if present
        String[] parts = line.split("=", 2);                   // splitting  line into variable name and expression
        String var_name = parts[0].trim();                                // variable name
        String expression = parts[1].trim();                              // variable expression


        // for boolean
        if (expression.equals("true")) {                                  //if true
            variables.put(var_name,1);                                    // storing 1 for true
        }
        else if (expression.equals("false")){                             //if false
            variables.put(var_name, 0);                                   // storing 0 for false
        }
        // for string
        else if (expression.startsWith("\"") && expression.endsWith("\"")) { //if string
            String stringValue = expression.substring(1, expression.length() - 1);//removing surrounding quotes and getting inside content using substring()
            variables.put(var_name, stringValue.hashCode());              // Store strings as hashCodes
        }
        // for  integer,
        else {
            int value = evaluate_expression(expression);                  // evaluating the expression(view below) for integer value
            variables.put(var_name, value);                               // storing the evaluated integer value
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void handle_print(String line) {
        String content = line.substring(line.indexOf('(') + 1, line.lastIndexOf(')')).trim(); //extracting content inside parentheses by trimming and indexing.
        //for string
        if (content.startsWith("\"") && content.endsWith("\"")) {           // if string
            System.out.println(content.substring(1, content.length() - 1)); // removing quotes and printing inside content using .substring()
        } else {
            // for integer
            int value = evaluate_expression(content);                       // handling integer content by evaluating the expression(view below)
            System.out.println(value);                                      // printing evaluated integer value
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private int evaluate_expression(String expression) {
        if (expression.contains("+")) {                                     //checking for addition
            String[] parts = expression.split("\\+");                 // splitting expression into two parts around the "+" symbol
            return evaluate_expression(parts[0].trim()) + evaluate_expression(parts[1].trim()); // recursively evaluating both parts and adding them
        }else if (expression.contains("-")) {                               // for -
            String[] parts = expression.split("\\-");
            return evaluate_expression(parts[0].trim()) - evaluate_expression(parts[1].trim());
        }else if (expression.contains("*")) {                               // for *
            String[] parts = expression.split("\\*");
            return evaluate_expression(parts[0].trim()) * evaluate_expression(parts[1].trim());
        }else if (expression.contains("/")) {
            String[] parts = expression.split("\\/");                 // splitting the expression into two parts around the "/" symbol
            int denominator = evaluate_expression(parts[1].trim());         // recursively evaluating denominator
            // Handling division by zero
            if (denominator == 0) {
                throw new ArithmeticException("Division by zero");          // throwing an exception if the denominator is zero
            }
            return evaluate_expression(parts[0].trim()) / denominator;
        } else if (expression.contains("%")) {                              // for %
            String[] parts = expression.split("\\%");
            return evaluate_expression(parts[0].trim()) % evaluate_expression(parts[1].trim());
        }else { // if the expression doesn't contain any arithmetic operator, it may be a variable or constant
            if (variables.containsKey(expression)) {                        // checking if the expression is in variables map
                return variables.get(expression);                           // returning the value of the variable
            } else {// if it's not a variable, assuming it's an integer value and parsing it
                return Integer.parseInt(expression);                        // parsing and returning the integer value
            }
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private boolean evaluate_condition(String condition) {
        if (condition.contains("<=")) {                                      // checking for less than or equal to condition (<=)
            String[] parts = condition.split("<=");                    // splitting the condition into two parts
            return evaluate_expression(parts[0].trim()) <= evaluate_expression(parts[1].trim()); //evaluating both expressions
        } else if (condition.contains(">=")) {                               // for >=
            String[] parts = condition.split(">=");
            return evaluate_expression(parts[0].trim()) >= evaluate_expression(parts[1].trim());
        } else if (condition.contains("==")) {                               // for ==
            String[] parts = condition.split("==");
            return evaluate_expression(parts[0].trim()) == evaluate_expression(parts[1].trim());
        } else if (condition.contains("!=")) {                               // for !=
            String[] parts = condition.split("!=");
            return evaluate_expression(parts[0].trim()) != evaluate_expression(parts[1].trim());
        } else if (condition.contains("<")) {                               // for <
            String[] parts = condition.split("<");
            return evaluate_expression(parts[0].trim()) < evaluate_expression(parts[1].trim());
        } else if (condition.contains(">")) {                               // for >
            String[] parts = condition.split(">");
            return evaluate_expression(parts[0].trim()) > evaluate_expression(parts[1].trim());
        } else {
            throw new IllegalArgumentException("Unsupported condition: " + condition);
        }                                                                   // throwing an exception if the condition is not recognized
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //handling if-else
    private int handle_if_else(String[] lines, int index) {
        String if_line = lines[index].trim();                               // extracting the condition
        String condition = if_line.substring(if_line.indexOf("if") + 2, if_line.indexOf("{")).trim();
        boolean condition_result = evaluate_condition(condition);           // evaluating the condition of the 'if' statement
        index++;                                                            // moving to the next line after the 'if' statement
        int if_end = find_matching_braces(lines, index);                    // finding the matching closing brace for the 'if' block(view below)


        int elseif_start = if_end + 1;                                      //evaluating else if
        int elseif_end = -1;


        // iterating through 'else if' block
        while (elseif_start < lines.length && lines[elseif_start].trim().startsWith("else if")) {
            String elseif_condition = lines[elseif_start].trim().substring("else if".length()).trim(); // Extract the condition
            // if the condition is true, execute the corresponding block
            if (evaluate_condition(elseif_condition)) {
                elseif_end = find_matching_braces(lines, elseif_start + 1);       //finding the end by matching braces
                interpret_block(lines, elseif_start + 1, elseif_end);             // interprets statements inside this else if
                return elseif_end;
            }
            elseif_start = find_matching_braces(lines, elseif_start + 1) + 1;// if the condition is false, moves to the next block
        }
        // Start of the else block (if any)
        int else_start = elseif_start;
        int else_end = -1;


        if (else_start < lines.length && lines[else_start].trim().startsWith("else")) {    // Checks for an 'else' block at the end
            else_end = find_matching_braces(lines, else_start + 1);               // finding the end
            if (!condition_result) {                                                       // if the if condition was false, executes the 'else' block
                interpret_block(lines, else_start + 1, else_end);
                return else_end;
            }
        }
        // if the initial 'if' condition was true, executes the corresponding block
        if (condition_result) {
            interpret_block(lines, index, if_end);
        }
        // returns the maximum of the indices to ensure we continue after the correct block (if, else if, or else)
        return Math.max(if_end, Math.max(elseif_end, else_end));
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // necessary helper methods for if-else
    private int find_matching_braces(String[] lines, int startIndex) {
        int braceCount = 1;                                         // starts with 1 to account for the opening brace '{'
        int index = startIndex;                                     // starts looking from the given index
        while (index < lines.length) {                              // loops through the lines to find the matching closing brace '}'
            String line = lines[index].trim();                      // gets the current line
            if (line.contains("{")) {                               // if an opening brace '{' is encountered, increments brace count
                braceCount++;                                       // if an opening brace '{' is encountered, increments brace count
            }
            if (line.contains("}")) {
                braceCount--;                                       // if a closing brace '}' is encountered, decrements brace count
                if (braceCount == 0) {                              // when brace count reaches zero, the matching closing brace is found
                    return index;                                   // returns the index of the matching closing brace
                }
            }
            index++;                                                // moves to the next line
        }
        throw new IllegalArgumentException("Unmatched brace");      // if no matching closing brace is found, throws an exception
    }
    ///////////////////////////
    private void interpret_block(String[] lines, int startIndex, int endIndex) {
        for (int i = startIndex; i <= endIndex; i++) {              // loops through each line in the block from start to end index
            String line = lines[i].trim();                          // gets the current line
            if (line.contains("=") && !line.startsWith("for") && !line.startsWith("if") && !line.startsWith("while")) {  //this line was cause of so many erroooorsss
                handle_assignment(line);                            // handls assignments(excluding loops and conditionals)
            } else if (line.startsWith("if")) {
                i = handle_if_else(lines, i);                       // if "if  - executes the corresponding if-else block
            } else if (line.startsWith("for")) {
                i = handleForLoop(lines, i);                        // if for - executes the corresponding for loop
            } else if (line.startsWith("print")) {                  // if print - prints the output
                handle_print(line);
            }
        }
    }//even tho eval already does this, without this function if-else wouldn't work for some reaason
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private int handleForLoop(String[] lines, int index) {
        String line = lines[index];
        String loop_head = line.substring(line.indexOf("for") + 3, line.indexOf("{")).trim(); // extracts the loop declaration
        String[] loop_parts = loop_head.split("in");                                    // splits the loop declaration into variable and range parts
        String loop_var = loop_parts[0].trim();
        String range_expr = loop_parts[1].trim().replace("...", ",");        // Handle Swift's `...`


        // splits the range expression into start and end values
        String[] range_bound = range_expr.split(",");
        int start = evaluate_expression(range_bound[0].trim());                               // start of the range
        int end = evaluate_expression(range_bound[1].trim());                                 // end of the range


        int loop_start = index + 1;                                                           // identifies the start of the loop body
        StringBuilder loop_body = new StringBuilder();
        // collects the lines of the loop body
        while (++index < lines.length && lines[index].startsWith("    ")) {
            loop_body.append(lines[index].trim()).append("\n");
        }
        index--;


        // starts the loop, iterating from start to end
        for (int i = start; i <= end; i++) {
            variables.put(loop_var, i);                                                       // assigns the current loop variable value
            eval(loop_body.toString());                                                       // evaluates the loop body
        }
        return index;
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private int handleWhileLoop (String [] lines, int index){
        String line = lines[index];
        String loop_head = line.substring(line.indexOf("while") + 5, line.indexOf("{")).trim();// extracts the condition




        StringBuilder loop_body = new StringBuilder();                               // stores the body of the while loop
        int braceCount = 1;                                                          // keeps track of brace pairs to handle nested blocks
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


            loop_body.append(currentLine).append("\n");                              // appends the line to the loop body
        }


        while (evaluate_condition(loop_head)) {                                      // starts looping as long as the condition is true
            eval(loop_body.toString());                                              // executes the body of the loop
        }


        return index;
    }  // this was written before creation of find_matching_braces method
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
//                       print("True")
//                }
//                else{
//                       print("False")
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













