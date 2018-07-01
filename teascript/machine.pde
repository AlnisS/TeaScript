class Machine {
  int debugline;
  Action[] actions;
  IntDict labels;
  ArrayList<FloatDict> floats;
  ArrayList<StringDict> strings;
  int labeltemp;
  HashMap<String, Function> functions;
  HashMap<String, Function> sfunctions;
  String[] rawstrings;
  
  public Machine() {
    
  }
  void init(String file) {
    labels = new IntDict();
    floats = new ArrayList<FloatDict>();
    strings = new ArrayList<StringDict>();
    functions = new HashMap<String, Function>();
    sfunctions = new HashMap<String, Function>();
    floats.add(new FloatDict());
    strings.add(new StringDict());
    
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
    if(debugMode) {
      new Action("USERFUN(main())").execute(null);
    } else {
      try {new Action("USERFUN(main())").execute(null);} catch(AssertionError e) {exit();} catch(Exception e) {exit();}
    }
  }
}
