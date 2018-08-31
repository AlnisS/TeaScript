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
    static boolean debugMode = false;
    static boolean runTests = false;
    static boolean printTime = false;
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
        File f = null;
        int fileIndex = -1;
        int rawFileIndex = -1;
        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-f")) fileIndex = i + 1;
            if(args[i].equals("-p")) rawFileIndex = i + 1;
            if(args[i].equals("-t")) runTests = true;
            if(args[i].equals("-l")) printTime = true;
            if(args[i].equals("-d")) debugMode = true;
        }
        if(fileIndex != -1) {
            f = stringToFile(args[fileIndex]);
        }
        if(rawFileIndex != -1) {
            f = new File(args[rawFileIndex]);
        }
        main(f, new Tint[0]);
    }

    public static void main(File file, Tint[] tints_) {
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
        if(printTime) {
            println("\ntime taken: " + (System.nanoTime() - st) / 1000000000. + " sec");
        }
    }
}
