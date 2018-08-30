package teascript;

import java.util.ArrayList;
import static processing.core.PApplet.*;
import static teascript.TeaScript.m;
import static teascript.Utils.*;
import static teascript.FMan.feval;
import static teascript.BMan.isBoolean;
import static teascript.BMan.beval;

/**
 * String manager: Evaluates expressions and manages variables. Uses
 * <code>Machine m</code> as a place to store variables (both single variables
 * and arrays). Also uses user defined and built in string functions such as
 * <code>substring</code> and contains various utility functions.
 *
 * @author alnis
 */
public class SMan {

    /**
     * Evaluates a string expression to its string result.
     *
     * @param exp Expression to evaluate.
     * @return Resulting string value.
     */
    static String streval(String exp) {
        exp = smartTrim(exp);
        String tmpstring = fstring(exp);
        int plus = tmpstring.lastIndexOf("+");
        if (plus != -1 && (isString(exp.substring(0, plus))
                || isString(exp.substring(plus + 1)))) {
            return streval(exp.substring(0, plus))
                    + streval(exp.substring(plus + 1));
        }

        if (slookupable(exp)) {
            return slookup(exp);
        }
        if (isRawString(exp)) {
            return rawString(exp, true);
        }
        if (isString(exp)) {
            return streval(exp);
        }
        if (isBoolean(exp)) {
            return str(beval(exp));
        }
        return str(feval(exp));
    }

    /**
     * Checks variable and function lists for <code>exp</code>. This includes
     * simple string variables, functions returning strings, default string
     * functions, and array lookups.
     *
     * @param exp Expression potentially representing lookup-able string value.
     * @return Whether or not <code>exp</code> can be string looked up.
     */
    static boolean slookupable(String exp) {
        for (int i = m.strings.size() - 1; i >= 0; i--) {
            if (m.strings.get(i).hasKey(exp)) {
                return true;
            }
        }
        if (m.sfunctions.containsKey(removeArgs(exp))) {
            return true;
        }
        return isStrf(exp);
    }

    static String slookup(String exp) {
        for (int i = m.strings.size() - 1; i >= 0; i--) {
            if (hasSVar(exp, i)) {
                return getSVar(exp, i);
            }
        }
        if (isStrf(exp)) {
            return doStrf(exp);
        }
        return m.sfunctions.get(removeArgs(exp)).dup().execute(exp);
    }

    static boolean hasSVar(String exp, int level) {
        return m.strings.get(level).hasKey(exp);
    }

    static String getSVar(String exp) {
        return getSVar(exp, m.strings.size() - 1);
    }

    static String getSVar(String exp, int level) {
        String s = null;
        try {
            s = m.strings.get(level).get(exp);
        } catch (Exception e) {
            error("NOVAR", "no string variable " + exp + " found.");
        }
        return s;
    }

    static void setSVar(int level, String name, String value) {
        m.strings.get(level).set(name, value);
    }

    static boolean isSArr(String exp) {
        return m.sarrs.get(m.sarrs.size() - 1).containsKey(exp);
    }

    static ArrayList<String> getSArr(String exp) {
        return getSArr(exp, m.sarrs.size() - 1);
    }

    static ArrayList<String> getSArr(String exp, int level) {
        ArrayList<String> f = null;
        try {
            f = m.sarrs.get(level).get(exp);
        } catch (Exception e) {
            error("NOVAR", "no string array " + exp + " found.");
        }
        return f;
    }

    static boolean isString(String exp) {
        String[] tmp = nsplit(exp, '+');
        for (String s : tmp) {
            if (slookupable(smartTrim(s))) {
                return true;
            }
        }
        String t = fstring(exp);
        return isRawString(exp) || t.contains("str(") || t.contains("\"")
                || m.sfunctions.containsKey(removeArgs(exp));
    }

    static boolean isRawString(String s) {
        return s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"';
    }
    static String[] specialCharsText = {"\\\\", "\\t", "\\n", "\\'"};
    static String[] specialChars = {"\\", "\t", "\n", "\""};

    static String rawString(String exp, boolean remq) {
        int bt = remq ? 1 : 0;
        StringBuilder str = new StringBuilder(
                exp.substring(bt, exp.length() - bt));
        for (int i = 0; i < str.length() - 1; i++) {
            String s = str.substring(i, i + 2);
            int f = -1;
            for (int j = 0; j < specialCharsText.length; j++) {
                if (s.equals(specialCharsText[j])) {
                    f = j;
                }
            }
            if (f != -1) {
                str.delete(i, i + 2);
                str.insert(i, specialChars[f]);
            }
        }
        return str.toString();
    }

    static String[] strfs = {"substring", "lowercase", "uppercase", "trim",
        "trimPar", "smartTrim", "sar"};

    static boolean isStrf(String exp) {
        String targ = iRemoveArgs(exp);
        for (String s : strfs) {
            if (s.equals(targ)) {
                return true;
            }
        }
        for (Tint t : TeaScript.tints) {
            if (t.hasStringByName(targ)) {
                return true;
            }
        }
        return false;
    }

    static String doStrf(String exp) {
        String[] sp = isplit(exp);
        for (Tint t : TeaScript.tints) {
            if (t.hasStringByName(sp[0])) {
                return t.stringByName(sp);
            }
        }
        switch (sp[0]) {
            case "substring":
                if (sp.length == 3) {
                    return streval(sp[1]).substring(sint(streval(sp[2])));
                } else {
                    return streval(sp[1]).substring(sint(streval(sp[2])),
                            sint(streval(sp[3])));
                }
            case "lowercase":
                return streval(sp[1]).toLowerCase();
            case "uppercase":
                return streval(sp[1]).toUpperCase();
            case "trim":
                return trim(streval(sp[1]));
            case "trimPar":
                return trimPar(streval(sp[1]));
            case "smartTrim":
                return smartTrim(streval(sp[1]));
            case "sar":
                return getSArr(sp[1]).get(sint(feval(sp[2])));
            default:
                error("NOCOMMAND", "no string command: " + exp);
        }
        return null;
    }
}
