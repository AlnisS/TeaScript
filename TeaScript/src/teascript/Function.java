package teascript;

import static teascript.TeaScript.m;
import static teascript.Utils.isplit;
import static processing.core.PApplet.trim;

public class Function {

    int line = -1;
    Action[] actions;
    boolean[] ifresults;
    int globalToLocalOffset;
    String ans;

    Function(Action[] actions, int line) {
        this.actions = actions;
        globalToLocalOffset = -1 - line;
        ifresults = new boolean[actions.length];
    }

    Function dup() {
        return new Function(actions, -(globalToLocalOffset + 1));
    }

    String execute(String vars) {
        m.debugline = -1 - globalToLocalOffset;
        new Action("UPSCOPE()").execute(this);
        String[] args = isplit(trim(vars));
        for (int i = 1; i < args.length; i++) {
            new Action("VARIABLE(a_temp_" + i + "," + args[i] + ")").execute(this);
        }
        for (int i = 1; i < args.length; i++) {
            new Action("VARIABLE(a" + i + ",a_temp_" + i + ")").execute(this);
        }

        while (++line < actions.length) {
            m.debugline = line - globalToLocalOffset;
            actions[line].execute(this);
        }
        m.debugline = -1 - globalToLocalOffset;
        new Action("DOWNSCOPE()").execute(this);
        line = -1;
        return ans;
    }

    void RET(String ans) {
        this.ans = ans;
        line = actions.length;
    }

    void GOTO(int line_) {
        line = line_ + globalToLocalOffset;
    }
}
