boolean beval(String exp) {
  exp = trimPar(exp);
  
  String tstr = fstring(exp); //filter characters within parentheses
  int or  = tstr.lastIndexOf("||");
  int and = tstr.lastIndexOf("&&");
  int gtr = noconpos(tstr, ">", ">=", ">=");
  int les = noconpos(tstr, "<", "<=", "<=");
  int gte = tstr.lastIndexOf(">=");
  int lse = tstr.lastIndexOf("<=");
  int neq = tstr.lastIndexOf("!=");
  int equ = tstr.lastIndexOf("==");
  int xor = tstr.lastIndexOf("^");
  int not = tstr.lastIndexOf("!");
  
  int maxmul = max(max(gtr, les), max(gte, lse));
  if(gtr != maxmul) gtr = -1;
  if(les != maxmul) les = -1;
  if(gte != maxmul) gte = -1;
  if(lse != maxmul) lse = -1;
  
  if(or != -1)  return beval(exp.substring(0, or))  || beval(exp.substring(or+2));
  if(and != -1) return beval(exp.substring(0, and)) && beval(exp.substring(and+2));
  if(xor != -1) return beval(exp.substring(0, xor)) ^  beval(exp.substring(xor+1));
  
  if (neq != -1 && neq>equ) return !equiv(exp.substring(0, neq), exp.substring(neq+2));
  if (equ != -1 && equ>neq) return  equiv(exp.substring(0, equ), exp.substring(equ+2));
  
  if (gtr != -1) return feval(exp.substring(0, gtr)) > feval(exp.substring(gtr+1));
  if (les != -1) return feval(exp.substring(0, les)) < feval(exp.substring(les+1));
  if (gte != -1) return feval(exp.substring(0, gte)) >= feval(exp.substring(gte+2));
  if (lse != -1) return feval(exp.substring(0, lse)) <= feval(exp.substring(lse+2));
  
  if(not != -1) return !beval(trim(exp.substring(not+1)));
  
  return trim(exp).equals("true");
}

float feval(String exp) {
  exp = trim(trimPar(trim(exp)));
  if (flookupable(exp)) return flookup(exp);
  
  String tstr = fstring(exp);
  int add = tstr.lastIndexOf("+");
  int sub = notNegative(tstr);
  int mul = noconpos(tstr, "*", "\\*\\*", "**");
  int div = tstr.lastIndexOf("/");
  int rem = tstr.lastIndexOf("%%");
  int mod = noconpos(tstr, "%", "%%", "%%");
  int pow = tstr.lastIndexOf("**");
  
  int maxmul = max(max(mul, div), max(rem, mod));
  if(mul != maxmul) mul = -1;
  if(div != maxmul) div = -1;
  if(rem != maxmul) rem = -1;
  if(mod != maxmul) mod = -1;
  
  if (add != -1 && add>sub) return feval(exp.substring(0, add))  +  feval(exp.substring(add+1));
  if (sub != -1 && sub>add) return feval(exp.substring(0, sub))  -  feval(exp.substring(sub+1));
  if (mul != -1)            return feval(exp.substring(0, mul))  *  feval(exp.substring(mul+1));
  if (div != -1)            return feval(exp.substring(0, div))  /  feval(exp.substring(div+1));
  if (rem != -1)            return feval(exp.substring(0, rem))  %  feval(exp.substring(rem+2));
  if (mod != -1)            return mod(feval(exp.substring(0, mod)),feval(exp.substring(mod+1)));
  if (pow != -1)            return pow(feval(exp.substring(0, pow)),feval(exp.substring(pow+2)));
  
  if(tstr.indexOf("-") != -1) return -feval(exp.substring(tstr.indexOf("-") + 1));
  
  float f = 0;
  try{f = Float.parseFloat(exp);} catch(Exception e) {error("FLOATPARSE", "problem parsing "+exp);}
  return f;
}

float mod(float a, float b) {
  return (a % b + b) % b;
}

boolean flookupable(String exp) {
  for(int i = m.floats.size() - 1; i >= 0; i--) {
    if(m.floats.get(i).hasKey(exp)) return true;
  }
  if(m.functions.containsKey(removeArgs(exp))) return true;
  return isMath(exp);
}

float flookup(String exp) {
  for(int i = m.floats.size() - 1; i >= 0; i--) {
    if(hasVar(exp, i)) return getVar(exp, i);
  }
  if(isMath(exp)) return doMath(exp);
  return m.functions.get(removeArgs(exp)).dup().execute(exp);
}

boolean hasVar(String exp, int level) {
  return m.floats.get(level).hasKey(exp);
}
float getVar(String exp) {
  return getVar(exp, m.floats.size() - 1);
}
float getVar(String exp, int level) {
  float f = 0;
  try{f = m.floats.get(level).get(exp);} catch(Exception e) {error("NOVAR", "no variable "+exp+" found.");}
  return f;
}

boolean isString(String s) {
  return s.indexOf("\"") != -1;
}

String streval(String[] exp, int index) {
  if (isString(exp[index]))  return exp[index].substring(1, exp[index].length()-1);
  if (isBoolean(exp[index])) return str(beval(exp[index]));
  return str(feval(exp[index]));
}
