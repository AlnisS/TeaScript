package teascript;

import static teascript.TeaScript.m;
import static teascript.Utils.isplit;
import static processing.core.PApplet.trim;

public class Function {
    /**
     * Line this instance is on in its local action space.
     */
    int line = -1;
    /**
     * A copy of this Function's Actions (not modified, for easy iteration).
     */
    Action[] actions;
    /**
     * Latest boolean result for IF/ELIF statement on that line (local space).
     */
    boolean[] ifresults;
    /**
     * Offset global space (Action array in m) -> local (line after FDEF 0).
     */
    int globalToLocalOffset;
    /**
     * Result to return.
     */
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
