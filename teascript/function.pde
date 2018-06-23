class Function {
  int line = -1;
  Action[] actions;
  int decplace;
  
  Function(Action[] _, int __) {
    actions = _;
    decplace = -1-__;
  }
  Function dup() {
    return new Function(actions, -(decplace+1));
  }
  float execute(String vars) {
    new Action("UPSCOPE()").execute(this);
    String[] args = isplit(trim(vars));
    for(int i = 1; i < args.length; i++) {
      println("VARIABLE(a"+i+","+args[i]+")");
      new Action("VARIABLE(a"+i+","+args[i]+")").execute(this);
      new Action("PRINT(a"+i+")").execute(this); //<>//
    }
    while(++line < actions.length) {
      actions[line].execute(this);
    }
    float ans = m.floats.get(m.floats.size() - 1).get("ans");
    new Action("DOWNSCOPE()").execute(this);
    line = -1;
    return ans;
  }
  void GOTO(int line_) {
    line = line_+decplace;
  }
}
