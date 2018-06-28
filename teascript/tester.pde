ArrayList<String> failed;
void test() {
  failed = new ArrayList<String>();
  m = new Machine();
  m.init("units.tea");
  m.action();
  println("\n\n");
  if(failed.size() != 0) {
    println("failed tests:");
    for(String s : failed) {
      println(s);
    }
  } else println("all tests passed!");
}
void prettyUnitPass(String id, String a, String b) {
  if(unitPass(a, b)) {
      println(id + "\tpass");
    } else {
      println(id + "\tfail\t" + a + "\t" + b);
      failed.add(id + "\tfail\t" + a + "\t" + b);
    }
}
boolean unitPass(String a, String b) {
  if(a.equals(b)) {
    return true;
  }
  try {
    float fa = Float.parseFloat(a);
    float fb = Float.parseFloat(b);
    if(round(fa*1000) == round(fb*1000)) {
      return true;
    } else {
      return false;
    }
  } catch(Exception e) {
    return false;
  }
}
