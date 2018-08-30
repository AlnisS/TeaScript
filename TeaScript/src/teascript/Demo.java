package teascript;

import static teascript.Nice.*;

public class Demo {
    public static void main(String[] args) {
        // add to registry of external function collections
        addTint(new DemoTint());
        // start script with filename and function registry
        start("demo.tea");
    }

    // example function: returns float version of string
    public static float foo(String number) {
        switch (stringEval(number)) {
            case "one": return 1;
            case "two": return 2;
            case "three": return 3;
            default: return -3.14159f;  //not a number
        }
    }

    /*
     * Example function: interfaces with TeaScript Machine to lookup floats.
     * The evaluation systems handle any variable, function, math, etc. lookups,
     * including functions like this one, including recursion!
     */
    public static float boo(String number1, String number2) {
        if (isFloat(number1) && isFloat(number2)) {
            return floatEval(number1) + floatEval(number2);
        }
        return -3.14159f;
    }

    /*
     * Loudly prints a string using the TeaScript print system. PRINT prints
     * using an ID (raw string) as the first argument, and content to evaluate
     * as the second. In this case, the content is a string, so it must be
     * enclosed in quotes. Finally, the Action must be executed. Because a
     * TeaScript function is not calling/doing this Action, its parent function
     * does not exist and is specified as null.
     */
    public static void loudPrint(String s) {
        executeAction("PRINT(LOUD," +
                "\"" + "*** " + s + " ***" + "\"" + ")");
    }
}
