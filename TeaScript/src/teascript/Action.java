package teascript;

import java.util.ArrayList;
import java.util.HashMap;
import processing.data.*;
import static teascript.TeaScript.m;
import static teascript.Tester.prettyUnitPass;
import static teascript.BMan.*;
import static teascript.FMan.*;
import static teascript.SMan.*;
import static teascript.Utils.*;
import static processing.core.PApplet.print;
import static processing.core.PApplet.println;
import static processing.core.PApplet.str;
import static processing.core.PApplet.trim;

/**
 * Represents a single line of script and handles its execution. Contains code
 * for what to do for every type of instruction, keeps track of what instruction
 * it is and its arguments, and handles its execution.
 *
 * @author alnis
 */
public class Action {

    /**
     * Raw string passed to it during creation.
     */
    String creationString;
    /**
     * <code>creationString</code> split into array like {"FUNCTION", "arg_a",
     * "arg_b", "arg_c", etc.}.
     */
    String[] splits;
    /**
     * The <code>Type</code> of this Action (from splits[0]).
     */
    Type type;
    /**
     * Function on which to call instructions and get actions/caches
     */
    Function parentFun;

    /**
     * Prints first arg ("N" if only one arg) + tab + evaluated second arg. More
     * specifically, if there are two arguments, it prints first argument + tab
     * + evaluated second argument. If there is one argument, it prints "N" +
     * tab + evaluated first argument.
     *
     * <p>
     * If there is only one argument, it modifies <code>splits</code> to be as
     * though the function call was <code>PRINT("N", [original first
     * argument])</code>. It then evaluates splits[2] and prints that to the
     * console. For a version running on a device without a nice console, this
     * could be changed to print to a log file for easier debugging.
     * </p>
     */
    void PRINT() {
        if (splits.length < 3) {
            splits = new String[]{splits[0], "N", splits[1]};
        }
        String tmp = streval(splits[2]);
        println(splits[1] + "\t" + tmp);
    }

    /**
     * Jumps to the label specified in the first argument. This is not safe to
     * use between different functions and should always be used with caution.
     *
     * <p>
     * Tells this <code>Action</code>'s containing function to jump to the line
     * specified by the raw label name in the first argument.
     * </p>
     */
    void GOTO() {
        parentFun.GOTO(m.labels.get(splits[1]));
    }

    /**
     * Defines a label using the name of the first argument on its line.
     *
     * <p>
     * Registers a label/line number pair in <code>m</code>'s label registry,
     * then deactivates. Labels only need to be registered once, so there is no
     * need to keep this instruction around.
     * </p>
     */
    void LABEL() {
        m.labels.set(splits[1], m.labeltemp);
        deactivate();
    }

    /**
     * Jumps label [second argument] if the first argument evaluates to true.
     *
     * <p>
     * Evaluates splits[1] as a boolean, then makes a new <code>Action</code>
     * with instruction GOTO for the label in the second argument, then executes
     * it while pointing it to this actions parent funtion.
     * </p>
     */
    void BRANCH() {
        if (beval(splits[1])) {
            new Action("GOTO(" + splits[2] + ")").execute(parentFun);
        }
    }

    /**
     * Sets a variable named first arg with the value of the second argument. It
     * automatically detects the type and will create it within the current
     * "scope" (for example, in the correct level of recursion in a recursive
     * function). This is also used to reassign existing variables.
     *
     * <p>
     * It first detects the type of splits[2], then sets the variable by the raw
     * name of splits[1] at the "top" level of variables to the evaluated value
     * of splits[2].
     * </p>
     */
    void VARIABLE() {
        if (isString(splits[2])) {
            setSVar(m.strings.size() - 1, splits[1], streval(splits[2]));
        } else if (isBoolean(splits[2])) {
            setBVar(m.booleans.size() - 1, splits[1], beval(splits[2]));
        } else {
            setFVar(m.floats.size() - 1, splits[1], feval(splits[2]));
        }
    }

    /**
     * Exits out of the script.
     *
     * <p>
     * Simply calls the <code>end</code> function to break out of execution of
     * the user script, run unit tests, and close.
     * </p>
     */
    void END() {
        TeaScript.end();
    }

