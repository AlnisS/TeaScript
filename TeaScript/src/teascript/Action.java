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

public class Action {

    String creationString;
    String[] splits;
    Type type;
    Function parentFun;

    void PRINT() {
        if (splits.length < 3) {
            splits = new String[]{splits[0], "N", splits[1]};
        }
        String tmp = streval(splits[2]);
        println(splits[1] + "\t" + tmp);
    }

    void GOTO() {
        parentFun.GOTO(m.labels.get(splits[1]));
    }

    void LABEL() {
        m.labels.set(splits[1], m.labeltemp);
        deactivate();
    }

    void BRANCH() {
        if (beval(splits[1])) {
            new Action("GOTO(" + splits[2] + ")").execute(parentFun);
        }
    }

    void VARIABLE() {
        if (isString(splits[2])) {
            setSVar(m.strings.size() - 1, splits[1], streval(splits[2]));
        } else if (isBoolean(splits[2])) {
            setBVar(m.booleans.size() - 1, splits[1], beval(splits[2]));
        } else {
            setFVar(m.floats.size() - 1, splits[1], feval(splits[2]));
        }
    }

    void END() {
        TeaScript.end();
    }

    void NONE() {

    }

    void UPSCOPE() {
        m.floats.add(new FloatDict());
        m.strings.add(new StringDict());
        m.booleans.add(new IntDict());
        m.farrs.add(new HashMap<>());
        m.sarrs.add(new HashMap<>());
        m.barrs.add(new HashMap<>());
    }

    void DOWNSCOPE() {
        m.floats.remove(m.floats.size() - 1);
        m.strings.remove(m.strings.size() - 1);
        m.booleans.remove(m.booleans.size() - 1);
        m.farrs.remove(m.farrs.size() - 1);
        m.sarrs.remove(m.sarrs.size() - 1);
        m.barrs.remove(m.barrs.size() - 1);
    }

    void USERFUN() {
        m.functions.get(removeArgs(splits[1])).dup().execute(splits[1]);
    }

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

    void EFDEF() {

    }

    void VARSET() {
        m.floats.get(m.floats.size() - 2).set(splits[1], feval(splits[2]));
    }

    void REMVAR() {
        m.floats.get(m.floats.size() - 1).remove(splits[1]);
    }

    void BRKPT() {
        print("");
    }

    void RET() {
        parentFun.RET(streval(splits[1]));
    }

    void GVAR() {
        m.floats.get(0).set(splits[1], feval(splits[2]));
    }

    void U() {
        prettyUnitPass(str(m.debugline + 1), splits[1], streval(splits[2]));
        deactivate();
    }

    void IF() {
        if (!(parentFun.ifresults[parentFun.line] = beval(splits[1]))) {
            skiptoendofif();
        }
    }

    void ELSE() {
        if (anytrue()) {
            skiptoendofif();
        }
    }

    void ELIF() {
        if (anytrue()) {
            skiptoendofif();
        } else {
            IF();
        }
    }

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
