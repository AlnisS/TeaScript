class Machine {
  private int line;
  //private String[] actions_;
  private Action[] actions;
  private IntDict labels;
  public FloatDict floats;
  public Machine() {
    
  }
  void init(String file) {
    line = -1;
    labels = new IntDict();
    floats = new FloatDict();

    String[] rawstrings = loadStrings(file);
    actions = new Action[rawstrings.length];
    for (int i = 0; i < actions.length; i++) actions[i] = new Action(rawstrings[i]);

    for (int i = 0; i < actions.length; i++) {
      Action a = actions[i];
      if (a.type == Type.LABEL) labels.set(a.splits[1], i);
      if (a.type == Type.VARIABLE) floats.set(a.splits[1], feval(a.splits[2]));
    }
  }
  public void next() {
    action(++line);
  }

  void action(int line) {
    actions[line].execute(this);
  }
}