    /**
     * Does noting and is the same as an empty line.
     *
     * <p>
     * When an Action is deactivated, its type is set to NONE.
     * </p>
     */
    void NONE() {

    }

    /**
     * Adds another "layer" or "level" of variables. This usually should not be
     * called in the script because it is mostly an internal function. However,
     * it can be called in scripts if you really want to, but don't forget a
     * matching <code>DOWNSCOPE</code> because then there will be problems.
     *
     * <p>
     * Adds a dictionary/hashmap to each arraylist of collections of
     * variables/arrays respectively.
     * </p>
     */
    void UPSCOPE() {
        m.floats.add(new FloatDict());
        m.strings.add(new StringDict());
        m.booleans.add(new IntDict());
        m.farrs.add(new HashMap<>());
        m.sarrs.add(new HashMap<>());
        m.barrs.add(new HashMap<>());
    }

    /**
     * Removes top "layer" or "level" of variables. This usually should not be
     * called in the script because it is mostly an internal function. However,
     * it can be called in scripts if you really want to, but don't forget a
     * matching <code>UPSCOPE</code> before because then there will be problems.
     *
     * <p>
     * Based off the size of each arraylist of dictionaries/hashmaps, for
     * variables/arrays respectively, it removes the top one.
     * </p>
     */
    void DOWNSCOPE() {
        m.floats.remove(m.floats.size() - 1);
        m.strings.remove(m.strings.size() - 1);
        m.booleans.remove(m.booleans.size() - 1);
        m.farrs.remove(m.farrs.size() - 1);
        m.sarrs.remove(m.sarrs.size() - 1);
        m.barrs.remove(m.barrs.size() - 1);
    }

    /**
     * Executes a function defined by the user while ignoring the return value.
     *
     * <p>
     * Gets a Function from the list of Functions in m by stripping the args
     * from splits[1] and matching by name, then duplicates it (so that
     * recursive calls don't trip each other up), then executes it with the
     * splits[1] containing the functions name and arguments, much like how an
     * Action is called.
     * </p>
     */
    void USERFUN() {
        m.functions.get(removeArgs(splits[1])).dup().execute(splits[1]);
    }

    /**
     * Flag for starting to define function foo as in <code>FDEF(foo())</code>.
     * Do not specify arguments because those are passed into the function as
     * a1, a2, a3... They can be of any type. An FDEF flag should have a
     * matching EFDEF (end function definition) flag.
     *
     * <p>
     * First, an arraylist of actions is created. Then, the list of actions in
     * <code>m</code> is iterated through until the EFDEF flag is found. Each
     * Action is added to the arraylist, and if an Action is of Type RET, the
     * type of the expression is noted and used to add the function to the
     * correct list or lists of functions. Note that having multiple return
     * types from a single function is kind of broken right now.
     * </p>
     */
    void FDEF() {
        ArrayList<Action> actions = new ArrayList<>();
        boolean isBoolean = false;
        boolean isString = false;
        boolean isFloat = false;
        for (int i = m.labeltemp + 1; m.actions[i].type != Type.EFDEF; i++) {
            actions.add(m.actions[i]);
            if (m.actions[i].type == Type.RET) {
                if (isString(m.actions[i].splits[1])) {
                    isString = true;
                } else if (isBoolean(m.actions[i].splits[1])) {
                    isBoolean = true;
                } else {
                    isFloat = true;
                }
            }
        }
        if (isFloat) {
            m.functions.put(splits[1], new Function(
                    actions.toArray(new Action[actions.size()]), m.labeltemp));
        }
        if (isString) {
            m.sfunctions.put(splits[1], new Function(
                    actions.toArray(new Action[actions.size()]), m.labeltemp));
        }
        if (isBoolean) {
            m.bfunctions.put(splits[1], new Function(
                    actions.toArray(new Action[actions.size()]), m.labeltemp));
        }
        deactivate();
    }

    /**
     * End function definition flag: closes the current FDEF block. Does not
     * have any arguments.
     *
     * <p>
     * Simply a flag for the FDEF function. Doesn't have any code.
     * </p>
     */
    void EFDEF() {

    }

