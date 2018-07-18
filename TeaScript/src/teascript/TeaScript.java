/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package teascript;

import static processing.core.PApplet.println;

/**
 *
 * @author chrx
 */
public class TeaScript {

    static Machine m;
    static boolean debugMode = true;
    static Tester tester;
    static long st;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        st = System.nanoTime();
        tester = new Tester();
        m = new Machine();
        m.init("test.tea");
        m.action();
    }
    public static void end() {
        tester.test();
        println("\ntime taken: " + (System.nanoTime()-st)/1000000000. + " sec");
    }
}
