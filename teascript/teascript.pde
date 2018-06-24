Machine m;
int st;
PrintWriter logger;
void setup() {
  st = millis();
  m = new Machine();
  m.init("test.tea");
  logger = createWriter("data/log.txt");
  logger.println("id\tval");
}
void draw() {
  m.next();
}
void end() {
  logger.flush();
  logger.close();
  run();
  exit();
}
