package teascript;

public class DemoTint extends Tint {
    @Override
    public boolean hasFloatByName(String name) {
        switch (name) {
            case "foo": return true;
            case "boo": return true;
            default: return false;
        }
    }

    @Override
    public boolean hasVoidByName(String name) {
        switch (name) {
            case "loudPrint": return true;
            default: return false;
        }
    }

    @Override
    public float floatByName(String[] info) {
        switch (info[0]) {
            case "foo": return Demo.foo(info[1]);
            case "boo": return Demo.boo(info[1], info[2]);
            default: return -3.14159f;
        }
    }

    @Override
    public void voidByName(String[] info) {
        switch (info[0]) {
            case "loudPrint": Demo.loudPrint(SMan.streval(info[1])); break;
            default: return;
        }
    }
}
