package teascript;

import static java.lang.Integer.max;
import java.util.ArrayList;
import static processing.core.PApplet.pow;
import static processing.core.PApplet.trim;
import static teascript.IFunctions.doMath;
import static teascript.IFunctions.isMath;
import static teascript.TeaScript.m;
import static teascript.Utils.*;
import java.lang.StringBuilder;
import static processing.core.PApplet.str;
import static teascript.IFunctions.doBoolf;
import static teascript.IFunctions.doStrf;
import static teascript.IFunctions.isBoolf;
import static teascript.IFunctions.isStrf;

/**
 *
 * @author alnis
 */
public class Evaluator {

    static boolean beval(String expb) {
        String exp = smartTrim(expb);
        if (blookupable(exp)) {
            return blookup(exp);
        }

        String tstr = fstring(exp);
        int or = tstr.lastIndexOf("||");
        int and = tstr.lastIndexOf("&&");
        int gtr = noconpos(tstr, ">", ">=", ">=");
        int les = noconpos(tstr, "<", "<=", "<=");
        int gte = tstr.lastIndexOf(">=");
        int lse = tstr.lastIndexOf("<=");
        int neq = tstr.lastIndexOf("!=");
        int equ = tstr.lastIndexOf("==");
        int xor = tstr.lastIndexOf("^");
        int not = tstr.lastIndexOf("!");

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

    static float feval(String exp) {
        exp = trim(trimPar(trim(exp)));
        if (flookupable(exp)) {
            return flookup(exp);
        }

        String tstr = fstring(exp);
        int add = tstr.lastIndexOf("+");
        int sub = notNegative(tstr);
        int mul = noconpos(tstr, "*", "\\*\\*", "**");
        int div = tstr.lastIndexOf("/");
        int rem = tstr.lastIndexOf("%%");
        int mod = noconpos(tstr, "%", "%%", "%%");
        int pow = tstr.lastIndexOf("**");

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
            return mod(feval(exp.substring(0, mod)), feval(exp.substring(mod + 1)));
        }
        if (pow != -1) {
            return pow(feval(exp.substring(0, pow)), feval(exp.substring(pow + 2)));
        }

        if (tstr.contains("-")) {
            return -feval(exp.substring(tstr.indexOf("-") + 1));
        }

        float f = 0;
        try {
            f = Float.parseFloat(exp);
        } catch (Exception e) {
            error("FLOATPARSE", "problem parsing " + exp);
        }
        return f;
    }

    static float mod(float a, float b) {
        return (a % b + b) % b;
    }

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
            if (hasVar(exp, i)) {
                return getVar(exp, i);
            }
        }
        if (isMath(exp)) {
            return doMath(exp);
        }
        return Float.parseFloat(m.functions.get(removeArgs(exp)).dup().execute(exp));
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
            b = m.booleans.get(level).get(exp) == 1;} catch (Exception e) {
            error("NOVAR", "no boolean variable " + exp + " found.");
        }
        return b;
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

    static boolean hasVar(String exp, int level) {
        return m.floats.get(level).hasKey(exp);
    }

    static float getVar(String exp) {
        return getVar(exp, m.floats.size() - 1);
    }

    static float getVar(String exp, int level) {
        float f = 0;
        try {
            f = m.floats.get(level).get(exp);
        } catch (Exception e) {
            error("NOVAR", "no variable " + exp + " found.");
        }
        return f;
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

    static boolean isRawString(String s) {
        return s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"';
    }

    static boolean isString(String exp) {
        String[] tmp = nsplit(exp, '+');
        for (String s : tmp) {
            if (slookupable(smartTrim(s))) {
                return true;
            }
        }
        String t = fstring(exp);
        return isRawString(exp) || t.indexOf("str(") != -1 || t.indexOf("\"") != -1 || m.sfunctions.containsKey(removeArgs(exp));
    }

    static boolean isBoolean(String exp_) {
        String expb = smartTrim(exp_);

        if (blookupable(expb)) {
            return true;
        }

        String exp = fstring(expb);

        return exp.indexOf(">") != -1 || exp.indexOf("<") != -1 || exp.indexOf("!=") != -1 || exp.indexOf("==") != -1
                || exp.indexOf("||") != -1 || exp.indexOf("&&") != -1 || exp.indexOf(">=") != -1 || exp.indexOf("<=") != -1
                || exp.indexOf("true") != -1 || exp.indexOf("false") != -1 || exp.indexOf("!") != -1;
    }

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
        return m.bfunctions.get(removeArgs(exp)).dup().execute(exp).equals("true");
    }

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

    static String streval(String expb) {
        String exp = smartTrim(expb);
        String tstr = fstring(exp);
        int plus = tstr.lastIndexOf("+");
        if (plus != -1 && (isString(exp.substring(0, plus)) || isString(exp.substring(plus + 1)))) {
            return streval(exp.substring(0, plus)) + streval(exp.substring(plus + 1));
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
}
