# TeaScript
TeaScript is an interpreted scripting language intended for quickly putting together high-level instructions along with doing math and data processing to control program flow while retaining an emphasis on rapid and easy script modification along with easy integration with other projects.

Check out http://teascript.bitballoon.com/ for javadoc documentation. The Action class has all of the instructions usable in a script. Check out the unit test file along with the test program file to see how to set stuff up. More documentation will come soon.

# Integrating
Integration is pretty simple. There are two TeaScript classes involved, and you need to make 0-3 new classes for your project depending on how clean you want the integration to be. To more easily understand how to integrate TeaScript into your project, here is a quick overview of how the relevant parts of TeaScript work:

  - In TeaScript, there are 3 basic data types: floats, strings, and booleans. There are also arrays/lists of these, but these are currently slightly sketchy, so don't worry about them for now.
  - When evaluating an expression, TeaScript recursively subdivides the expression into sub-expressions until a raw value or lookup-able value is found (this is done following the order of operations including parentheses etc.). Lookupable values might be variables or function calls (including math operations, user defined functions in TeaScript scripts, or Java functions added by a TeaScript interface).
  - TeaScript interfaces allow a script to access functions and data outside of the script and in other Java classes. They add functions to those the evaluation system can see and use. They also add void functions which can be called with the SYSFUN (system function) command in TeaScript.
  - TeaScript interfaces are created by extending the abstract class Tint.java. The reason for extending and abstract class instead of implementing an interface is that when an interface is implemented, all methods must be implemented (which is not always necessary), it increases boilerplate slightly, and it allows adding an interface to an existing class (which would be a bad idea with a TeaScript interface because of the very single-tracked purpose).
  - TeaScript interfaces consist of two types of methods which may be implemented (but each always needs a pair of the other type): hasTypeByName(String name) and typeByName(String[] info). The types are float, string, boolean, and void.
  - When "implementing" a Tint, the methods are added in pairs. Here is an example because explaining it in prose is not really productive (foo and boo are not the pair; the `has...()` and `...()` are the pair):

~~~~
// name is always just the name of the method being checked
// it never has arguments, parentheses, etc. attached
public boolean hasFloatByName(String name) {
    switch (name) {
        case "foo": return true;
        case "boo": return true;
        default: return false;
    }
}

// info is an array representation of the name and arguments:
// somefun("foo", 3.14159, true, "flase") will give the array:
// { "somefun", "\"foo\"", "3.14159", "true", "\"flase\"" }
public float floatByName(String[] info) {
    switch (info[0]) {
        case "foo": return SomeClass.foo(info[1]);
        case "boo": return SomeClass.boo(info[1], info[2]);
        default: return -3.14159f; //arbitrary
    }
}
~~~~

  - This would be the 1st of 3 classes for clean integration. The next one would be the one (or many) which actually has/have the Java methods referenced by the Tint "implementation" class. In this example, that was SomeClass.java. However, you can have methods from many classes included in one Tint. For example, the foo and boo methods could be from two different classes. Also, remember that you are not limited to one Tint "implementing" class. Just make sure to add all of them properly with `addTint()` as shown in the final implementation example. For example, you might separate them such that one Tint "implementation" works with a drive system, another with another subsystem, etc.
  - When writing the class, you will almost certainly encounter Strings which need to be evaluated to some value. In other cases, you might want to evaluate that String already in the Tint class.
  - For example, you might have a method `setMotorPower(String name, float power)`. Remember that the Strings in info are not actually the Strings you probably want: for example, if it is a TeaScript raw string, it will have quotation marks. It also might be a variable or even a function call (perhaps to another method via the Tint(s)). Therefore, you want the evaluated value:
  - You can import the Nice.java class and it will make it pretty nice for you to work with TeaScript in your Java classes. To evaluate a String from info to get the resulting String, simply do `String actualString = Nice.stringEval(infoString);`. Similarly, you can do `float actualFloat = Nice.floatEval(infoString)`. This works with booleans, too.
  - A good rule of thumb is to always use the evaluate methods when grabbing data from TeaScript. You can even do stuff like `float goodVariableName = Nice.floatEval("variableWhichExists")` to grab the float value of `variableWhichExists` from TeaScript (but you have to be sure that there will be a variable by that name at that time in the script!).
  - In some cases, like the setMotorPower case, it might be a good idea to do the evaluation within the Tint so you don't have to make another method expressly for the purpose of evaluating the info Strings, like this:
~~~~
//SomeClass.setMotorPower(String name, float power)
case "setMotorPower":
    return SomeClass.setMotorPower(stringEval(info[1]),
            floatEval(info[2]));
~~~~
  - In other cases, you might have special methods expressly for the purpose of working with TeaScript (such as exposing logging utilities, device features, etc.), so instead of having a Tint class (or classes) with a lot of long and hard to read cases which fiddle with the info Strings, you could just have a dedicated class which imports Nice and does the data passing around where it makes more sense. This would be the 2nd class in a clean implementation.
  - The final class is the shortest: it simply assembles all of the Tint "implementations" and passes those, along with starting TeaScript with the appropriate .tea file. Here is an example:
~~~~
import static teascript.Nice;

public class TeaScriptStarter {
    public static void startTeaScript(File file) {
        addTint(new SomeTint());
        // ^ repeat for all Tint "implementations" you're using
        start(file);
        //put stuff here if needed to close devices etc. if needed
    }
}
~~~~
  - Don't worry about having to clean up TeaScript data/stuff/whatever. When you call `start(file)`, it resets the TeaScript system like it hadn't run before. This would be the 3rd class in a clean integration. Of course, you can squeeze all 3+ into one class (or even into existing classes) but it will probably be harder to understand. Also remember that 3 is definitely not the limit: you can have as many Tint "implementations" and intermediate processing classes as is productive.

Thanks for reading!