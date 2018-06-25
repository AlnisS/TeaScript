class Machine {
  int debugline;
  Action[] actions;
  IntDict labels;
  ArrayList<FloatDict> floats;
  int labeltemp;
  HashMap<String, Function> functions;
  String[] rawstrings;
  
  public Machine() {
    
  }
  void init(String file) {
    labels = new IntDict();
    floats = new ArrayList<FloatDict>();
    functions = new HashMap<String, Function>();
    floats.add(new FloatDict());
    
    rawstrings = loadStrings(file);
    actions = new Action[rawstrings.length];
    for (int i = 0; i < actions.length; i++) {
      debugline = i;
      actions[i] = new Action(rawstrings[i]);
    }

    for (int i = 0; i < actions.length; i++) {
      Action a = actions[i];
      labeltemp = i;
      if (a.type == Type.LABEL || a.type == Type.FDEF || a.type == Type.GVAR) a.execute(null);
    }
    new Action("USERFUN(init())").execute(null);
  }
  void action() {
    new Action("USERFUN(main())").execute(null);
  }
}
