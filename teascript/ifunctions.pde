String[] boolfs = {"isEmpty", "equals", "equalsic", "startsWith", "endsWith"};
boolean isBoolf(String exp) {
  String targ = "";
  if(exp.indexOf("(") != -1) targ = exp.substring(0, exp.indexOf("("));
  for(String s : boolfs) {
    if(s.equals(targ)) return true;
  }
  return false;
}
boolean doBoolf(String exp) {
  String[] sp = isplit(exp);
  switch(sp[0]) {
    case "isEmpty": return streval(sp[1]).isEmpty();
    case "equals": return streval(sp[1]).equals(streval(sp[2]));
    case "equalsic": return streval(sp[1]).equalsIgnoreCase(streval(sp[2]));
    case "startsWith": if(sp.length == 3) {
                         return streval(sp[1]).startsWith(streval(sp[2]));
                       } else {
                         return streval(sp[1]).startsWith(streval(sp[2]), sint(streval(sp[3])));
                       }
    case "endsWith": return streval(sp[1]).endsWith(streval(sp[2]));
    default: error("NOCOMMAND", "no boolean command: "+exp);
  }
  return false;
}

String[] strfs = {"substring", "lowercase", "uppercase", "trim", "trimPar", "smartTrim"};
boolean isStrf(String exp) {
  String targ = "";
  if(exp.indexOf("(") != -1) targ = exp.substring(0, exp.indexOf("("));
  for(String s : strfs) {
    if(s.equals(targ)) return true;
  }
  return false;
}
String doStrf(String exp) {
  String[] sp = isplit(exp);
  switch(sp[0]) {
    case "substring": if(sp.length == 3) {
                        return streval(sp[1]).substring(sint(streval(sp[2])));
                      } else {
                        return streval(sp[1]).substring(sint(streval(sp[2])), sint(streval(sp[3])));
                      }
    case "lowercase": return streval(sp[1]).toLowerCase();
    case "uppercase": return streval(sp[1]).toUpperCase();
    case "trim": return trim(streval(sp[1]));
    case "trimPar": return trimPar(streval(sp[1]));
    case "smartTrim": return smartTrim(streval(sp[1]));
    default: error("NOCOMMAND", "no string command: "+exp);
  }
  return null;
}

String[] maths = {"abs", "ceil", "floor", "floordiv", "min", "max", "round", "random",
                  "exp", "log", "log10", "pow", "sqrt", "sin", "cos", "tan", "asin", "acos",
                  "atan", "atan2", "sinh", "cosh", "tanh", "todeg", "torad", "sq",
                  "length", "indexOf", "lindexOf", "compTo", "compToic", "ag"};
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
    case "length":   if(isString(sp[1])) return streval(sp[1]).length();
                     else return getFArr(smartTrim(sp[1])).size();
    case "indexOf":  if(sp.length == 3) {
                       return streval(sp[1]).indexOf(streval(sp[2]));
                     } else {
                       return streval(sp[1]).indexOf(streval(sp[2]), sint(streval(sp[3])));
                     }
    case "lindexOf": if(sp.length == 3) {
                       return streval(sp[1]).lastIndexOf(streval(sp[2]));
                     } else {
                       return streval(sp[1]).lastIndexOf(streval(sp[2]), sint(streval(sp[3])));
                     }
    case "compTo":   return streval(sp[1]).compareTo(streval(sp[2]));
    case "compToic": return streval(sp[1]).compareToIgnoreCase(streval(sp[2]));
    case "ag":       return getFArr(sp[1]).get(sint(feval(sp[2])));
    default: error("NOCOMMAND", "no math command: "+exp);
  }
  return -1;
}
