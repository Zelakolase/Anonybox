import java.util.Scanner;

import lib.AES;
import lib.IO;
import lib.SparkDB;

public class IDS extends PraudyoTerminal {
    static SparkDB db = new SparkDB();
    static String SK = "";
    public static void main(String[] args) {
        System.out.print("Enter Server key > ");
        Scanner s = new Scanner(System.in);
        SK = s.nextLine();
        try {
            db.readfromString(AES.decrypt(IO.read("INS.db"), SK));
        } catch (Exception e) {
            System.out.println("Server Key Error");
            System.exit(1);
        }
        IDS d = new IDS();
        d.run();
    }  
    @Override
    public String engine(String cmd) {
        String res = "";
        if(cmd.equals("help")) {
            res = "help | help screen\n"+
            "view | view the Internal Domain System database\n"+
            "add <ip> <domain> | Add IP,Domain pair\n"+
            "delete <ip> <domain> | Delete IP,Domain pair";
        }
        else if(cmd.equals("view")) {
            for(int i = 0 ;i<db.num_queries;i++) {
                res += db.getbyindex(i)+"\n";
            }
        }
        else if(cmd.startsWith("add")) {
            String[] args = cmd.split("\\s+");
            db.add(new String[] {args[1],args[2]});
            try {
                IO.write("INS.db", AES.encrypt(db.print(), SK), false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(cmd.startsWith("delete")) {
            String[] args = cmd.split("\\s+");
            db.delete(new String[] {args[1],args[2]});
            try {
                IO.write("INS.db", AES.encrypt(db.print(), SK), false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return res;
    }
}
