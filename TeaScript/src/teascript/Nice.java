package teascript;

import java.io.File;
import java.util.ArrayList;

import static teascript.BMan.beval;
import static teascript.FMan.feval;
import static teascript.FMan.isMushyFloat;
import static teascript.SMan.streval;
import static teascript.Utils.stringToFile;

public class Nice {
    public static ArrayList<Tint> tints = new ArrayList<>();
    public static void addTint(Tint t) { tints.add(t); }
    public static void start(String file) {
        start(stringToFile(file));
    }
    public static void start(File file) {
        TeaScript.main(file, tints.toArray(new Tint[tints.size()]));
    }
    public static void init(String file)  {
        init(stringToFile(file));
    }
    public static void init(File file)  {
        TeaScript.init(file, tints.toArray(new Tint[tints.size()]));
    }

    public static float floatEval(String s) { return feval(s); }
    public static String stringEval(String s) { return streval(s); }
    public static boolean booleanEval(String s) { return beval(s); }
    public static boolean isFloat(String s) { return isMushyFloat(s); }
    public static boolean isString(String s) { return SMan.isString(s); }
    public static boolean isBoolean(String s) { return BMan.isBoolean(s); }

    public static void executeAction(String s) { new Action(s).execute(null); }

}
