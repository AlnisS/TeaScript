class Action {
  String thing;
  String[] splits;
  Type type;
  void PRINT() {
    String tmp = streval(splits, 2);
    println(splits[1] + "\t" + tmp);
    logger.println(splits[1] + "\t" + tmp);
  }
  void GOTO() {
    m.line = m.labels.get(splits[1]);
  }
  void LABEL() {
    m.labels.set(splits[1], m.labeltemp);
    type = Type.NONE;
  }
  void BRANCH() {
    if (beval(splits[1])) m.line = m.labels.get(splits[2]);
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
  void execute() {
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
      default: if(splits[0].length() == 0) type = Type.NONE;
               else println("no command " + args);
    }
  }
  String g() {
    return thing;
  }
}
enum Type {PRINT, GOTO, LABEL, BRANCH, VARIABLE, END, UNIT, NONE, UPSCOPE, DOWNSCOPE}
