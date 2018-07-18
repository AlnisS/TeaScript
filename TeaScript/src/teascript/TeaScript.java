/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package teascript;

import java.util.ArrayList;
import static processing.core.PApplet.println;

/**
 *
 * @author alnis
 */
public class TeaScript {

    static Machine m;
    static boolean debugMode = true;
    static long st;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        st = System.nanoTime();
        Tester.failed = new ArrayList<>();
        m = new Machine();
        m.init("test.tea");
        m.action();
    }
    public static void end() {
        Tester.test();
        println("\ntime taken: " + (System.nanoTime()-st)/1000000000. + " sec");
    }
}
