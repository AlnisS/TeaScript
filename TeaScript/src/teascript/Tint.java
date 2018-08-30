package teascript;

public abstract class Tint {
    public Tint() {}

    public boolean hasFloatByName(String name) { return false; }
    public boolean hasStringByName(String name) { return false; }
    public boolean hasBooleanByName(String name) { return false; }
    public boolean hasVoidByName(String name) { return false; }

    public float floatByName(String[] info) { return -1; }
    public String stringByName(String[] info) { return null; }
    public boolean booleanByName(String[] info) { return false; }
    public void voidByName(String[] info) { return; }
}
