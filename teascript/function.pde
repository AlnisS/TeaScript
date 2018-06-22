class Function {
  int line = -1;
  Action[] actions;
  int decplace;
  
  Function(Action[] _, int __) {
    actions = _;
    decplace = -1-__;
  }
  Function dup() {
    return new Function(actions, decplace);
  }
  void execute() {
    new Action("UPSCOPE()").execute(this);
    while(++line < actions.length) {
      actions[line].execute(this);
    }
    new Action("DOWNSCOPE()").execute(this);
  }
  void GOTO(int line_) {
    line = line_-decplace;
  }
}
