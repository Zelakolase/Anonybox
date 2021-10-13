
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import lib.*;

public class Server {
    public static String SK = "";
    public static HashMap<String, SparkDB> DB = new HashMap<String, SparkDB>();
    static LocalDateTime LTD = LocalDateTime.now();
    static DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    static String date = LTD.format(DTF);
    /*
     * Stage 1 :Server Key Init
     */
    public String Entry(String cmd) {
        File f = new File("Server.key");
        if (f.exists()) {
            if (cmd.isBlank())
                return "require server key";
            String key = cmd;
            try {
                String actualKey = AES.decrypt(IO.read("Server.key"), key);
                if (actualKey.equals(key)) {
                    SK = key;
                    return ("0");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return ("1");
            }
        } else {
            try {
                f.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                return ("2");
            }
            if (cmd.isBlank())
                return "require new server key";
            String key = cmd;
            try {
                IO.write("Server.key", AES.encrypt(key, key), false);
                SK = key;
                return ("0");
            } catch (Exception e) {
                e.printStackTrace();
                return ("2");
            }
        }
        return "0";
    }

    /*
     * Stage 2 : User Files
     */
    public String userinit() {
        File f = new File("data/user.db");
        String dec_cont = "";
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                return ("2");
            }
            try {
                IO.write("user.db"
                , AES.encrypt("\"id\",\"user\",\"pass\"\n" 
                + "\"0\",\"default\",\"default\"", SK)
                ,false);
            } catch (Exception e) {
                return ("3");
            }
        }
        f = new File("data/default-log.db");
        if(!f.exists()) {
        try {
            IO.write("default-log.db"
            , AES.encrypt("\"TD\",\"event\"\n"
            +"\"0\","+"\"0\"", SK)
            , false);
        } catch (Exception e) {
            return ("3");
        }
    }
    f = new File("data/default-mail.db");
        if(!f.exists()){
        try {
            IO.write("default-mail.db"
            , AES.encrypt("\"id\",\"user\",\"subject\",\"content\"\n"
            +"\"0\","+"\"0\","+"\"0\","+"\"0\"", SK)
            , false);
        } catch (Exception e) {
            return ("3");
        }
    }
        try {
            dec_cont = AES.decrypt(IO.read("user.db"), SK);
        } catch (Exception e) {
            return ("5");
        }
        DB.put("credentials", new SparkDB());
        DB.get("credentials").readfromString(dec_cont);
        f = new File("data/INS.db");
        if(!f.exists()) {
            try {
            IO.write("INS.db", AES.encrypt("\"ip\",\"name\"\n\"127.0.0.1\",\"local\"", SK), false);
            }catch(Exception e) {
                return "3";
            }
        }
        return "done";
    }

    /*
     * Stage 3 : Core
     */
    public static class T {
        String current_user = "";
        boolean is_logged_in = false;
        public String redirector(String cmd, SocketAddress socketAddress) throws Exception {
            String res = "";
            if (cmd.equals("test")) {
                res = test(); // No args required
            }
             else if (cmd.startsWith("login")) {
                res = login(cmd+","+socketAddress); // login,<user>,<pass>,<ip>
            }
             else if (cmd.startsWith("register")) {
                res = register(cmd); // register,<user>,<pass>
            }
            else if (cmd.startsWith("mail")) {
                if(is_logged_in) {
                res = mail(cmd+","+socketAddress); // mail,<F <num>|L <num>|A>,<ip>
                }else {
                    res = "You're not logged in";
                }
            }
             else if (cmd.startsWith("log")) {
                if(is_logged_in){
                res = log();
                }else {
                    res = "You're not logged in";
                }
            }
            else if(cmd.startsWith("recieve")) {
                res = recieve(cmd+","+socketAddress);
            }
            else if(cmd.startsWith("send")) {
                if(is_logged_in) {
                res = send_mail(cmd); // send,<dest_user>@<dest_ip>,<subject>,<content>
                }else {
                    res = "You're not logged in";
                }
            }
             else {
                res = "Unknown";
            }
            return res;
        }
        public String recieve(String cmd) { // recieve,src_user,dest_user,subject,content,ip
            String res = "";
            String[] args = cmd.split(",");
            SparkDB temp_db = new SparkDB();
            try {
                temp_db.readfromString(AES.decrypt(IO.read(args[2]+"-mail.db"), SK));
            } catch (Exception e) {
                res = "Server Error";
            }
            temp_db.add(new String[] {""+temp_db.num_queries,args[1]+"@"+args[5],args[3],args[4]});
            try {
                IO.write(args[2]+"-mail.db", AES.encrypt(temp_db.print(), SK), false);
                res = "done";
            } catch (Exception e) {
                res = "Server Error";
            }
            return res;
        }
        public String test() {
            return "done";
        }
        public String login(String cmd) {
            String res = "";
            String[] args = cmd.split(","); // login,user,pass,ip
            if(DB.get("credentials").Mapper.get("user").contains(args[1])) {
                // User exists
                if(DB.get("credentials").get("user", args[1], "pass").equals(args[2])) {
                    // Password is true
                    current_user = args[1];
                    is_logged_in = true;
                    DB.put(current_user+"-log", new SparkDB());
                    try {
                        DB.get(current_user+"-log").readfromString(AES.decrypt(IO.read(current_user+"-log.db"), SK));
                    } catch (Exception e) {
                        res = "Server Error";
                    }
                    time_update();
                    DB.get(current_user+"-log").add(new String[] {date,args[3]+" has logged in"});
                    if(DB.get(current_user+"-log").Mapper.get("TD").contains("0")) {
                        DB.get(current_user+"-log").delete(new String[] {"0","0"});
                    }
                    try {
                        IO.write(current_user+"-log.db", AES.encrypt(DB.get(current_user+"-log").print(), SK), false);
                        res = "Welcome "+current_user;
                    } catch (Exception e) {
                        res = "Server Error";
                    }
                }
                else {
                    res = "Password is incorrect";
                }
            }else {
                res = "User doesn't exist";
            }
            return res;
        }
        
        public String register(String cmd) {
            String res = "";
            String[] args = cmd.split(","); // register,user,pass
            if(DB.get("credentials").Mapper.get("user").contains(args[1])) {
                res = "Username already exists";
            }else {
                args[1].replaceAll("@", "").replaceAll(",", "").replaceAll(":", "");
                DB.get("credentials").add(new String[] {""+DB.get("credentials").num_queries,args[1],args[2]});
                try {
                    IO.write("user.db", AES.encrypt(DB.get("credentials").print(), SK), false);
                    IO.write(args[1]+"-log.db", AES.encrypt("\"TD\",\"event\"\n"+"\"0\","+"\"0\"", SK), false);
                    IO.write(args[1]+"-mail.db", AES.encrypt("\"id\",\"user\",\"subject\",\"content\"\n"+"\"0\","+"\"0\","+"\"0\","+"\"0\"", SK), false);
                    res = "Registered , you can login";
                } catch (Exception e) {
                    res = "Server Error";
                    e.printStackTrace();
                }
            }
            return res;
        }
        public String log() {
            String res = "";
            try {
                DB.get(current_user+"-log").readfromString(AES.decrypt(IO.read(current_user+"-log.db"), SK));
            } catch (Exception e) {
                // Fake Catch
            }
            for(int i = 0;i<DB.get(current_user+"-log").num_queries;i++) {
                res += DB.get(current_user+"-log").getbyindex(i)+"\n";
            }
            return res;
        }
        public String send_mail(String cmd) {  // send,user@domain/ip,subject,content
            String res = "";
            String[] args = cmd.split(",");
            String[] user_domain = args[1].split("@");
            try {
                String[] ip_s = toIP(user_domain[1]).split(",");
                for(int i = 0;i<ip_s.length;i++) {
                    String current_ip = ip_s[i];

                    Socket s = new Socket(current_ip,1200);
                    DataInputStream tempDIS = new DataInputStream(s.getInputStream());
                    DataOutputStream tempDOS = new DataOutputStream(s.getOutputStream());
                    boolean is_secure = false;
                    String tempsecret = "";
                    while(!is_secure) {
                    Net.write(tempDOS, "start DH");
                    tempsecret = DH.Client(tempDIS, tempDOS);
                    Net.write(tempDOS, AES.encrypt("test", tempsecret));
				    String ress = Net.read(tempDIS);
                    if (ress.equals("Unknown")) {

                    } else {
                        if (AES.decrypt(ress, tempsecret).equals("done")) {
                            is_secure = true;
                        }
                    }
                    }
                    Net.write(tempDOS, AES.encrypt("recieve,"+current_user+","+user_domain[0]+","+
                    args[2]+","+args[3], tempsecret));
                    if(AES.decrypt(Net.read(tempDIS), tempsecret).equals("done")) {
                        res = "done";
                    }else {
                        res = "failed";
                    }
                    s.close();
                }
            } catch (Exception e) {
                res = "Invalid Domain";
            }
            return res;
        }

        public String mail(String cmd) { // mail,Ln/Fn/A,ip
            String res = "";
            String[] args = cmd.split(",");
            SparkDB db = new SparkDB();
            try {
                db.readfromString(AES.decrypt(IO.read(current_user+"-mail.db"), SK));
            } catch (Exception e) {
                res = "Server Error";
            }
            if(db.Mapper.get("user").contains("0")) {
                db.delete(new String[] {"0","0","0","0"});
            }
            if(args[1].equals("A")) {
                for(int i = 0;i<db.num_queries;i++) {
                    res += db.getbyindex(i)+"\n";
                }
            }
            else if(args[1].startsWith("F")) {
                int to_i = Integer.parseInt(args[1].replaceFirst("F", ""));
                if(db.num_queries-to_i>0) {
                    for(int i = 0;i<to_i;i++) {
                        res += db.getbyindex(i)+"\n";
                    }
                }else {
                    res = "Out of range";
                }
            }
            else if(args[1].startsWith("L")) {
                int to_i = Integer.parseInt(args[1].replaceFirst("L", ""));
                if(db.num_queries-to_i>0) {
                    for(int i = 1;i<to_i+1;i++) {
                        res += db.getbyindex(db.num_queries-i)+"\n";
                    }
                }else {
                    res = "Out of range";
                }
            }
            SparkDB db_0 = new SparkDB();
            try {
                db_0.readfromString(AES.decrypt(IO.read(current_user+"-log.db"), SK));
            } catch (Exception e) {
                res = "Server Error";
            }
            time_update();
            db_0.add(new String[] {date,args[2]+" has viewed your mail"});
            try {
                IO.write(current_user+"-log.db", AES.encrypt(db_0.print(), SK), false);
            } catch (Exception e) {
                res = "Server Error";
            }
            return res;
        }
        public String toIP(String domain) throws Exception {
            String res = "";
            if(domain.matches("^.[0-9]{1,3}/..[0-9]{1,3}/..[0-9]{1,3}/..[0-9]{1,3}")) {
                res = domain;
            }else if(domain.contains(".")) {
                res = java.net.InetAddress.getByName(domain).getHostAddress();
            }else {
                SparkDB INS = new SparkDB();
                INS.readfromString(AES.decrypt(IO.read("INS.db"), SK));
                res = INS.multiget("name", domain, "ip");
            }
            return res;
        }
        public static void time_update() {
            LTD = LocalDateTime.now();
            date = LTD.format(DTF);
        }
    }
}
