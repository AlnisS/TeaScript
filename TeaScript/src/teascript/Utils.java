package teascript;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import static teascript.TeaScript.debugMode;
import static teascript.TeaScript.m;
import static processing.core.PApplet.println;
import static processing.core.PApplet.round;
import static processing.core.PApplet.trim;
import static teascript.BMan.beval;
import static teascript.BMan.isBoolean;
import static teascript.FMan.feval;

/**
 * Misc utils for string/file wrangling along with math and other stuff.
 *
 * @author chrx
 */
public class Utils {

    /**
     * "smart" cast to integer: parses, rounds, then casts string to int.
     *
     * @param s String to turn into int.
     * @return s turned into an int.
     */
    static int sint(String s) {
        return (int) (round(Float.parseFloat(s)));
    }

    /**
     * "smart" cast to integer: rounds, then casts float to int.
     *
     * @param f float to turn into int.
     * @return f turned into an int.
     */
    static int sint(float f) {
        return (int) (round(f));
    }

    /**
     * Given index of "-" in exp, returns whether it is a negative sign.
     * Searches backward to find which char is in front of it. If it is a math
     * op/boolean op symbol or the start of the expression, returns true. Else,
     * returns false.
     *
     * @param exp String to search.
     * @param index index of "-" to search back from.
     * @return whether the "-" at index in exp is a negative sign.
     */
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

    /**
     * Returns last index of "-" in exp which is not a negative sign.
     *
     * @param exp String to search.
     * @return last index of "-" in exp which is not a negative sign.
     */
    static int notNegative(String exp) {
        int cand = exp.lastIndexOf("-");
        while (cand != -1 && isNegative(exp, cand)) {
            cand = exp.lastIndexOf("-", cand - 1);
        }
        return cand;
    }

    /**
     * Pretty prints an array of Strings.
     *
     * @param s array of Strings to pretty print.
     */
    static void clsap(String[] s) {
        String acc = "list: ";
        for (String a : s) {
            acc += a + " ";
        }
        println(acc);
    }

    /**
     * Returns last non-conflicting (index != bad regex) of String ok in exp.
     * For example, there might be a String with an > sign in it. However, this
     * > sign may be part of a >= sign. Only using lastIndexOf may give the
     * index of this sign. Instead, it may be more correct to ignore this >
     * sign. To do so, do: <code>var = noconpos(string to search, ">", ">=",
     * ">=")</code>. In this case, the regex is the same as the bad String.
     * However, in other cases, be careful to type the regex String such that
     * the resulting String is itself the valid regex expression matching the
     * bad string. For example: "\\*\\*" would be the regex version of "**" as
     * the bad String.
     *
     * @param exp String in which to search for non-conflicting position.
     * @param ok good String to search for.
     * @param badrx regex version of bad String.S
     * @param bad plain version of bad String.
     * @return index of last non-conflicting position of ok in exp (not bad).
     */
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

    /**
     * The various TeaScript script errors.
     */
    enum Error {
        MISMATCH, NOVAR, NOCOMMAND, FLOATPARSE, ARGCOUNT
    }

    /**
     * "handles" a script error by sort of pretty printing and maybe stopping.
     *
     * @param s type of Error (as a String, enum is broken).
     * @param message hopefully nice desciption of what happened.
     */
    static void error(String s, String message) {
        println("\n\n\nerror " + s + " on line " + (m.debugline + 1) + ":");
        println(m.rawstrings[m.debugline]);
        println(message);
        println("");
        assert (debugMode);
    }

    /**
     * "Integligent" check of lesser than: if b == -1, it ignores it.
     *
     * @param a first part of lesser than.
     * @param b second part of lesser than.
     * @return "integligent" lesser than check (if second arg is -1, true).
     */
    static boolean il(float a, float b) {
        return a < b || b == -1;
    }

    /**
     * Checks if two Strings are equal as booleans or floats.
     *
     * @param exp first String to compare.
     * @param expb second String to compare.
     * @return whether exp and expb are equal as booleans or floats.
     */
    static boolean equiv(String exp, String expb) {
        if (isBoolean(exp) && isBoolean(expb)) {
            return beval(exp) == beval(expb);
        }
        return feval(exp) == feval(expb);
    }

    /**
     * Filters String exp using <code>fstring(exp, "(", ")")</code>.
     *
     * @param exp String to filter
     * @return String exp flitered using "(" and ")".
     */
    static String fstring(String exp) {
        return fstring(exp, "(", ")");
    }

