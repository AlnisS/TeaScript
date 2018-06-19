boolean beval(String exp) {
  exp = trimPar(exp);
  
  String tstr = fstring(exp); //filter characters within parentheses
  int or  = tstr.lastIndexOf("||");
  int and = tstr.lastIndexOf("&&");
  int gtr = tstr.lastIndexOf(">");
  int les = tstr.lastIndexOf("<");
  int gte = tstr.lastIndexOf(">=");
  int lse = tstr.lastIndexOf("<=");
  int neq = tstr.lastIndexOf("!=");
  int equ = tstr.lastIndexOf("==");
  
  if(gtr == gte) gtr = -1; //handle operators which happen to be substrings of other operators
  if(les == lse) les = -1;
  
  if(or != -1)  return beval(exp.substring(0, or)) || beval(exp.substring(or+2));
  if(and != -1) return beval(exp.substring(0, and)) && beval(exp.substring(and+2));
  
  if (neq != -1 && il(neq,equ)) return !equiv(exp.substring(0, neq), exp.substring(neq+2));
  if (equ != -1 && il(equ,neq)) return  equiv(exp.substring(0, equ), exp.substring(equ+2));
  
  if (gtr != -1 && il(gtr,les) && il(gtr,gte) && il(gtr,lse)) return feval(exp.substring(0, gtr)) > feval(exp.substring(gtr+1));
  if (les != -1 && il(les,gtr) && il(les,gte) && il(les,lse)) return feval(exp.substring(0, les)) < feval(exp.substring(les+1));
  if (gte != -1 && il(gte,gtr) && il(gte,les) && il(gte,lse)) return feval(exp.substring(0, gte)) >= feval(exp.substring(gte+2));
  if (lse != -1 && il(lse,gtr) && il(lse,les) && il(lse,gte)) return feval(exp.substring(0, lse)) <= feval(exp.substring(lse+2));
  
  return exp.equals("true");
}

float feval(String exp) {
  exp = trimPar(exp);
  if (m.floats.hasKey(exp)) return m.floats.get(exp);
  
  String tstr = fstring(exp);
  int add = tstr.lastIndexOf("+");
  int sub = tstr.lastIndexOf("-");
  int mul = tstr.lastIndexOf("*");
  int div = tstr.lastIndexOf("/");
  
  if (add != -1 && il(add,sub)) return feval(exp.substring(0, add)) + feval(exp.substring(add+1));
  if (sub != -1 && il(sub,add)) return feval(exp.substring(0, sub)) - feval(exp.substring(sub+1));
  if (mul != -1 && il(mul,div)) return feval(exp.substring(0, mul)) * feval(exp.substring(mul+1));
  if (div != -1 && il(div,mul)) return feval(exp.substring(0, div)) / feval(exp.substring(div+1));

  return Float.parseFloat(exp);
}

String streval(String[] exp, int index) {
  if (exp[index].indexOf("M") != -1) return str(feval(exp[index+1]));
  else if (isBoolean(exp[index]))    return str(beval(exp[index]));
  return exp[index];
}
