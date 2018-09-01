package teascript;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import static processing.core.PApplet.println;
import static teascript.Utils.help;
import static teascript.Utils.stringToFile;

/**
 * Main class: holds main Machine, other vars; handles startup and unit tests.
 *
 * @author chrx
 */
public class TeaScript {

    /** The Machine that all variables and functions are attached to. */
    static Machine m;
    /** Whether errors should be printed (true) or swallowed/pretty printed. */
    static boolean debugMode = false;
    /** Whether to run units.tea after specified file (from directory above). */
    static boolean runTests = false;
    /** Whether to print total run/loading time after everything finishes. */
    static boolean printTime = false;
    /** Start time of execution. */
    static long st;
    /** Registry of TeaScript interfaces for external function/data lookup. */
    static Tint[] tints;

    /**
     * Sets up st, list of failed tests, and m, then runs the test.tea script.
     *
     * @param args command line args--see Utils.help() for what they do.
     */
    public static void main(String[] args) {
        File f = null;
        int fileIndex = -1;
        int rawFileIndex = -1;
        for (int i = 0; i < args.length; i++) {
            String s = args[i];
            if (s.equals("-f") || s.equals("--file")) fileIndex = i + 1;
            if (s.equals("-p") || s.equals("--path")) rawFileIndex = i + 1;
            if (s.equals("-t") || s.equals("--test")) runTests = true;
            if (s.equals("-l") || s.equals("--logtime")) printTime = true;
            if (s.equals("-d") || s.equals("--debug")) debugMode = true;
            if (s.equals("-h") || s.equals("--help")) help();
        }
        if (fileIndex != -1) {
            f = stringToFile(args[fileIndex]);
        }
        if (rawFileIndex != -1) {
            f = new File(args[rawFileIndex]);
        }
        if (f != null) main(f, new Tint[0]);
        else println("\nno file specified for execution: try --help\n");
    }

    /** Runs TeaScript file (file) using tints_ to lookup external stuff. */
    public static void main(File file, Tint[] tints_) {
        tints = tints_;
        st = System.nanoTime();
        Tester.failed = new ArrayList<>();
        m = new Machine();
        m.init(file);
        m.action();
    }

    /** Runs unit tests and prints out run time. */
    public static void end() {
        if (runTests) {
            Tester.test();
        }
        if (printTime) {
            println("\ntime taken: " + (System.nanoTime() - st) / 1000000000. + " sec");
        }
    }
}
