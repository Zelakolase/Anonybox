package lib;

public class Log {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String CYAN = "\u001B[36m";

    public static void e(String in) {
        System.out.println(RED + "[Error] " + in + RESET);
    }

    public static void s(String in) {
        System.out.println(GREEN + "[Success] " + in + RESET);
    }

    public static void i(String in) {
        System.out.println(CYAN + "[Info] " + in + RESET);
    }
}