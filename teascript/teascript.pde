Machine m;
int st;
PrintWriter logger;
void setup() {
  st = millis();
  logger = createWriter("data/log.txt");
  logger.println("id\tval");
  m = new Machine();
  m.init("test.tea");
}
void draw() {
  m.action();
}
void end() {
  logger.flush();
  logger.close();
  run();
  exit();
}
