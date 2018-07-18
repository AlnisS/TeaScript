package teascript;

/**
 *
 * @author alnis
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import static processing.core.PApplet.println;
import static processing.core.PApplet.round;
import static processing.core.PApplet.trim;
import static teascript.Evaluator.beval;
import static teascript.Evaluator.feval;
import static teascript.Evaluator.isBoolean;
import static teascript.TeaScript.debugMode;
import static teascript.TeaScript.m;

public class Utils {

    static int sint(String s) {
        return (int) (round(Float.parseFloat(s)));
    }

    static int sint(float f) {
        return (int) (round(f));
    }

    static boolean isNegative(String exp, int index) {
        while (--index != -1 && exp.charAt(index) == ' ') {
        }
        if (index == -1) {
            return true;
        }
        switch (exp.charAt(index)) {
            case '(':
            case '+':
            case '-':
            case '*':
            case '/':
            case '|':
            case '&':
            case '=':
            case '>':
            case '<':
            case '^':
            case '!':
            case '~':
                return true;
            default:
                return false;
        }
    }

    static int notNegative(String exp) {
        int cand = exp.lastIndexOf("-");
        while (cand != -1 && isNegative(exp, cand)) {
            cand = exp.lastIndexOf("-", cand - 1);
        }
        return cand;
    }

    static void clsap(String[] s) {
        String acc = "list: ";
        for (String a : s) {
            acc += a + " ";
        }
        println(acc);
    }

    static int noconpos(String exp, String ok, String badrx, String bad) {
        String[] p = exp.split(badrx);
        for (int i = p.length - 1; i >= 0; i--) {
            int lio = p[i].lastIndexOf(ok);
            if (lio != -1) {
                int acc = 0;
                for (int j = 0; j < i; j++) {
                    acc += bad.length() + p[j].length();
                }
                return acc + lio;
            }
        }
        return -1;
    }

    enum Error {
        MISMATCH, NOVAR, NOCOMMAND, FLOATPARSE, ARGCOUNT
    }

    static void error(String s, String message) {
        println("\n\n\nerror " + s + " on line " + (m.debugline + 1) + ":");
        println(m.rawstrings[m.debugline]);
        println(message);
        println("");
        assert (debugMode);
    }

    static boolean il(float a, float b) {
        return a < b || b == -1;
    }

    static boolean equiv(String exp, String expb) {
        if (isBoolean(exp) && isBoolean(expb)) {
            return beval(exp) == beval(expb);
        }
        return feval(exp) == feval(expb);
    }

    static String fstring(String exp) {
        return fstring(exp, "(", ")");
    }

    static String fstring(String exp, String splito, String splitc) {
        int par = 0;
        int[] pars = new int[exp.length()];
        boolean s = false;
        if (splito.equals(splitc)) {
            s = true;
        }
        boolean o = true;
        for (int i = 0; i < exp.length(); i++) {
            if (exp.substring(i, i + 1).equals(splito) && (!s || o)) {
                o = false;
                pars[i] = par++;
            } else if (exp.substring(i, i + 1).equals(splitc) && (!s || !o)) {
                o = true;
                pars[i] = --par;
            } else {
                pars[i] = par;
            }
        }
        if (!o || par != 0) {
            error("MISMATCH", "mismatch on " + exp + " with o " + splito
                    + " and c " + splitc);
        }
        char[] tmp = exp.toCharArray();
        for (int j = 0; j < tmp.length; j++) {
            if (pars[j] > 0) {
                tmp[j] = '#';
            }
        }
        return new String(tmp);
    }

    static String removeArgs(String _exp) {
        String exp = fstring(_exp);
        String res = "";
        for (int i = 0; i < exp.length(); i++) {
            if (exp.charAt(i) != '#') {
                res += exp.charAt(i);
            }
        }
        return res;
    }

    static String smartTrim(String exp) {
        String a;
        String b = exp;
        do {
            a = b;
            b = trimPar(trim(b));
        } while (!a.equals(b));
        return b;
    }

    static String trimPar(String _exp) {
        String exp = _exp.substring(0);
        int par = 0;
        boolean ok = true;
        boolean inq = false;
        for (int i = 1; i < exp.length() - 1; i++) {
            if (exp.charAt(i) == '\"') {
                inq = !inq;
            }
            if (exp.substring(i, i + 1).equals("(") && !inq) {
                par++;
            }
            if (exp.substring(i, i + 1).equals(")") && !inq) {
                par--;
            }
            if (par < 0) {
                ok = false;
            }
        }

        while (exp.length() != 0 && exp.charAt(0) == '('
                && exp.charAt(exp.length() - 1) == ')' && ok) {
            exp = exp.substring(1, exp.length() - 1);
            par = 0;
            for (int i = 1; i < exp.length() - 1; i++) {
                if (exp.substring(i, i + 1).equals("(")) {
                    par++;
                }
                if (exp.substring(i, i + 1).equals(")")) {
                    par--;
                }
                if (par < 0) {
                    ok = false;
                }
            }
        }
        return exp;
    }

    static String[] isplit(String args) {
        if (args.length() == 0 || trim(args).charAt(0) == '#'
                || !args.contains("(")) {
            String[] s = {""};
            return s;
        }
        ArrayList<String> result = new ArrayList<>();
        int start = args.indexOf("(") + 1;
        result.add(args.substring(0, start - 1));

        String args_b = args.substring(start, args.length() - 1);

        String[] split = nsplit(args_b, ',');
        result.addAll(Arrays.asList(split));
        return result.toArray(new String[result.size()]);
    }

    static String[] nsplit(String s, char c) {
        ArrayList<String> result = new ArrayList<>();
        String filtereda = fstring(s, "\"", "\"");
        String filteredb = fstring(s, "(", ")");
        String filtered = "";
        for (int i = 0; i < s.length(); i++) {
            if (filtereda.charAt(i) == '#' || filteredb.charAt(i) == '#') {
                filtered += "#";
            } else {
                filtered += s.charAt(i);
            }
        }
        int start = 0;
        for (int i = 0; i <= filtered.length(); i++) {
            if ((i == filtered.length() || (filtered.charAt(i) == c
                    && (i > 0 && filtered.charAt(i - 1) != '\\')))
                    && i > start) {
                result.add(trim(s.substring(start, i)));
                start = i + 1;
            } else if (i > 0 && filtered.charAt(i - 1) == '\\') {
                s = s.substring(0, i - 1) + s.substring(i);
                filtered = filtered.substring(0, i - 1) + filtered.substring(i);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    static String[] loadStrings(File file) {
        try {
            FileReader fileReader = new FileReader(file);
            ArrayList<String> lines;
            try (BufferedReader bufferedReader =
                    new BufferedReader(fileReader)) {
                lines = new ArrayList<>();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    lines.add(line);
                }
            }
            return lines.toArray(new String[lines.size()]);
        } catch (Exception e) {
            println(e.toString());
            return null;
        }
    }
}