    /**
     * Sets float var one scope up name first argument, value evaluated second.
     * This is mostly left over from earlier development.
     *
     * <p>
     * Gets the second from top FloatDict and sets the variable with name raw
     * splits[1] and the float evaluated value of splits[2].
     * </p>
     */
    void VARSET() {
        m.floats.get(m.floats.size() - 2).set(splits[1], feval(splits[2]));
    }

    /**
     * Undefines a float by the name of the first argument in the top scope.
     * This can be useful if there is a needed variable further down of the same
     * name as a variable in the current top scope. Then, the top level variable
     * can be removed/undefined to grant access.
     *
     * <p>
     * Gets the top FloatDict and removes the variable by the name of the raw
     * value of splits[1].
     * </p>
     */
    void REMVAR() {
        m.floats.get(m.floats.size() - 1).remove(splits[1]);
    }

    /**
     * Breakpoint for debugging in a Java debugger. This is more for debugging
     * TeaScript rather than a script. For scripts, PRINT should be used.
     *
     * <p>
     * Do nothing method which can then have a breakpoint assigned to it in a
     * debugger for easy breakpoint adding.
     * </p>
     */
    void BRKPT() {
        print("");
    }

    /**
     * Returns the evaluated value of the first argument. This must be called
     * within a function to exit it even if the return value is not meaningful.
     *
     * <p>
     * Tells the parent function to return with the value of the evaluated value
     * of splits[1].</p>
     */
    void RET() {
        parentFun.RET(streval(splits[1]));
    }

    /**
     * Sets a global float variable which is not removed until the script exits.
     * The variable by the name of the first argument gets set to the float
     * evaluated value of the second argument.
     */
    void GVAR() {
        m.floats.get(0).set(splits[1], feval(splits[2]));
    }

    /**
     * Runs unit test checking if first argument matches evaluated second. Note
     * that raw numbers may be specified as integers in the first argument, but
     * if any string operations occur (such as adding spaces or something to
     * format the number), the number will be treated as a float. For example,
     * <code>1 + " " + 2</code> will give the string "1.0 2.0". If the test
     * passes, the line number and "pass" will be printed. If it fails, the line
     * number, the expected value, and the resulting value are printed.
     *
     * <p>
     * Uses the debug line (index of the action) + 1 (text editors index at 1),
     * the raw value of splits[1], and the evaluated value of splits[2] to check
     * equality. Then, deactivates action because unit tests only need to run
     * once to prevent console logging spam.
     * </p>
     */
    void U() {
        prettyUnitPass(str(m.debugline + 1), splits[1], streval(splits[2]));
        deactivate();
    }

    /**
     * A rather standard if statement. If the boolean evaluated result of the
     * first argument is true, execution continues. If it is false, function
     * execution skips to the next ELIF, ELSE, or ENDIF statement.
     *
     * <p>
     * If the boolean evaluated value of splits[1] is false, skips to the next
     * ELIF, ELSE, or ENDIF statement. Also stores the resulting boolean in the
     * parent function's cache of if results according to the line the if
     * statement is on in function space.
     * </p>
     */
    void IF() {
        if (!(parentFun.ifresults[parentFun.line] = beval(splits[1]))) {
            skiptoendofif();
        }
    }

    /**
     * Rest of statements in if block executed iff all previous ifs false.
     *
     * <p>
     * Runs through cache and checks for any true statements, if any are, skips
     * to the end of the block.
     * </p>
     */
    void ELSE() {
        if (anytrue()) {
            skiptoendofif();
        }
    }

    /**
     * Same thing as an ELSE with an IF after it. The first argument is what
     * goes into the IF statement.
     *
     * <p>
     * Works like an ELSE statement with an IF statement after it.
     * </p>
     */
    void ELIF() {
        if (anytrue()) {
            skiptoendofif();
        } else {
            IF();
        }
    }

    /**
     * End IF block: ends the currently open IF block. Does not require any
     * arguments.
     * 
     * <p>
     * Just a flag for other instructions. Doesn't have code.
     * </p>
     */
    void ENDIF() {

    }

    void DO() {

    }

    void DOWHILE() {
        if (beval(splits[1])) {
            int dos = -1;
            while (dos < 0) {
                Type t = parentFun.actions[--parentFun.line].type;
                if (t == Type.DO) {
                    dos++;
                }
                if (t == Type.DOWHILE) {
                    dos--;
                }
            }
        }
    }

