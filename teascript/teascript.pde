Machine m;
int st;
PrintWriter logger;
boolean testing = false;
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
