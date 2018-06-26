String[] maths = {"abs", "ceil", "floor", "floordiv", "min", "max", "round", "random",
                  "exp", "log", "log10", "pow", "sqrt", "sin", "cos", "tan", "asin", "acos",
                  "atan", "atan2", "sinh", "cosh", "tanh", "todeg", "torad", "sq"};
boolean isMath(String exp) {
  String targ = "";
  if(exp.indexOf("(") != -1) targ = exp.substring(0, exp.indexOf("("));
  for(String s : maths) {
    if(s.equals(targ)) return true;
  }
  return false;
}
float doMath(String exp) {
  String[] sp = isplit(exp);
  switch(sp[0]) {
    case "abs":      return abs(feval(sp[1]));
    case "ceil":     return ceil(feval(sp[1]));
    case "floor":    return floor(feval(sp[1]));
    case "floordiv": return floor(feval(sp[1]) / feval(sp[2]));
    case "min":      return min(feval(sp[1]), feval(sp[2]));
    case "max":      return max(feval(sp[1]), feval(sp[2]));
    case "round":    return round(feval(sp[1]));
    case "random":   return random(feval(sp[1]), feval(sp[2]));
    case "exp":      return exp(feval(sp[1]));
    case "log":      return log(feval(sp[1]));
    case "log10":    return log(feval(sp[1])) / log(10);
    case "pow":      return pow(feval(sp[1]), feval(sp[2]));
    case "sqrt":     return sqrt(feval(sp[1]));
    case "sin":      return sin(feval(sp[1]));
    case "cos":      return cos(feval(sp[1]));
    case "tan":      return tan(feval(sp[1]));
    case "asin":     return asin(feval(sp[1]));
    case "acos":     return acos(feval(sp[1]));
    case "atan":     return atan(feval(sp[1]));
    case "atan2":    return atan2(feval(sp[1]), feval(sp[2]));
    case "sinh":     return (exp(feval(sp[1]))-exp(-feval(sp[1]))) / 2;
    case "cosh":     return (exp(feval(sp[1]))+exp(-feval(sp[1]))) / 2;
    case "tanh":     return (exp(2*feval(sp[1])) - 1) / (exp(2*feval(sp[1])) + 1);
    case "todeg":    return degrees(feval(sp[1]));
    case "torad":    return radians(feval(sp[1]));
    case "sq":       return sq(feval(sp[1]));
    default: error("NOCOMMAND", "no math command: "+exp);
  }
  return -1;
}
