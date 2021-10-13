import java.util.Scanner;

import lib.Log;

public class main {
	public static void main(String[] args) throws Exception {
		MemManage M = new MemManage();
			M.start();
			System.out.print("1:Server , 2:Client , Choose: ");
			Scanner s = new Scanner(System.in);
			String in = s.nextLine();
			if (in.equals("1")) {
				Server server = new Server();
				String new_or_old = server.Entry("");
				if(new_or_old.equals("require server key")) {
					System.out.print("Enter the server key : ");
					String res = server.Entry(s.nextLine());
					if(res.equals("1")) {
						Log.e("Incorrect key");
						System.exit(1);
					}
				}else if(new_or_old.equals("require new server key")) {
					System.out.print("Enter the new server key : ");
					String res = server.Entry(s.nextLine());
					if(res.equals("2")) {
						 Log.e("Cannot create 'Server.key' , Check your permissions.");
						 System.exit(1);
				}
				}
				Log.s("Passed Server Key check");
				String pot_err = server.userinit();
				if(pot_err.equals("5")) {
					Log.e("Cannot Decrypt the credentials file , Two common ways to fix it\n"
                    + "1. Delete 'Server.key', then make it with a different password.\n"
                    + "2. Delete 'user.db', DATA LOSS IS EXPECTED.");
					System.exit(1);
				}
				if(pot_err.equals("3")||pot_err.equals("2")) {
					Log.e("Cannot write files , Check these two things\n"
					+ "1. Check your permissions , try to run with root\n"
					+ "2. Create data/ directory , if it doesn't exist.");
					System.exit(1);
				}
				Log.s("Passed Files check");
				new TCPInterface();
				TCPInterface.TCPManager(server);
			}
			if (in.equals("2")) {
				Client.Entry();
		}
	}

	public static class MemManage extends Thread {
		public void run() {
			while (true) {
				double usage = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000;
				if (usage >= 10.0) {
					System.gc();
				}
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
