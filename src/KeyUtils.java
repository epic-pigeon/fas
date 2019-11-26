public class KeyUtils {
    public static final int ESCAPE = 27;

    public static boolean isKeyDown(int key) {
        return stateOf(key) < 0;
    }

    public static int stateOf(int key) {
        return User32.GetKeyState(key);
    }
}
