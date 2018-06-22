class Machine {
  private int line;
  //private String[] actions_;
  Action[] actions;
  IntDict labels;
  ArrayList<FloatDict> floats;
  int labeltemp;
  
  public Machine() {
    
  }
  void init(String file) {
    line = -1;
    labels = new IntDict();
    floats = new ArrayList<FloatDict>();
    floats.add(new FloatDict());
    
    String[] rawstrings = loadStrings(file);
    actions = new Action[rawstrings.length];
    for (int i = 0; i < actions.length; i++) actions[i] = new Action(rawstrings[i]);

    for (int i = 0; i < actions.length; i++) {
      Action a = actions[i];
      labeltemp = i;
      if (a.type == Type.LABEL) a.execute();
    }
  }
  public void next() {
    action(++line);
  }

  void action(int line) {
    actions[line].execute();
  }
}
