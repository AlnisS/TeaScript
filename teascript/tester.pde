Table log;
Table val;
PrintWriter testcon;

void test() {
  testing = true;
  logger = createWriter("data/log.unitlog");
  logger.println("id\tval");
  testcon = createWriter("data/vals.unitlog");
  testcon.println("id\tval");
  m = new Machine();
  m.init("units.tea");
  testcon.flush();
  testcon.close();
  m.action();
  logger.flush();
  logger.close();
  println("\n\n");
  run();
}
void run() {
  log = loadTable("log.unitlog", "header, tsv");
  val = loadTable("vals.unitlog", "header, tsv");
  for(TableRow row : val.rows()) {
    String id = row.getString("id");
    String val = row.getString("val");
    TableRow rob = log.findRow(id, "id");
    if(id.charAt(0) == '#') {
      continue;
    }
    if(rob == null) {
      println("no ID found: " + id);
      continue;
    }
    if(rob.getString("val").equals(val)) {
      println(id + "\tpass");
    } else {
      try {
        float a = Float.parseFloat(val);
        float b = Float.parseFloat(rob.getString("val"));
        if(round(a*1000) == round(b*1000)) {
          println(id + "\tpass");
        } else {
          println(id + "\tfail\t" + val + "\t" + rob.getString("val"));
        }
      } catch(Exception e) {
        println(id + "\tfail\t" + val + "\t" + rob.getString("val"));
      }
    }
  }
}
