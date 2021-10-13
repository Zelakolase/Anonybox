import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import lib.*;

public class Client extends PraudyoTerminal {
	public static final String SPT = "\033[0;0m"; // Set Plain Text
	public static final String SBT = "\033[0;1m"; // Set Bold Text
	public static final String SBlT = "\033[0;5m"; // Set Blinking Text
	public static Socket s;
	public static DataInputStream DIS;
	public static DataOutputStream DOS;
	public static String secret = "";
	public static String help = SBT + "connect <ip>" + SPT + " | Connect to IP\n" + SBT + "clear" + SPT
			+ " | Clear the screen\n" + SBT + "exit" + SPT + " | Exit the terminal";

	public static void Entry()  {
		Client c = new Client();
		c.AppName = "Anonybox Client";
		c.run();
	}

	@Override
	public String engine(String cmd)   {
		try {
		String res = "";
		if (cmd.equals("help")) {
			res = help;
		}
		if (cmd.equals("clear"))
			res = cmd("echo -en '\\033c\\033[3J'");
		if (cmd.equals("exit"))
			System.exit(0);
		if (cmd.startsWith("connect")) {
			s = new Socket(cmd.split(" ")[1], 1200);
			DIS = new DataInputStream(s.getInputStream());
			DOS = new DataOutputStream(s.getOutputStream());
			boolean is_secure = false;
			while (!is_secure) {
				Net.write(DOS, "start DH");
				secret = DH.Client(DIS, DOS);
				Net.write(DOS, AES.encrypt("test", secret));
				String ress = Net.read(DIS);
				if (ress.equals("Unknown")) {

				} else {
					if (AES.decrypt(ress, secret).equals("done")) {
						res = SBlT + "Secure Connection Established" + SPT;
						path = "" + s.getRemoteSocketAddress();
						is_secure = true;
						help += "\n" + SBT + "login <user> <pass>" + SPT + " | Login with username and password\n" + SBT
								+ "register <user> <pass>" + SPT + " | Register with username and password\n" + SBT
								+ "mail <F|L|A <number>>" + SPT
								+ " | Get (F)irst|(L)ast|(A)ll, <number> applies on First and Last\n" + SBT
								+ "log" + SPT
								+ " | View log\n" + SBT
								+ "send <dest> <subject> <content>" + SPT
								+ " | Send Mail to <dest>";
					}
				}
			}
		}
		if (cmd.startsWith("login")) {
			String[] args = cmd.split("\\s+");
			Net.write(DOS, AES.encrypt("login," + args[1] + "," + args[2], secret));
			res = AES.decrypt(Net.read(DIS), secret);
		}
		if (cmd.startsWith("register")) {
			String[] args = cmd.split("\\s+");
			Net.write(DOS, AES.encrypt("register," + args[1] + "," + args[2], secret));
			res = AES.decrypt(Net.read(DIS), secret);
		}
		if (cmd.startsWith("mail")) {
			String[] args = cmd.split("\\s+");
			Net.write(DOS, AES.encrypt("mail," + args[1], secret));
			res = AES.decrypt(Net.read(DIS), secret);
		}
		if(cmd.equals("log")) {
			Net.write(DOS, AES.encrypt("log", secret));
			res = AES.decrypt(Net.read(DIS), secret);
		}
		if(cmd.startsWith("send")) {
			String[] args = cmd.split("\\s+");
			Net.write(DOS, AES.encrypt("send," + args[1]+","+args[2]+","+args[3], secret));
			res = AES.decrypt(Net.read(DIS), secret);
		}
		return res;
	}catch(Exception e) {

	}
	return "Err";
	}
}