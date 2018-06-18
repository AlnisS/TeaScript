Machine m;
int st;
void setup() {
  st = millis();
  m = new Machine();
  m.init("test.tea");
  //String[] tmp = {"This is a \"test of\" the system"};
  //for(String s: tmp) {
  //  printArray(isplit(s));
  //}
  //noLoop();
}
void draw() {
  //for(int i = 0; i < 1000; i++)
  m.next();
  //for(int i = 0; i < 256; i++) {
  //  println("L " + char(i));
  //}
  //printArray(isplit("this is \"a test\" of the system"));
}
