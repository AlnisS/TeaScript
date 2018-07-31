package teascript;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import processing.data.FloatDict;
import processing.data.IntDict;
import processing.data.StringDict;
import static teascript.Action.Type.*;
import static teascript.TeaScript.*;
import static teascript.Utils.loadStrings;

/**
 * Holds variables, Actions, Functions; handles setup and overall orchestration.
 * Also has flags for the current line along with other misc data for setup.
 * Runs an initial pass to define all global variables, labels, and functions.
 * Handles swallowing script errors and makes sure stuff is mostly in order.
 *
 * @author alnis
 */
public class Machine {

    /**
     * Reference line written to for debugging and stuff when errors are thrown.
     */
    int debugline;
    /**
     * All of the Actions in the script file in a single, absolute array.
     */
    Action[] actions;
    /**
     * Dictionary of label names to absolute/global line numbers.
     */
    IntDict labels;
    /**
     * List of dictionaries (one for each "scope") of names -> int var vals.
     */
    ArrayList<FloatDict> floats;
    /**
     * List of dictionaries (one for each "scope") of names -> string var vals.
     */
    ArrayList<StringDict> strings;
    /**
     * List of dictionaries (one for each "scope") of names -> boolean var vals.
     */
    ArrayList<IntDict> booleans;
    /**
     * Used during setup for labels/functions to know where they are.
     */
    int labeltemp;
    /**
     * Function names -> float functions.
     */
    HashMap<String, Function> functions;
    /**
     * Function names -> string functions.
     */
    HashMap<String, Function> sfunctions;
    /**
     * Function names -> boolean functions.
     */
    HashMap<String, Function> bfunctions;
    /**
     * List (one map/"scope") of hashmaps for array name -> array of floats.
     */
    ArrayList<HashMap<String, ArrayList<Float>>> farrs;
    /**
     * List (one map/"scope") of hashmaps for array name -> array of strings.
     */
    ArrayList<HashMap<String, ArrayList<String>>> sarrs;
    /**
     * List (one map/"scope") of hashmaps for array name -> array of booleans.
     */
    ArrayList<HashMap<String, ArrayList<Boolean>>> barrs;
    /**
     * Raw string contents of each line in the script file.
     */
    String[] rawstrings;

    /**
     * Doesn't init anything, use init to actually setup this Machine.
     */
    public Machine() {

    }

    /**
     * Inits all varlists, reads file_ to load/setup Actions and functions. May
     * be called more than once to reset and use a new file. Makes new dicts,
     * arraylists, hashmaps, etc. for all applicable variable storage units.
     * Loads the file into rawstrings with one line/array item. File is
     * specified relative to folder containing NetBeans project. Parses all
     * strings into Actions and adds them to the array of Actions. Executes any
     * function define, global var define, and label define Actions. Finally,
     * runs the <code>init()</code> function in the script file.
     *
     * @param file_
     */
    void init(String file_) {
        String f = System.getProperty("user.dir");
        f = f.substring(0, f.lastIndexOf("/") + 1);
        String file = f + file_;
        labels = new IntDict();
        floats = new ArrayList<>();
        strings = new ArrayList<>();
        booleans = new ArrayList<>();
        functions = new HashMap<>();
        sfunctions = new HashMap<>();
        bfunctions = new HashMap<>();
        farrs = new ArrayList<>();
        sarrs = new ArrayList<>();
        barrs = new ArrayList<>();
        floats.add(new FloatDict());
        strings.add(new StringDict());
        booleans.add(new IntDict());
        farrs.add(new HashMap<>());
        sarrs.add(new HashMap<>());
        barrs.add(new HashMap<>());

        rawstrings = loadStrings(new File(file));
        actions = new Action[rawstrings.length];
        for (int i = 0; i < actions.length; i++) {
            debugline = i;
            actions[i] = new Action(rawstrings[i]);
        }

        for (int i = 0; i < actions.length; i++) {
            Action a = actions[i];
            labeltemp = i;
            if (a.type == LABEL || a.type == FDEF || a.type == GVAR) {
                a.execute(null);
            }
        }
        new Action("USERFUN(init())").execute(null);
    }

    /**
     * Runs <code>main()</code> function in script file, swallowing errors if
     * !debugMode. Swallowing errors == catch all exceptions by doing nothing.
     */
    void action() {
        if (debugMode) {
            new Action("USERFUN(main())").execute(null);
        } else {
            try {
                new Action("USERFUN(main())").execute(null);
            } catch (AssertionError | Exception e) {
                //exit();
            }
        }
    }
}
