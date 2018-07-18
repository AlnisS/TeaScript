/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package teascript;

import java.util.ArrayList;
import static processing.core.PApplet.println;
import static processing.core.PApplet.round;
import static teascript.TeaScript.m;

/**
 *
 * @author chrx
 */
public class Tester {

    static ArrayList<String> failed;

    static void test() {
        failed = new ArrayList<>();
        m = new Machine();
        m.init("units.tea");
        m.action();
        println("\n\n");
        if (failed.size() != 0) {
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
            if (round(fa * 1000) == round(fb * 1000)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
