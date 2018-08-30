package teascript;

import static teascript.FMan.feval;
import static teascript.FMan.isMushyFloat;
import static teascript.SMan.streval;

public class Demo {
    public static void main(String[] args) {
        Tint[] temp = {new DemoTint()};  // create registry of external function collections
        TeaScript.main("demo.tea", temp);  // start script with filename and function registry
    }

    // example function: returns float version of string
    public static float foo(String number) {
        switch (streval(number)) {
            case "one": return 1;
            case "two": return 2;
            case "three": return 3;
            default: return -3.14159f;  //not a number
        }
    }

    // example function: interfaces with TeaScript Machine to lookup floats
    // the evaluation systems handle any variable, function, math, etc. lookups (including calls to external functions!)
    public static float boo(String number1, String number2) {
        if (isMushyFloat(number1) && isMushyFloat(number2)) {
            return feval(number1) + feval(number2);
        }
        return -3.14159f;
    }

    //loudly prints a string
    public static void loudPrint(String s) {
        System.out.println("*** " + s + " ***");
    }
}
