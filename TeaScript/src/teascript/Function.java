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

    /**
     * Constructs new Function given array of its Actions and line it is on.
     * actions is an array of all Actions which are within the Function block.
     * The line is the line (zero indexed) the function is on in the file of
     * script instructions. line is used to calculate he offset from global
     * (file) to local (actions array) Action space. It can be added to a global
     * line number to give the local number. Local numbers are zero indexed
     * starting on the first line after the function definition Action. A new
     * array of if results is made so that recursive calls do not interfere with
     * logic in other levels.
     *
     * @param actions
     * @param line
     */
    Function(Action[] actions, int line) {
        this.actions = actions;
        globalToLocalOffset = -1 - line;
        ifresults = new boolean[actions.length];
    }

    /**
     * Creates duplicate of current function for actual execution. This should
     * be used to create a new Function (with an isolated current line number,
     * set of if results, etc.) before executing. The original Function remains
     * as a sort of template for duplicating off copies for recursive execution.
     *
     * @return New Function, executable without interfering with other copies.
     */
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
