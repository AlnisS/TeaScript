class Action {
  String thing;
  String[] splits;
  Type type;
  Function jumpcall;
  void PRINT() {
    if(splits.length == 2) splits = new String[]{splits[0], "N", splits[1]};
    String tmp = streval(splits, 2);
    println(splits[1] + "\t" + tmp);
    logger.println(splits[1] + "\t" + tmp);
  }
  void GOTO() {
    if(jumpcall == null) m.line = m.labels.get(splits[1]);
    else jumpcall.GOTO(m.labels.get(splits[1]));
  }
  void LABEL() {
    m.labels.set(splits[1], m.labeltemp);
    type = Type.NONE;
  }
  void BRANCH() {
    if (beval(splits[1])) new Action("GOTO("+splits[2]+")").execute(jumpcall);
  }
  void VARIABLE() {
    m.floats.get(m.floats.size() - 1).set(splits[1], feval(splits[2]));
  }
  void END() {
    end();
  }
  void UNIT() {
    
  }
  void NONE() {
    
  }
  void UPSCOPE() {
    m.floats.add(new FloatDict());
  }
  void DOWNSCOPE() {
    m.floats.remove(m.floats.size()-1);
  }
  void USERFUN() {
    //m.functions.get(splits[1]).dup().execute();
  }
  void FDEF() {
    ArrayList<Action> actions = new ArrayList<Action>();
    for(int i = m.labeltemp+1; m.actions[i].type != Type.EFDEF; i++) {
      actions.add(m.actions[i]);
    }
    m.functions.put(splits[1], new Function(actions.toArray(new Action[actions.size()]), m.labeltemp));
    type = Type.NONE;
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
    jumpcall.RET(feval(splits[1]));
  }
  void execute(Function f) {
    jumpcall = f;
    switch(type) {
      case PRINT:    PRINT();    break;
      case GOTO:     GOTO();     break;
      case LABEL:    LABEL();    break;
      case BRANCH:   BRANCH();   break;
      case VARIABLE: VARIABLE(); break;
      case END:      END();      break;
      case UNIT:     UNIT();     break;
      case NONE:     NONE();     break;
      case UPSCOPE:  UPSCOPE();  break;
      case DOWNSCOPE:DOWNSCOPE();break;
      case USERFUN:  USERFUN();  break;
      case FDEF:     FDEF();     break;
      case EFDEF:    EFDEF();    break;
      case VARSET:   VARSET();   break;
      case REMVAR:   REMVAR();   break;
      case BRKPT:    BRKPT();    break;
      case RET:      RET();      break;
    }
  }
  Action(String args) {
    thing = trim(args);
    splits = isplit(thing);
    switch(splits[0]) {
      case "PRINT":    type = Type.PRINT;    break;
      case "GOTO":     type = Type.GOTO;     break;
      case "LABEL":    type = Type.LABEL;    break;
      case "BRANCH":   type = Type.BRANCH;   break;
      case "VARIABLE": type = Type.VARIABLE; break;
      case "END":      type = Type.END;      break;
      case "UNIT":     type = Type.UNIT;     break;
      case "UPSCOPE":  type = Type.UPSCOPE;  break;
      case "DOWNSCOPE":type = Type.DOWNSCOPE;break;
      case "FDEF":     type = Type.FDEF;     break;
      case "EFDEF":    type = Type.EFDEF;    break;
      case "USERFUN":  type = Type.USERFUN;  break;
      case "VARSET":   type = Type.VARSET;   break;
      case "REMVAR":   type = Type.REMVAR;   break;
      case "BRKPT":    type = Type.BRKPT;    break;
      case "RET":      type = Type.RET;      break;
      default: if(splits[0].length() == 0) type = Type.NONE;
               else error("NOCOMMAND", "command "+splits[0]+" not found.");
    }
  }
  String g() {
    return thing;
  }
}
enum Type {PRINT, GOTO, LABEL, BRANCH, VARIABLE, END, UNIT, NONE, UPSCOPE, DOWNSCOPE, USERFUN, FDEF, EFDEF, VARSET, REMVAR, BRKPT, RET}
