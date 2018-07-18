package teascript;

import static processing.core.PApplet.trim;
import static teascript.TeaScript.m;
import static teascript.Utils.isplit;

/**
 *
 * @author alnis
 */
public class Function {

    int line = -1;
    Action[] actions;
    boolean[] ifresults;
    int decplace;  //adjustment to local action space from global action space
    String ans;

    Function(Action[] actions, int line) {
        this.actions = actions;
        decplace = -1 - line;
        ifresults = new boolean[actions.length];
    }

    Function dup() {
        return new Function(actions, -(decplace + 1));
    }

    String execute(String vars) {
        m.debugline = -1 - decplace;
        new Action("UPSCOPE()").execute(this);
        String[] args = isplit(trim(vars));
        for (int i = 1; i < args.length; i++) {
            new Action("VARIABLE(a_temp_"+i+"," + args[i] + ")").execute(this);
        }
        for (int i = 1; i < args.length; i++) {
            new Action("VARIABLE(a" + i + ",a_temp_" + i + ")").execute(this);
        }
        while (++line < actions.length) {
            m.debugline = line - decplace;
            actions[line].execute(this);
        }
        m.debugline = -1 - decplace;
        new Action("DOWNSCOPE()").execute(this);
        line = -1;
        return ans;
    }

    void RET(String ans) {
        this.ans = ans;
        line = actions.length;
    }

    void GOTO(int line_) {
        line = line_ + decplace;
    }
}
