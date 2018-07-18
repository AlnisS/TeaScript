/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 *
 * @author alnis
 */
public class Machine {

    int debugline;
    Action[] actions;
    IntDict labels;
    ArrayList<FloatDict> floats;
    ArrayList<StringDict> strings;
    ArrayList<IntDict> booleans;
    int labeltemp;
    HashMap<String, Function> functions;
    HashMap<String, Function> sfunctions;
    HashMap<String, Function> bfunctions;
    ArrayList<HashMap<String, ArrayList<Float>>> farrs;
    ArrayList<HashMap<String, ArrayList<String>>> sarrs;
    ArrayList<HashMap<String, ArrayList<Boolean>>> barrs;
    String[] rawstrings;

    public Machine() {

    }

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
