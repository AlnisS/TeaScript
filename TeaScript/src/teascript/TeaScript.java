package teascript;

import java.util.ArrayList;
import static processing.core.PApplet.println;

/**
 * Main class: holds main Machine, other vars; handles startup and unit tests.
 *
 * @author chrx
 */
public class TeaScript {

    /**
     * The Machine that all variables and functions are attached to.
     */
    static Machine m;
    /**
     * Whether errors should be printed (true) or swallowed and pretty printed.
     */
    static boolean debugMode = true;
    /**
     * Start time of execution.
     */
    static long st;

    /**
     * Sets up st, list of failed tests, and m, then runs the test.tea script.
     *
     * @param args not currently used
     */
    public static void main(String[] args) {
        st = System.nanoTime();
        Tester.failed = new ArrayList<>();
        m = new Machine();
        m.init("test.tea");
        m.action();
    }

    /**
     * Runs unit tests and prints out run time.
     */
    public static void end() {
        Tester.test();
        println("\ntime taken: " + (System.nanoTime() - st) / 1000000000. + " sec");
    }
}
