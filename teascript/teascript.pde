Machine m;
int st;
boolean debugMode = true; //false: only shows script errors    true: yells at you more
void setup() {
  st = millis();
  m = new Machine();
  m.init("test.tea");
}
void draw() {
  m.action();
}
void end() {
  test();
  exit();
}
