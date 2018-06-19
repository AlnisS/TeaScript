

boolean il(float a, float b) { //index less, like less than but ignores -1 indices
  return a < b || b == -1;
}

boolean equiv(String exp, String expb) {
  if(isBoolean(exp) && isBoolean(expb)) return beval(exp) == beval(expb);
  return feval(exp) == feval(expb);
}

String fstring(String exp) {
  int par = 0;
  int[] pars = new int[exp.length()];
  for(int i = 0; i < exp.length(); i++) {
    if(exp.substring(i, i+1).equals("(")) par++;
    if(exp.substring(i, i+1).equals(")")) par--;
    pars[i] = par;
  }
  char[] tmp = exp.toCharArray();
  for(int j = 0; j < tmp.length; j++) {
    if(pars[j] > 0) {
      tmp[j] = '#';
    }
  }
  return new String(tmp);
}
String trimPar(String _exp) {
  String exp = _exp.substring(0);
  int par = 0;
  boolean ok = true;
  for(int i = 1; i < exp.length()-1; i++) {
    if(exp.substring(i, i+1).equals("(")) par++;
    if(exp.substring(i, i+1).equals(")")) par--;
    if(par < 0) ok = false;
  }
  
  while(exp.charAt(0) == '(' && exp.charAt(exp.length()-1) == ')' && ok) {
    //println(exp);
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
  ArrayList<String> tmp = new ArrayList<String>();
  String[] res;
  String[] asplit = split(args, '\"');
  for (int i = 0; i < asplit.length; i += 2) {
    String[] bsplit = split(trim(asplit[i]), ' ');
    for (String s : bsplit) {
      tmp.add(s);
    }
    if (i + 1 < asplit.length) {
      tmp.add(asplit[i + 1]);
    }
  }
  res = new String[tmp.size()];
  for (int i = 0; i < res.length; i++) {
    res[i] = tmp.get(i);
  }

  return res;
}
