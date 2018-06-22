class Machine {
  private int line;
  Action[] actions;
  IntDict labels;
  ArrayList<FloatDict> floats;
  int labeltemp;
  HashMap<String, Function> functions;
  
  public Machine() {
    
  }
  void init(String file) {
    line = -1;
    labels = new IntDict();
    floats = new ArrayList<FloatDict>();
    functions = new HashMap<String, Function>();
    floats.add(new FloatDict());
    
    String[] rawstrings = loadStrings(file);
    actions = new Action[rawstrings.length];
    for (int i = 0; i < actions.length; i++) actions[i] = new Action(rawstrings[i]);

    for (int i = 0; i < actions.length; i++) {
      Action a = actions[i];
      labeltemp = i;
      if (a.type == Type.LABEL || a.type == Type.FDEF) a.execute(null);
    }
  }
  public void next() {
    action(++line);
  }

  void action(int line) {
    actions[line].execute(null);
  }
}
