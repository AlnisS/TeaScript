package teascript;

import java.util.ArrayList;
import static processing.core.PApplet.*;
import static teascript.FMan.feval;
import static teascript.SMan.streval;
import static teascript.TeaScript.m;
import static teascript.Utils.*;

/**
 * Boolean manager: Evaluates expressions and manages variables. Uses
 * <code>Machine m</code> as a place to store variables (both single variables
 * and arrays). Also uses user defined and built in boolean functions such as
 * <code>isEmpty</code> and contains various utility functions.
 *
 * @author alnis
 */
public class BMan {

    /**
     * Evaluates a string expression to its boolean result.
     *
     * @param exp Expression to evaluate.
     * @return Resulting boolean value.
     */
    static boolean beval(String exp) {
        exp = smartTrim(exp);
        if (blookupable(exp)) {
            return blookup(exp);
        }

        String tmpstring = fstring(exp);
        int or = tmpstring.lastIndexOf("||");
        int and = tmpstring.lastIndexOf("&&");
        int gtr = noconpos(tmpstring, ">", ">=", ">=");
        int les = noconpos(tmpstring, "<", "<=", "<=");
        int gte = tmpstring.lastIndexOf(">=");
        int lse = tmpstring.lastIndexOf("<=");
        int neq = tmpstring.lastIndexOf("!=");
        int equ = tmpstring.lastIndexOf("==");
        int xor = tmpstring.lastIndexOf("^");
        int not = tmpstring.lastIndexOf("!");

        int maxmul = max(max(gtr, les), max(gte, lse));
        if (gtr != maxmul) {
            gtr = -1;
        }
        if (les != maxmul) {
            les = -1;
        }
        if (gte != maxmul) {
            gte = -1;
        }
        if (lse != maxmul) {
            lse = -1;
        }

        if (or != -1) {
            return beval(exp.substring(0, or)) || beval(exp.substring(or + 2));
        }
        if (and != -1) {
            return beval(exp.substring(0, and)) && beval(exp.substring(and + 2));
        }
        if (xor != -1) {
            return beval(exp.substring(0, xor)) ^ beval(exp.substring(xor + 1));
        }

        if (neq != -1 && neq > equ) {
            return !equiv(exp.substring(0, neq), exp.substring(neq + 2));
        }
        if (equ != -1 && equ > neq) {
            return equiv(exp.substring(0, equ), exp.substring(equ + 2));
        }

        if (gtr != -1) {
            return feval(exp.substring(0, gtr)) > feval(exp.substring(gtr + 1));
        }
        if (les != -1) {
            return feval(exp.substring(0, les)) < feval(exp.substring(les + 1));
        }
        if (gte != -1) {
            return feval(exp.substring(0, gte)) >= feval(exp.substring(gte + 2));
        }
        if (lse != -1) {
            return feval(exp.substring(0, lse)) <= feval(exp.substring(lse + 2));
        }

        if (not != -1) {
            return !beval(trim(exp.substring(not + 1)));
        }

        return exp.equals("true");
    }

    /**
     * Checks variable and function lists for <code>exp</code>. This includes
     * simple boolean variables, functions returning booleans, default boolean
     * functions, and array lookups.
     *
     * @param exp Expression potentially representing lookup-able boolean value.
     * @return Whether or not <code>exp</code> can be boolean looked up.
     */
    static boolean blookupable(String exp) {
        for (int i = m.booleans.size() - 1; i >= 0; i--) {
            if (m.booleans.get(i).hasKey(exp)) {
                return true;
            }
        }
        if (m.bfunctions.containsKey(removeArgs(exp))) {
            return true;
        }
        return isBoolf(exp);
    }

    static boolean blookup(String exp) {
        for (int i = m.booleans.size() - 1; i >= 0; i--) {
            if (hasBVar(exp, i)) {
                return getBVar(exp, i);
            }
        }
        if (isBoolf(exp)) {
            return doBoolf(exp);
        }
        return m.bfunctions.get(removeArgs(exp))
                .dup().execute(exp).equals("true");
    }

    static boolean hasBVar(String exp, int level) {
        return m.booleans.get(level).hasKey(exp);
    }

    static boolean getBVar(String exp) {
        return getBVar(exp, m.booleans.size() - 1);
    }

    static boolean getBVar(String exp, int level) {
        boolean b = false;
        try {
            b = m.booleans.get(level).get(exp) == 1;
        } catch (Exception e) {
            error("NOVAR", "no boolean variable " + exp + " found.");
        }
        return b;
    }

    static void setBVar(int level, String name, boolean value) {
        m.booleans.get(level).set(name, value ? 1 : 0);
    }

    static boolean isBArr(String exp) {
        return m.barrs.get(m.barrs.size() - 1).containsKey(exp);
    }

    static ArrayList<Boolean> getBArr(String exp) {
        return getBArr(exp, m.barrs.size() - 1);
    }

    static ArrayList<Boolean> getBArr(String exp, int level) {
        ArrayList<Boolean> f = null;
        try {
            f = m.barrs.get(level).get(exp);
        } catch (Exception e) {
            error("NOVAR", "no boolean array " + exp + " found.");
        }
        return f;
    }

    static boolean isBoolean(String exp_) {
        String expb = smartTrim(exp_);

        if (blookupable(expb)) {
            return true;
        }

        String exp = fstring(expb);

        return exp.contains(">") || exp.contains("<") || exp.contains("!=")
                || exp.contains("==") || exp.contains("||")
                || exp.contains("&&") || exp.contains(">=")
                || exp.contains("<=") || exp.contains("true")
                || exp.contains("false") || exp.contains("!");
    }

    static String[] boolfs = {"isEmpty", "equals", "equalsic", "startsWith",
        "endsWith", "bar"};

    static boolean isBoolf(String exp) {
        String targ = iRemoveArgs(exp);
        for (String s : boolfs) {
            if (s.equals(targ)) {
                return true;
            }
        }
        for (Tint t : TeaScript.tints) {
            if (t.hasBooleanByName(targ)) {
                return true;
            }
        }
        return false;
    }

    static boolean doBoolf(String exp) {
        String[] sp = isplit(exp);
        for (Tint t : TeaScript.tints) {
            if (t.hasBooleanByName(sp[0])) {
                return t.booleanByName(sp);
            }
        }
        switch (sp[0]) {
            case "isEmpty":
                return streval(sp[1]).isEmpty();
            case "equals":
                return streval(sp[1]).equals(streval(sp[2]));
            case "equalsic":
                return streval(sp[1]).equalsIgnoreCase(streval(sp[2]));
            case "startsWith":
                if (sp.length == 3) {
                    return streval(sp[1]).startsWith(streval(sp[2]));
                } else {
                    return streval(sp[1]).startsWith(streval(sp[2]), sint(streval(sp[3])));
                }
            case "endsWith":
                return streval(sp[1]).endsWith(streval(sp[2]));
            case "bar":
                return getBArr(sp[1]).get(sint(feval(sp[2])));
            default:
                error("NOCOMMAND", "no boolean command: " + exp);
        }
        return false;
    }
}
