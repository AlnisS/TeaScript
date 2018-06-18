boolean beval(String exp) {
  exp = trimPar(exp);
  String tstr = fstring(exp);
  int or  = tstr.lastIndexOf("||");
  int and = tstr.lastIndexOf("&&");
  int gtr = tstr.lastIndexOf(">");
  int les = tstr.lastIndexOf("<");
  int gte = tstr.lastIndexOf(">=");
  int lse = tstr.lastIndexOf("<=");
  int neq = tstr.lastIndexOf("!=");
  int equ = tstr.lastIndexOf("==");
  
  if(gtr == gte) gtr = -1;
  if(les == lse) les = -1;
  
  if(equ != -1 && neq != -1) {
    if(equ < neq) neq = -1;
    else equ = -1;
  }
  
  
  if(or != -1) {
    return beval(exp.substring(0, or)) || beval(exp.substring(or+2));
  }
  if(and != -1) {
    return beval(exp.substring(0, and)) && beval(exp.substring(and+2));
  }
  if (neq != -1 && il(neq,equ)) {
    return !equiv(exp.substring(0, neq), exp.substring(neq+2));
  }
  if (equ != -1 && il(equ,neq)) {
    return equiv(exp.substring(0, equ), exp.substring(equ+2));
  }
  
  if (gtr != -1 && il(gtr,les) && il(gtr,gte) && il(gtr,lse)) {
    return feval(exp.substring(0, gtr)) > feval(exp.substring(gtr+1));
  }
  if (les != -1 && il(les,gtr) && il(les,gte) && il(les,lse)) {
    return feval(exp.substring(0, les)) < feval(exp.substring(les+1));
  }
  if (gte != -1 && il(gte,gtr) && il(gte,les) && il(gte,lse)) {
    return feval(exp.substring(0, gte)) >= feval(exp.substring(gte+2));
  }
  if (lse != -1 && il(lse,gtr) && il(lse,les) && il(lse,gte)) {
    return feval(exp.substring(0, lse)) <= feval(exp.substring(lse+2));
  }
  return exp.equals("true");
}
boolean il(float a, float b) { //index less, like less than but ignores -1 indices
  return a < b || b == -1;
}
boolean equiv(String exp, String expb) {
  if(isBoolean(exp) && isBoolean(expb)) return beval(exp) == beval(expb);
  return feval(exp) == feval(expb);
}
float feval(String exp) {
  exp = trimPar(exp);
  if (m.floats.hasKey(exp)) return m.floats.get(exp);
  
  String tstr = fstring(exp);
  
  int add = tstr.lastIndexOf("+");
  int sub = tstr.lastIndexOf("-");
  int mul = tstr.lastIndexOf("*");
  int div = tstr.lastIndexOf("/");
  
  
  //println(add, sub, mul, div);
  if (add != -1 && il(add,sub)) {
    return feval(exp.substring(0, add)) + feval(exp.substring(add+1));
  }
  if (sub != -1 && il(sub,add)) {
    return feval(exp.substring(0, sub)) - feval(exp.substring(sub+1));
  }
  if (mul != -1 && il(mul,div)) { //if there is multiplication and (it appears before the division or there is no division)
    return feval(exp.substring(0, mul)) * feval(exp.substring(mul+1));
  }
  if (div != -1 && il(div,mul)) {
    return feval(exp.substring(0, div)) / feval(exp.substring(div+1));
  }

  return Float.parseFloat(exp);
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
String streval(String[] exp, int index) {
  String result = "";
  if (exp[index].indexOf("M") != -1) {
    result = str(feval(exp[index+1]));
  } else if (isBoolean(exp[index])) {
    result = str(beval(exp[index]));
  } else {
    result = exp[index];
  }
  return result;
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
