Table log;
Table val;
void run() {
  log = loadTable("log.txt", "header, tsv");
  val = loadTable("val.txt", "header, tsv");
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
        if(a - a % .0001 == b - b % .0001) {
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
