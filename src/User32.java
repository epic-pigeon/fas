import com.sun.jna.Native;

public class User32 {
    static {
        Native.register("user32");
    }

    public static native short GetKeyState(int vKey);
}
