

boolean il(float a, float b) { //index less, like less than but ignores -1 indices
  return a < b || b == -1;
}

boolean equiv(String exp, String expb) {
  if(isBoolean(exp) && isBoolean(expb)) return beval(exp) == beval(expb);
  return feval(exp) == feval(expb);
}

String fstring(String exp) {
  return fstring(exp, "(", ")");
}

String fstring(String exp, String splito, String splitc) {
  int par = 0;
  int[] pars = new int[exp.length()];
  boolean s = false;
  if(splito.equals(splitc)) s = true;
  boolean o = true;
  for(int i = 0; i < exp.length(); i++) {
    if(exp.substring(i, i+1).equals(splito) && (!s || o)) {
      o = false;
      pars[i] = par++;
    }
    else if(exp.substring(i, i+1).equals(splitc) && (!s || !o)) {
      o = true;
      pars[i] = --par;
    }
    else pars[i] = par;
  }
  char[] tmp = exp.toCharArray();
  for(int j = 0; j < tmp.length; j++) {
    if(pars[j] > 0) {
      tmp[j] = '#';
    }
  }
  return new String(tmp);
}

String removeArgs(String _exp) {
  String exp = fstring(_exp);
  String res = "";
  for(int i = 0; i < exp.length(); i++) {
    if(exp.charAt(i) != '#') res += exp.charAt(i);
  }
  return res;
}

String trimPar(String _exp) {
  String exp = _exp.substring(0);
  int par = 0;
  boolean ok = true;
  boolean inq = false;
  for(int i = 1; i < exp.length()-1; i++) {
    if(exp.charAt(i) == '\"') inq = !inq;
    if(exp.substring(i, i+1).equals("(") && !inq) par++;
    if(exp.substring(i, i+1).equals(")") && !inq) par--;
    if(par < 0) ok = false;
  }
  
  while(exp.length() != 0 && exp.charAt(0) == '(' && exp.charAt(exp.length()-1) == ')' && ok) {
    exp = exp.substring(1, exp.length()-1);
    par = 0;
    for(int i = 1; i < exp.length()-1; i++) {
      if(exp.substring(i, i+1).equals("(")) par++;
      if(exp.substring(i, i+1).equals(")")) par--;
      if(par < 0) ok = false;
    }
  }
  return exp;
}
boolean isBoolean(String exp) {
  return exp.indexOf(">") != -1  || exp.indexOf("<") != -1 || exp.indexOf("!=") != -1 || exp.indexOf("==") != -1 ||
         exp.indexOf("||") != -1 || exp.indexOf("&&") != -1|| exp.indexOf(">=") != -1 || exp.indexOf("<=") != -1 ||
         exp.indexOf("true")!= -1|| exp.indexOf("false")!= -1;
}
String[] isplit(String args) {
  if(args.length() == 0 || trim(args).charAt(0) == '#') {
    String[] s = {""};
    return s;
  }
  ArrayList<String> result = new ArrayList<String>();
  int start = args.indexOf("(") + 1;
  result.add(args.substring(0, start - 1));
  
  String args_b = args.substring(start, args.length() - 1);
  
  String filtereda = fstring(args_b, "\"", "\"");
  String filteredb = fstring(args_b, "(",  ")");
  String filtered = "";
  for(int i = 0; i < args_b.length(); i++) {
    if(filtereda.charAt(i) == '#' || filteredb.charAt(i) == '#') filtered += "#";
    else filtered += args_b.charAt(i);
  }
  start = 0;
  for(int i = 0; i <= filtered.length(); i++) {
    if((i == filtered.length() || filtered.charAt(i) == ',') && i > start) {
      result.add(trim(args_b.substring(start, i)));
      start = i + 1;
    }
  }
  return result.toArray(new String[result.size()]);
}
