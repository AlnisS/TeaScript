class Action {
  String thing;
  String[] splits;
  Type type;
  void PRINT(Machine m) {
    String tmp = streval(splits, 2);
    println(splits[1] + "\t" + tmp);
    logger.println(splits[1] + "\t" + tmp);
  }
  void GOTO(Machine m) {
    m.line = m.labels.get(splits[1]);
  }
  void LABEL(Machine m) {
    
  }
  void BRANCH(Machine m) {
    if (beval(splits[1])) m.line = m.labels.get(splits[2]);
  }
  void VARIABLE(Machine m) {
    m.floats.set(splits[1], feval(splits[2]));
  }
  void END(Machine m) {
    end();
  }
  void UNIT(Machine m) {
    
  }
  void NONE(Machine m) {
    
  }
  void execute(Machine m) {
    //String[] args = splits;
    switch(type) {
      case PRINT:    PRINT(m);    break;
      case GOTO:     GOTO(m);     break;
      case LABEL:    LABEL(m);    break;
      case BRANCH:   BRANCH(m);   break;
      case VARIABLE: VARIABLE(m); break;
      case END:      END(m);      break;
      case UNIT:     UNIT(m);     break;
      case NONE:     NONE(m);     break;
      default: println("no command " + args);
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
      default: if(splits[0].charAt(0) == '#') type = Type.NONE;
               else println("no command " + args);
    }
  }
  String g() {
    return thing;
  }
}
enum Type {PRINT, GOTO, LABEL, BRANCH, VARIABLE, END, UNIT, NONE}
