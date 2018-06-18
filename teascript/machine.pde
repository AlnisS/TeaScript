class Machine {
  private int line;
  private String actions[];
  //private ArrayList<String[]>
  private IntDict labels;
  public FloatDict floats;
  public Machine() {}
  public void init(String file) {
    actions = loadStrings(file);
    line = -1;
    labels = new IntDict();
    floats = new FloatDict();
    for(int i = 0; i < actions.length; i++) {
      String[] s = isplit(actions[i]);
      if(s[0].charAt(0) == 'L') {
        labels.set(s[1], i);
      }
      if(s[0].charAt(0) == 'V') {
        floats.set(s[1], feval(s[2]));
      }
    }
  }
  public void next() {
    action(actions[++line]);
  }
  
  void action(String _args) {
    String[] args = isplit(_args);
    //printArray(args);
    char c = args[0].charAt(0);
    switch(c) {
      case 'P': //println(millis() - st);
                println(streval(args, 2));
                break;
      case 'G': line = labels.get(args[1]);
                break;
      case 'L': break;
      case 'B': if(beval(args[1])) {
                  line = labels.get(args[2]);
                }
                break;
      case 'V': floats.set(args[1], feval(args[2]));
                break;
      default:  println("no command " + c + " " + args);
    }
  }
}
