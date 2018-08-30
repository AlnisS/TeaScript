package teascript;

import java.util.ArrayList;
import static processing.core.PApplet.*;
import static teascript.BMan.*;
import static teascript.SMan.*;
import static teascript.TeaScript.m;
import static teascript.TeaScript.tints;
import static teascript.Utils.*;

/**
 * Float manager: Evaluates expressions and manages variables. Uses
 * <code>Machine m</code> as a place to store variables (both single variables
 * and arrays). Also uses user defined and built in float functions such as
 * <code>atan2</code> and contains various utility functions.
 *
 * @author alnis
 */
public class FMan {

    /**
     * Evaluates a string expression to its float result.
     *
     * @param exp Expression to evaluate.
     * @return Resulting float value.
     */
    static float feval(String exp) {
        exp = trim(trimPar(trim(exp)));
        if (flookupable(exp)) {
            return flookup(exp);
        }

        String tmpstring = fstring(exp);
        int add = tmpstring.lastIndexOf("+");
        int sub = notNegative(tmpstring);
        int mul = noconpos(tmpstring, "*", "\\*\\*", "**");
        int div = tmpstring.lastIndexOf("/");
        int rem = tmpstring.lastIndexOf("%%");
        int mod = noconpos(tmpstring, "%", "%%", "%%");
        int pow = tmpstring.lastIndexOf("**");

        int maxmul = max(max(mul, div), max(rem, mod));
        if (mul != maxmul) {
            mul = -1;
        }
        if (div != maxmul) {
            div = -1;
        }
        if (rem != maxmul) {
            rem = -1;
        }
        if (mod != maxmul) {
            mod = -1;
        }

        if (add != -1 && add > sub) {
            return feval(exp.substring(0, add)) + feval(exp.substring(add + 1));
        }
        if (sub != -1 && sub > add) {
            return feval(exp.substring(0, sub)) - feval(exp.substring(sub + 1));
        }
        if (mul != -1) {
            return feval(exp.substring(0, mul)) * feval(exp.substring(mul + 1));
        }
        if (div != -1) {
            return feval(exp.substring(0, div)) / feval(exp.substring(div + 1));
        }
        if (rem != -1) {
            return feval(exp.substring(0, rem)) % feval(exp.substring(rem + 2));
        }
        if (mod != -1) {
            return mod(feval(exp.substring(0, mod)),
                    feval(exp.substring(mod + 1)));
        }
        if (pow != -1) {
            return pow(feval(exp.substring(0, pow)),
                    feval(exp.substring(pow + 2)));
        }

        if (tmpstring.contains("-")) {
            return -feval(exp.substring(tmpstring.indexOf("-") + 1));
        }

        float f = 0;
        try {
            f = Float.parseFloat(exp);
        } catch (Exception e) {
            error("FLOATPARSE", "problem parsing " + exp);
        }
        return f;
    }

    /**
     * Checks variable and function lists for <code>exp</code>. This includes
     * simple float variables, functions returning floats, default float
     * functions, and array lookups.
     *
     * @param exp Expression potentially representing lookup-able float value.
     * @return Whether or not <code>exp</code> can be float looked up.
     */
    static boolean flookupable(String exp) {
        for (int i = m.floats.size() - 1; i >= 0; i--) {
            if (m.floats.get(i).hasKey(exp)) {
                return true;
            }
        }
        if (m.functions.containsKey(removeArgs(exp))) {
            return true;
        }
        return isMath(exp);
    }

    static float flookup(String exp) {
        for (int i = m.floats.size() - 1; i >= 0; i--) {
            if (hasFVar(exp, i)) {
                return getFVar(exp, i);
            }
        }
        if (isMath(exp)) {
            return doMath(exp);
        }
        return Float.parseFloat(m.functions.get(removeArgs(exp))
                .dup().execute(exp));
    }

    static boolean hasFVar(String exp, int level) {
        return m.floats.get(level).hasKey(exp);
    }

    static float getFVar(String exp) {
        return getFVar(exp, m.floats.size() - 1);
    }

    static float getFVar(String exp, int level) {
        float f = 0;
        try {
            f = m.floats.get(level).get(exp);
        } catch (Exception e) {
            error("NOVAR", "no variable " + exp + " found.");
        }
        return f;
    }

    static void setFVar(int level, String name, float value) {
        m.floats.get(level).set(name, value);
    }

    static boolean isFArr(String exp) {
        return m.farrs.get(m.farrs.size() - 1).containsKey(exp);
    }

    static ArrayList<Float> getFArr(String exp) {
        return getFArr(exp, m.farrs.size() - 1);
    }

