import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

public  class PraudyoTerminal {
    public String AppName = "PraudyoTerminal";
    public String AppVersion = "v1.0";
    public String path = "default";

    public void run() {
        Scanner s = new Scanner(System.in);
        System.out.println(cmd("echo -en '\\033c\\033[3J'"));
        System.out.print(cmd("echo -en \"\\033]0;" + AppName + "(" + AppVersion + ")" + "\\a\""));
        System.out.println(AppName + "(" + AppVersion + ") , Powered by PraudyoTerminal");
        while (true) {
            System.out.print(path + " > ");
            System.out.println(engine(s.nextLine()));
        }
    }
    public String engine(String cmd) {
        return "";
    }
    public String cmd(String s) {
        String out = "";
        try {
            Process pr = new ProcessBuilder().command("bash", "-c", s).start();
            BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line = "";
            while ((line = buf.readLine()) != null) {
                out += line + "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }
}