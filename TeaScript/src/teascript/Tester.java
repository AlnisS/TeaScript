package teascript;

import java.util.ArrayList;
import static processing.core.PApplet.println;
import static processing.core.PApplet.round;
import static teascript.TeaScript.m;
import static teascript.Utils.stringToFile;

public class Tester {

    static ArrayList<String> failed;

    static void test() {
        failed = new ArrayList<>();
        m = new Machine();
        m.init(stringToFile("units.tea"));
        m.action();
        println("\n\n");
        if (!failed.isEmpty()) {
            println("failed tests:");
            for (String s : failed) {
                println(s);
            }
        } else {
            println("all tests passed!");
        }
    }

    static void prettyUnitPass(String id, String a, String b) {
        if (unitPass(a, b)) {
            println(id + "\tpass");
        } else {
            println(id + "\tfail\t" + a + "\t" + b);
            failed.add(id + "\tfail\t" + a + "\t" + b);
        }
    }

    static boolean unitPass(String a, String b) {
        if (a.equals(b)) {
            return true;
        }
        try {
            float fa = Float.parseFloat(a);
            float fb = Float.parseFloat(b);
            return round(fa * 1000) == round(fb * 1000);
        } catch (Exception e) {
            return false;
        }
    }
}