    static ArrayList<Float> getFArr(String exp, int level) {
        ArrayList<Float> f = null;
        try {
            f = m.farrs.get(level).get(exp);
        } catch (Exception e) {
            error("NOVAR", "no float array " + exp + " found.");
        }
        return f;
    }

    static boolean isMushyFloat(String exp_) {
        return ! (isString(exp_) && isBoolean(exp_));
    }

    static String[] maths = {"abs", "ceil", "floor", "floordiv", "min", "max",
        "round", "random", "exp", "log", "log10", "pow", "sqrt", "sin", "cos",
        "tan", "asin", "acos", "atan", "atan2", "sinh", "cosh", "tanh", "todeg",
        "torad", "sq", "length", "indexOf", "lindexOf", "compTo", "compToic",
        "far"};

    static boolean isMath(String exp) {
        String targ = removeArgs(exp);
        if(targ.length() > 2) targ = targ.substring(0, targ.length() - 2);
        for (String s : maths) {
            if (s.equals(targ)) {
                return true;
            }
        }
        for (Tint t : TeaScript.tints) {
            if (t.hasFloatByName(targ)) {
                return true;
            }
        }
        return false;
    }

    static float doMath(String exp) {
        String[] sp = isplit(exp);
        for (Tint t : TeaScript.tints) {
            if (t.hasFloatByName(sp[0])) {
                return t.floatByName(sp);
            }
        }
        switch (sp[0]) {
            case "abs":
                return abs(feval(sp[1]));
            case "ceil":
                return ceil(feval(sp[1]));
            case "floor":
                return floor(feval(sp[1]));
            case "floordiv":
                return floor(feval(sp[1]) / feval(sp[2]));
            case "min":
                return min(feval(sp[1]), feval(sp[2]));
            case "max":
                return max(feval(sp[1]), feval(sp[2]));
            case "round":
                return round(feval(sp[1]));
            case "random":
                float min = feval(sp[1]);
                float max = feval(sp[2]);
                return (float) (min + Math.random() * (max - min));
            case "exp":
                return exp(feval(sp[1]));
            case "log":
                return log(feval(sp[1]));
            case "log10":
                return log(feval(sp[1])) / log(10);
            case "pow":
                return pow(feval(sp[1]), feval(sp[2]));
            case "sqrt":
                return sqrt(feval(sp[1]));
            case "sin":
                return sin(feval(sp[1]));
            case "cos":
                return cos(feval(sp[1]));
            case "tan":
                return tan(feval(sp[1]));
            case "asin":
                return asin(feval(sp[1]));
            case "acos":
                return acos(feval(sp[1]));
            case "atan":
                return atan(feval(sp[1]));
            case "atan2":
                return atan2(feval(sp[1]), feval(sp[2]));
            case "sinh":
                return (exp(feval(sp[1])) - exp(-feval(sp[1]))) / 2;
            case "cosh":
                return (exp(feval(sp[1])) + exp(-feval(sp[1]))) / 2;
            case "tanh":
                return (exp(2 * feval(sp[1])) - 1) / (exp(2 * feval(sp[1])) + 1);
            case "todeg":
                return degrees(feval(sp[1]));
            case "torad":
                return radians(feval(sp[1]));
            case "sq":
                return sq(feval(sp[1]));
            case "length":
                if (isString(sp[1])) {
                    return streval(sp[1]).length();
                }
                if (isFArr(smartTrim(sp[1]))) {
                    return getFArr(smartTrim(sp[1])).size();
                }
                if (isSArr(smartTrim(sp[1]))) {
                    return getSArr(smartTrim(sp[1])).size();
                }
                if (isBArr(smartTrim(sp[1]))) {
                    return getBArr(smartTrim(sp[1])).size();
                }
            case "indexOf":
                if (sp.length == 3) {
                    return streval(sp[1]).indexOf(streval(sp[2]));
                } else {
                    return streval(sp[1]).indexOf(streval(sp[2]),
                            sint(streval(sp[3])));
                }
            case "lindexOf":
                if (sp.length == 3) {
                    return streval(sp[1]).lastIndexOf(streval(sp[2]));
                } else {
                    return streval(sp[1]).lastIndexOf(streval(sp[2]),
                            sint(streval(sp[3])));
                }
            case "compTo":
                return streval(sp[1]).compareTo(streval(sp[2]));
            case "compToic":
                return streval(sp[1]).compareToIgnoreCase(streval(sp[2]));
            case "far":
                return getFArr(sp[1]).get(sint(feval(sp[2])));
            default:
                error("NOCOMMAND", "no math command: " + exp);
        }
        return -1;
    }
}
