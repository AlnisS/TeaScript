package teascript;

import java.io.File;
import java.util.ArrayList;
import static processing.core.PApplet.println;
import static teascript.Utils.stringToFile;

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
    static boolean runTests = true;
    /**
     * Start time of execution.
     */
    static long st;

    static Tint[] tints;

    /**
     * Sets up st, list of failed tests, and m, then runs the test.tea script.
     *
     * @param args not currently used
     */
    public static void main(String[] args) {
        main(stringToFile("test.tea"), new Tint[0]);
    }

    public static void main(File file, Tint[] tints_) {
        runTests = true;
        main(file, tints_, false);
    }

    public static void main(File file, Tint[] tints_, boolean debug) {
        debugMode = debug;
        tints = tints_;
        st = System.nanoTime();
        Tester.failed = new ArrayList<>();
        m = new Machine();
        m.init(file);
        m.action();
    }

    /**
     * Runs unit tests and prints out run time.
     */
    public static void end() {
        if(runTests) {
            Tester.test();
        }
        println("\ntime taken: " + (System.nanoTime() - st) / 1000000000. + " sec");
    }
}