    /**
     * Filters String exp by replacing chars within filter bounds with '#'.
     * Iterates over exp, incrementing the "parentheses" counter for every
     * opening String and decrementing it for every "closing" string while also
     * creating an array of ints tracking this "depth". If the opening and
     * closing Strings are equivalent, it toggles between open and closed
     * instead. If the parentheses don't match up/cancel out, throws a script
     * error. Finally, replaces chars which should be filtered with '#' chars.
     *
     * @param exp String to filter.
     * @param splito opening char for filter.
     * @param splitc closing char for filter.
     * @return String exp filtered with opener splito and closer splitc.
     */
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

    /**
     * Strips the arguments out of a function call (foo(a, b) -> foo()).
     *
     * @param _exp String to remove arguments from (assumes it is a function).
     * @return _exp with arguments stripped.
     */
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

    /**
     * Trims all whitespace and matching parentheses off the ends of String exp.
     *
     * @param exp String to trim.
     * @return exp with parentheses and whitespace trimmed off.
     */
    static String smartTrim(String exp) {
        String a;
        String b = exp;
        do {
            a = b;
            b = trimPar(trim(b));
        } while (!a.equals(b));
        return b;
    }

    /**
     * Trims matching parentheses off the ends of String _exp. Runs through to
     * check if the outer parentheses match, then whacks them off and continues
     * to do so until they don't or aren't parentheses.
     *
     * @param _exp String to trim parentheses off of.
     * @return _exp with parentheses trimmed off of the ends.
     */
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
            try (BufferedReader bufferedReader
                    = new BufferedReader(fileReader)) {
                lines = new ArrayList<>();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    lines.add(line);
                }
            }
            return lines.toArray(new String[lines.size()]);
        } catch (FileNotFoundException e) {
            println("\ncouldn't find specified file for execution.\n");
            System.exit(0);
            return null;
        } catch (Exception e) {
            println(e.toString());
            return null;
        }
    }

    static float mod(float a, float b) {
        return (a % b + b) % b;
    }

    static String fname(String cand) {
        if(cand.indexOf("(") != -1) {
            return cand.substring(0, cand.indexOf("("));
        }
        return cand;
    }

    static String iRemoveArgs(String exp) {
        String targ = removeArgs(exp);
        if(targ.length() > 2) targ = targ.substring(0, targ.length() - 2);
        return targ;
    }

    static File stringToFile(String file_) {
        String f = System.getProperty("user.dir");
        f = f.substring(0, f.lastIndexOf("/") + 1);
        return new File(f + file_);
    }

    static void help() {
        String s = "                    ";
        println();
        println("-f    --file        " +
                "Specifies .tea file to run relative to the directory above\n" +
            s + "the one in which the project/jar resides. This is used for\n" +
            s + "easy specification of a file when working in the IDE. For\n" +
            s + "an example, see how the test.tea file in this repo is one\n" +
            s + "folder up from the IntelliJ IDEA project.\n");
        println("-p    --path        " +
                "Specifies the exact global path to the .tea file to run\n" +
            s + "including the name of the file itself. Use of the \"~\"\n" +
            s + "character to represent the user's home directory works.\n");
        println("-t    --test        " +
                "Runs the unit tests after running the specified .tea file.\n" +
            s + "It is intended for use in the IDE because the path to the\n" +
            s + "unit test file is somewhat hardcoded in a way equivalent\n" +
            s + "to \"-f units.tea\". If you want to run it from an IDE,\n" +
            s + "ensure the file is in the same place relative to the IDE\n" +
            s + "as it is in this repo. If using an exported .jar (other\n" +
            s + "than in the context of an added library in an IDE), the\n" +
            s + "units.tea file must be one directory up from the .jar.\n");
        println("-l    --logtime     " +
                "Log the amount of time taken to load and execute the\n" +
            s + "script (including unit tests if applicable). Will only be\n" +
            s + "called if the .tea file calls END() to halt execution\n" +
            s + "(instead of running to the end of its main() function).\n");
        println("-d    --debug       " +
                "Enables debug mode. Not recommended for normal script\n" +
            s + "execution. This prevents the interpreter from trying to\n" +
            s + "swallow the Java stack traces of script errors. By\n" +
            s + "default, the interpreter tries to pretty print the error\n" +
            s + "from parsing the script. It will still try to do so in\n" +
            s + "debug mode, but things may disassemble. Basically, if\n" +
            s + "you're trying to debug your script, don't use it, but if\n" +
            s + "you are trying to debug the TeaScript interpreter, use it.\n");
        println("-h    --help        " +
                "Prints an incomprehensible and poorly worded ramble about\n" +
            s + "TeaScript, allegedly on how to use it.\n");
        System.exit(0);
    }
}