    void WHILE() {
        if (!beval(splits[1])) {
            int whiles = 1;
            while (whiles > 0) {
                Type t = parentFun.actions[++parentFun.line].type;
                if (t == Type.WHILE) {
                    whiles++;
                }
                if (t == Type.ENDWHILE) {
                    whiles--;
                }
            }
        }
    }

    void ENDWHILE() {
        int whiles = -1;
        while (whiles < 0) {
            Type t = parentFun.actions[--parentFun.line].type;
            if (t == Type.WHILE) {
                whiles++;
            }
            if (t == Type.ENDWHILE) {
                whiles--;
            }
        }
        parentFun.line--;
    }

    void FOR() {
        new Action(splits[1]).execute(parentFun);
        jumpfor(splits[2]);
    }

    void ENDFOR() {
        int fors = -1;
        while (fors < 0) {
            Type t = parentFun.actions[--parentFun.line].type;
            if (t == Type.FOR) {
                fors++;
            }
            if (t == Type.ENDFOR) {
                fors--;
            }
        }
        new Action(parentFun.actions[parentFun.line].splits[3])
                .execute(parentFun);
        jumpfor(parentFun.actions[parentFun.line].splits[2]);
    }

    void ARR() {
        switch (smartTrim(splits[2])) {
            case "float":
                m.farrs.get(m.farrs.size() - 1).put(splits[1], new ArrayList<>());
                return;
            case "string":
                m.sarrs.get(m.sarrs.size() - 1).put(splits[1], new ArrayList<>());
                return;
            case "boolean":
                m.barrs.get(m.barrs.size() - 1).put(splits[1], new ArrayList<>());
            default:
        }
    }

    void ASET() {
        if (isFArr(splits[1])) {
            ArrayList<Float> fs = getFArr(splits[1]);
            while (fs.size() <= feval(splits[2])) {
                fs.add(0f);
            }
            fs.set(sint(feval(splits[2])), feval(splits[3]));
        }
        if (isSArr(splits[1])) {
            ArrayList<String> fs = getSArr(splits[1]);
            while (fs.size() <= feval(splits[2])) {
                fs.add("");
            }
            fs.set(sint(feval(splits[2])), streval(splits[3]));
        }
        if (isBArr(splits[1])) {
            ArrayList<Boolean> fs = getBArr(splits[1]);
            while (fs.size() <= feval(splits[2])) {
                fs.add(false);
            }
            fs.set(sint(feval(splits[2])), beval(splits[3]));
        }
    }

    void jumpfor(String s) {
        if (!beval(s)) {
            int fors = 1;
            while (fors > 0) {
                Type t = parentFun.actions[++parentFun.line].type;
                if (t == Type.FOR) {
                    fors++;
                }
                if (t == Type.ENDFOR) {
                    fors--;
                }
            }
        }
    }

    boolean anytrue() {
        int ifs = -1;
        int tline = parentFun.line;
        while (ifs < 0) {
            Type t = parentFun.actions[--tline].type;
            if ((t == Type.IF || t == Type.ELIF) && ifs == -1
                    && parentFun.ifresults[tline]) {
                return true;
            }
            if (t == Type.IF) {
                ifs++;
            }
            if (t == Type.ENDIF) {
                ifs--;
            }
        }
        return false;
    }

    void skiptoendofif() {
        int ifs = 1;
        while (ifs > 0) {
            Type t = parentFun.actions[++parentFun.line].type;
            if (t == Type.IF) {
                ifs++;
            }
            if (t == Type.ENDIF || (ifs == 1 && (t == Type.ELSE
                    || t == Type.ELIF))) {
                ifs--;
            }
        }
        parentFun.line--;
    }

    void deactivate() {
        type = Type.NONE;
    }

    final void s(Type t, int args) {
        type = t;
        if (splits.length - 1 < args) {
            error("ARGCOUNT", "expected " + args + " arguments, got "
                    + (splits.length - 1) + ".");
        }
    }

