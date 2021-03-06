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

    /**
     * DO flag for a DO/DOWHILE loop. This opens a new DOWHILE block. The
     * instructions following it will always be executed at least once. This is
     * the point to which the corresponding DOWHILE statement will jump.
     *
     * <p>
     * Just a flag, so there is no code.
     * </p>
     */
    void DO() {

    }

    /**
     * WHILE part of a DOWHILE block: if the condition is true, jumps to the DO.
     * If the first argument evaluates to a boolean true, it will jump right
     * back up, all the way to the DO.
     *
     * <p>
     * Boolean evaluates the first argument. If true, skips back up to the DO.
     * </p>
     */
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

    /**
     * As long as the condition is true, the block will be repeatedly run. If
     * the condition is false when the statement is first reached, the block
     * will be skipped. This statement is also evaluated after each run of the
     * block. If it is still true, the block will be evaluated again.
     *
     * <p>
     * Skips to the end of the WHILE block if splits[1] evaluates to false.
     * </p>
     */
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

    /**
     * End flag for a WHILE block/loop. Doesn't take arguments, just closes the
     * block.
     *
     * <p>
     * Sends execution up to the WHILE instruction for the condition to be
     * evaluated again/for the WHILE instruction to run again.
     */
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

    /**
     * First argument init op, second continue condition, third increment op.
     * The first argument is the initializer instruction. It is a TeaScript
     * Action which looks like any other line of code in the script. For
     * example, it could be <code>VARIABLE(i, 0)</code> to create variable i and
     * initialize it to 0. If no Action is required, use <code>NONE()</code>. It
     * runs before condition evaluation, block execution, and incrementation.
     * The second argument must be an expression which evaluates to a boolean.
     * For example, it could be <code>i < 8</code>. Execution will repeat as
     * long as the condition is true. Finally, the incrementation Action is
     * another instruction like any normal line of TeaScript code which runs
     * after the block is executed but before jumping back to the condition. For
     * example, it could be <code>VARIABLE(i, i + 1)</code>. The overall order
     * is: init -> condition (skip past everything else if false) -> block of
     * code -> incrementation -> back to condition.
     *
     * <p>
     * Executes a new action made from the raw text of the first argument, then
     * jumps over the rest if the second argument evaluates to false.
     * </p>
     */
    void FOR() {
        new Action(splits[1]).execute(parentFun);
        jumpfor(splits[2]);
    }

    /**
     * Flag for the end of a FOR loop. Doesn't take any arguments.
     *
     * <p>
     * Skips the line back up to the line of the FOR statement, then creates and
     * executes an action from the text of the FOR statement's third argument,
     * then evaluates the FOR statement's condition and jumps over the block if
     * it is false.
     * </p>
     */
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

    /**
     * Creates an array of name first argument and type specified in the second.
     * For example, an array of strings called names can be created with the
     * statement <code>ARR(names, string)</code>.
     *
     * <p>
     * Switches based off the trimmed second argument for the type, then adds an
     * arraylist to the hashmap with key first argument.
     * </p>
     */
    void ARR() {
        switch (smartTrim(splits[2])) {
            case "float":
                m.farrs.get(m.farrs.size() - 1)
                        .put(splits[1], new ArrayList<>());
                return;
            case "string":
                m.sarrs.get(m.sarrs.size() - 1)
                        .put(splits[1], new ArrayList<>());
                return;
            case "boolean":
                m.barrs.get(m.barrs.size() - 1)
                        .put(splits[1], new ArrayList<>());
            default:
        }
    }

    /**
     * Sets item in array first arg, item second arg, and value eval third arg.
     * Automatically detects type and adds any items needed to get up to that
     * index. This is both used to change existing elements and to add new ones.
     * For example, to add 3 elements to an array of floats called fooarr:
     * <code>ASET(fooarr, 2, 0)</code> The default value for added elements is
     * 0, "", and false, for floats, strings, and booleans respectively. The
     * third argument is only used as the value for the element at the specified
     * index. The other values are set to their respective default values. The
     * index value may be any float value. It will be rounded to the nearest
     * integer when used to lookup an index.
     *
     * <p>
     * First, the second argument is float evaluated and rounded to give the
     * target index of the item. The, the type of the array is detected and
     * default values are added up to and including the target index if needed.
     * Finally, the target index is set to the evaluated value of the third
     * argument.
     * </p>
     */
    void ASET() {
        int itmp = sint(feval(splits[2]));
        if (isFArr(splits[1])) {
            ArrayList<Float> fs = getFArr(splits[1]);
            while (fs.size() <= itmp) {
                fs.add(0f);
            }
            fs.set(itmp, feval(splits[3]));
        }
        if (isSArr(splits[1])) {
            ArrayList<String> fs = getSArr(splits[1]);
            while (fs.size() <= itmp) {
                fs.add("");
            }
            fs.set(itmp, streval(splits[3]));
        }
        if (isBArr(splits[1])) {
            ArrayList<Boolean> fs = getBArr(splits[1]);
            while (fs.size() <= itmp) {
                fs.add(false);
            }
            fs.set(itmp, beval(splits[3]));
        }
    }

    void SYSFUN() {
        for (Tint t : TeaScript.tints) {
            if (t.hasVoidByName(fname(splits[1]))) {
                t.voidByName(isplit(splits[1]));
                return;
            }
        }
        error("NOCOMMAND", "no void sysfun called " +
                fname(splits[1]) + " found");
    }

    /**
     * Boolean evaluates string, and if false, skips over rest of FOR block. The
     * skip handles nested FOR statements by adding and subtracting from a
     * counter based off the type of Action the current line is. It moves the
     * line number within the parent function as it progresses.
     *
     * @param s string to evaluate to boolean for whether block should execute
     */
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

    /**
     * Checks whether ELSE/ELIF statement should be executed/checked. It
     * iterates through the cache of if results in the parent function while
     * keeping track of how deep in a block it is using the ifs variable. If the
     * depth is -1, or one block in, it will check if the current line is an IF
     * or ELIF statement, and if it is, it will return true if the cached result
     * for it is true. Otherwise, it will continue. It returns false if no level
     * -1 IF/ELIF statements were true.
     *
     * @return whether current IF block had true IF/ELIF statement result
     */
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

    /**
     * Skips to the next potential end of the current IF block. This includes
     * ENDIF, ELSE, and ELIF statements. It decrements the line once in order to
     * ensure the execution of the found statement on the next go-around.
     */
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

    /**
     * Sets this Action's Type to NONE, thus deactivating it from executing.
     * This does still preserve the arguments passed to the function along with
     * the creation string and parent function.
     */
    void deactivate() {
        type = Type.NONE;
    }

    /**
     * Sets up this Action's Type as t and checks for at least args arguments.
     * Assumes that splits has already been correctly created. Will give a
     * script error if there are not enough arguments.
     *
     * @param t Type of this Action
     * @param args number of arguments required at a minimum
     */
    final void s(Type t, int args) {
        type = t;
        if (splits.length - 1 < args) {
            error("ARGCOUNT", "expected " + args + " arguments, got "
                    + (splits.length - 1) + ".");
        }
    }

    /**
     * Runs this Action with f indicating the Function calling this Action.
     *
     * @param f function which is calling this Action
     */
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
            case SYSFUN:
                SYSFUN();
        }
    }

    /**
     * Constructs a new Action from the String representing FUNCTION(arg1, ...).
     * Trims and splits the String into the function name and arguments into the
     * array splits, then calls the setup/check function with the type and
     * required number of arguments.
     *
     * @param args String representing the function with arguments
     */
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
            case "SYSFUN":
                s(Type.SYSFUN, 1);
                break;
            default:
                if (splits[0].length() == 0) {
                    type = Type.NONE;
                } else {
                    error("NOCOMMAND", "command " + splits[0] + " not found.");
                }
        }
    }

    /**
     * The possible types of Actions.
     */
    enum Type {
        PRINT, GOTO, LABEL, BRANCH, VARIABLE, END, NONE, UPSCOPE, DOWNSCOPE,
        USERFUN, FDEF, EFDEF, VARSET, REMVAR, BRKPT, RET, GVAR, U, IF, ENDIF,
        ELSE, ELIF, DO, DOWHILE, WHILE, ENDWHILE, FOR, ENDFOR, ARR, ASET, SYSFUN
    }
}
