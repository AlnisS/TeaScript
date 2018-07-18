/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package teascript;

import static java.lang.Float.max;
import static java.lang.Float.min;
import static processing.core.PApplet.abs;
import static processing.core.PApplet.acos;
import static processing.core.PApplet.asin;
import static processing.core.PApplet.atan;
import static processing.core.PApplet.atan2;
import static processing.core.PApplet.ceil;
import static processing.core.PApplet.cos;
import static processing.core.PApplet.degrees;
import static processing.core.PApplet.exp;
import static processing.core.PApplet.floor;
import static processing.core.PApplet.round;
import static processing.core.PApplet.trim;
import static teascript.Evaluator.*;
import static teascript.Utils.*;
import static processing.core.PApplet.log;
import static processing.core.PApplet.pow;
import static processing.core.PApplet.radians;
import static processing.core.PApplet.sin;
import static processing.core.PApplet.sq;
import static processing.core.PApplet.sqrt;
import static processing.core.PApplet.tan;

/**
 *
 * @author chrx
 */
public class IFunctions {

    static String[] boolfs = {"isEmpty", "equals", "equalsic", "startsWith", "endsWith", "bar"};

    static boolean isBoolf(String exp) {
        String targ = "";
        if (exp.indexOf("(") != -1) {
            targ = exp.substring(0, exp.indexOf("("));
        }
        for (String s : boolfs) {
            if (s.equals(targ)) {
                return true;
            }
        }
        return false;
    }

    static boolean doBoolf(String exp) {
        String[] sp = isplit(exp);
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

    static String[] strfs = {"substring", "lowercase", "uppercase", "trim", "trimPar", "smartTrim", "sar"};

    static boolean isStrf(String exp) {
        String targ = "";
        if (exp.indexOf("(") != -1) {
            targ = exp.substring(0, exp.indexOf("("));
        }
        for (String s : strfs) {
            if (s.equals(targ)) {
                return true;
            }
        }
        return false;
    }

    static String doStrf(String exp) {
        String[] sp = isplit(exp);
        switch (sp[0]) {
            case "substring":
                if (sp.length == 3) {
                    return streval(sp[1]).substring(sint(streval(sp[2])));
                } else {
                    return streval(sp[1]).substring(sint(streval(sp[2])), sint(streval(sp[3])));
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

    static String[] maths = {"abs", "ceil", "floor", "floordiv", "min", "max", "round", "random",
        "exp", "log", "log10", "pow", "sqrt", "sin", "cos", "tan", "asin", "acos",
        "atan", "atan2", "sinh", "cosh", "tanh", "todeg", "torad", "sq",
        "length", "indexOf", "lindexOf", "compTo", "compToic", "far"};

    static boolean isMath(String exp) {
        String targ = "";
        if (exp.indexOf("(") != -1) {
            targ = exp.substring(0, exp.indexOf("("));
        }
        for (String s : maths) {
            if (s.equals(targ)) {
                return true;
            }
        }
        return false;
    }

    static float doMath(String exp) {
        String[] sp = isplit(exp);
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
                return random(feval(sp[1]), feval(sp[2]));
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
                    return streval(sp[1]).indexOf(streval(sp[2]), sint(streval(sp[3])));
                }
            case "lindexOf":
                if (sp.length == 3) {
                    return streval(sp[1]).lastIndexOf(streval(sp[2]));
                } else {
                    return streval(sp[1]).lastIndexOf(streval(sp[2]), sint(streval(sp[3])));
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

    private static float random(float feval, float feval0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
