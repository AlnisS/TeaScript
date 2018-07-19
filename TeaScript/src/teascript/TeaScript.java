package teascript;

import java.util.ArrayList;
import static processing.core.PApplet.println;

public class TeaScript {

    static Machine m;
    static boolean debugMode = true;
    static long st;
    
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
