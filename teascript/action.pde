class Action {
  String thing;
  String[] splits;
  Type type;
  Function jumpcall;
  void PRINT() {
    if(splits.length == 2) splits = new String[]{splits[0], "N", splits[1]};
    String tmp = streval(splits, 2);
    println(splits[1] + "\t" + tmp);
  }
  void GOTO() {
    jumpcall.GOTO(m.labels.get(splits[1]));
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
  void NONE() {
    
  }
  void UPSCOPE() {
    m.floats.add(new FloatDict());
  }
  void DOWNSCOPE() {
    m.floats.remove(m.floats.size()-1);
  }
  void USERFUN() {
    m.functions.get(removeArgs(splits[1])).dup().execute(splits[1]);
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
    print(""); //<>//
  }
  void RET() {
    jumpcall.RET(feval(splits[1]));
  }
  void GVAR() {
    m.floats.get(0).set(splits[1], feval(splits[2]));
  }
  void U() {
    prettyUnitPass(str(m.debugline+1), splits[1], streval(splits, 2));
    type = Type.NONE;
  }
  void IF() {
    if(!(jumpcall.ifresults[jumpcall.line] = beval(splits[1]))) {
      skiptoendofif(true);
    }
  }
  void ELSE() {
    if(anytrue()) {
      skiptoendofif(false);
    }
  }
  void ELIF() {
    if(anytrue()) {
      skiptoendofif(false);
    } else IF();
  }
  void ENDIF() {
    
  }
  boolean anytrue() {
    int ifs = -1;
    int tline = jumpcall.line;
    while(ifs < 0) {
      Type t = jumpcall.actions[--tline].type;
      if((t == Type.IF || t == Type.ELIF) && ifs == -1 && jumpcall.ifresults[tline]) return true;
      if(t == Type.IF) ifs++;
      if(t == Type.ENDIF) ifs--;
    }
    return false;
  }
  void skiptoendofif(boolean inif) {
    int ifs = 1;
    while(ifs > 0) {
      Type t = jumpcall.actions[++jumpcall.line].type;
      if(t == Type.IF) ifs++;
      if(t == Type.ENDIF || (inif && (ifs == 1 && (t == Type.ELSE || t == Type.ELIF)))) ifs--;
    }
    jumpcall.line--;
  }
  void s(Type t, int args) {
    type = t;
    if(splits.length - 1 < args) error("ARGCOUNT", "expected "+args+" arguments, got "+(splits.length-1)+".");
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
      case GVAR:     GVAR();     break;
      case U:        U();        break;
      case IF:       IF();       break;
      case ENDIF:    ENDIF();    break;
      case ELSE:     ELSE();     break;
      case ELIF:     ELIF();     break;
    }
  }
  Action(String args) {
    thing = trim(args);
    splits = isplit(thing);
    switch(splits[0]) {
      case "PRINT":    s(Type.PRINT, 1);    break;
      case "GOTO":     s(Type.GOTO, 1);     break;
      case "LABEL":    s(Type.LABEL, 1);    break;
      case "BRANCH":   s(Type.BRANCH, 2);   break;
      case "VARIABLE": s(Type.VARIABLE, 2); break;
      case "END":      s(Type.END, 0);      break;
      case "UPSCOPE":  s(Type.UPSCOPE, 0);  break;
      case "DOWNSCOPE":s(Type.DOWNSCOPE, 0);break;
      case "FDEF":     s(Type.FDEF, 1);     break;
      case "EFDEF":    s(Type.EFDEF, 0);    break;
      case "USERFUN":  s(Type.USERFUN, 1);  break;
      case "VARSET":   s(Type.VARSET, 2);   break;
      case "REMVAR":   s(Type.REMVAR, 1);   break;
      case "BRKPT":    s(Type.BRKPT, 0);    break;
      case "RET":      s(Type.RET, 1);      break;
      case "GVAR":     s(Type.GVAR, 2);     break;
      case "U":        s(Type.U, 2);        break;
      case "IF":       s(Type.IF, 1);       break;
      case "ENDIF":    s(Type.ENDIF, 0);    break;
      case "ELSE":     s(Type.ELSE, 0);     break;
      case "ELIF":     s(Type.ELIF, 1);     break;
      default: if(splits[0].length() == 0) type = Type.NONE;
               else error("NOCOMMAND", "command "+splits[0]+" not found.");
    }
  }
}
enum Type {PRINT, GOTO, LABEL, BRANCH, VARIABLE, END, NONE, UPSCOPE, DOWNSCOPE, USERFUN, FDEF, EFDEF, VARSET, REMVAR, BRKPT, RET, GVAR, U, IF, ENDIF, ELSE, ELIF}