    void execute(Function f) {
        parentFun = f;
        switch (type) {
            case PRINT:
                PRINT();
                break;
            case GOTO:
                GOTO();
                break;
            case LABEL:
                LABEL();
                break;
            case BRANCH:
                BRANCH();
                break;
            case VARIABLE:
                VARIABLE();
                break;
            case END:
                END();
                break;
            case NONE:
                NONE();
                break;
            case UPSCOPE:
                UPSCOPE();
                break;
            case DOWNSCOPE:
                DOWNSCOPE();
                break;
            case USERFUN:
                USERFUN();
                break;
            case FDEF:
                FDEF();
                break;
            case EFDEF:
                EFDEF();
                break;
            case VARSET:
                VARSET();
                break;
            case REMVAR:
                REMVAR();
                break;
            case BRKPT:
                BRKPT();
                break;
            case RET:
                RET();
                break;
            case GVAR:
                GVAR();
                break;
            case U:
                U();
                break;
            case IF:
                IF();
                break;
            case ENDIF:
                ENDIF();
                break;
            case ELSE:
                ELSE();
                break;
            case ELIF:
                ELIF();
                break;
            case DO:
                DO();
                break;
            case DOWHILE:
                DOWHILE();
                break;
            case WHILE:
                WHILE();
                break;
            case ENDWHILE:
                ENDWHILE();
                break;
            case FOR:
                FOR();
                break;
            case ENDFOR:
                ENDFOR();
                break;
            case ARR:
                ARR();
                break;
            case ASET:
                ASET();
                break;
        }
    }

    Action(String args) {
        creationString = trim(args);
        splits = isplit(creationString);
        switch (splits[0]) {
            case "PRINT":
                s(Type.PRINT, 1);
                break;
            case "GOTO":
                s(Type.GOTO, 1);
                break;
            case "LABEL":
                s(Type.LABEL, 1);
                break;
            case "BRANCH":
                s(Type.BRANCH, 2);
                break;
            case "VARIABLE":
                s(Type.VARIABLE, 2);
                break;
            case "END":
                s(Type.END, 0);
                break;
            case "UPSCOPE":
                s(Type.UPSCOPE, 0);
                break;
            case "DOWNSCOPE":
                s(Type.DOWNSCOPE, 0);
                break;
            case "FDEF":
                s(Type.FDEF, 1);
                break;
            case "EFDEF":
                s(Type.EFDEF, 0);
                break;
            case "USERFUN":
                s(Type.USERFUN, 1);
                break;
            case "VARSET":
                s(Type.VARSET, 2);
                break;
            case "REMVAR":
                s(Type.REMVAR, 1);
                break;
            case "BRKPT":
                s(Type.BRKPT, 0);
                break;
            case "RET":
                s(Type.RET, 1);
                break;
            case "GVAR":
                s(Type.GVAR, 2);
                break;
            case "U":
                s(Type.U, 2);
                break;
            case "IF":
                s(Type.IF, 1);
                break;
            case "ENDIF":
                s(Type.ENDIF, 0);
                break;
            case "ELSE":
                s(Type.ELSE, 0);
                break;
            case "ELIF":
                s(Type.ELIF, 1);
                break;
            case "DO":
                s(Type.DO, 0);
                break;
            case "DOWHILE":
                s(Type.DOWHILE, 1);
                break;
            case "WHILE":
                s(Type.WHILE, 1);
                break;
            case "ENDWHILE":
                s(Type.ENDWHILE, 0);
                break;
            case "FOR":
                s(Type.FOR, 3);
                break;
            case "ENDFOR":
                s(Type.ENDFOR, 0);
                break;
            case "ARR":
                s(Type.ARR, 2);
                break;
            case "ASET":
                s(Type.ASET, 3);
                break;
            default:
                if (splits[0].length() == 0) {
                    type = Type.NONE;
                } else {
                    error("NOCOMMAND", "command " + splits[0] + " not found.");
                }
        }
    }

    enum Type {
        PRINT, GOTO, LABEL, BRANCH, VARIABLE, END, NONE, UPSCOPE, DOWNSCOPE,
        USERFUN, FDEF, EFDEF, VARSET, REMVAR, BRKPT, RET, GVAR, U, IF, ENDIF,
        ELSE, ELIF, DO, DOWHILE, WHILE, ENDWHILE, FOR, ENDFOR, ARR, ASET
    }
}
