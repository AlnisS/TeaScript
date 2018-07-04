class Machine {
  int debugline;
  Action[] actions;
  IntDict labels;
  ArrayList<FloatDict> floats;
  ArrayList<StringDict> strings;
  ArrayList<IntDict> booleans;
  int labeltemp;
  HashMap<String, Function> functions;
  HashMap<String, Function> sfunctions;
  HashMap<String, Function> bfunctions;
  ArrayList<HashMap<String, ArrayList<Float>>> farrs;
  String[] rawstrings;
  
  public Machine() {
    
  }
  void init(String file) {
    labels = new IntDict();
    floats = new ArrayList<FloatDict>();
    strings = new ArrayList<StringDict>();
    booleans = new ArrayList<IntDict>();
    functions = new HashMap<String, Function>();
    sfunctions = new HashMap<String, Function>();
    bfunctions = new HashMap<String, Function>();
    farrs = new ArrayList<HashMap<String, ArrayList<Float>>>();
    floats.add(new FloatDict());
    strings.add(new StringDict());
    booleans.add(new IntDict());
    farrs.add(new HashMap<String, ArrayList<Float>>());
    
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
