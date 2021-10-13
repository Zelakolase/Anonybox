import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import lib.AES;
import lib.DH;
import lib.*;

public class TCPInterface {
    public static int max_concurrent_cons = 1024;
	public static int current_cons = 0;
    public static void TCPManager(Server s) {
		try {
			ServerSocket SS = new ServerSocket(1200);
			while (true) {
				if (current_cons <= max_concurrent_cons) {
					new TCPThread(SS.accept(), s).start();
					current_cons++;
				} else {
					boolean is_done = false;
					int tries = 5;
					int counter = 0;
					while (!is_done || counter <= tries) {
						if (current_cons <= max_concurrent_cons) {
							new TCPThread(SS.accept(), s).start();
							current_cons++;
						}
						Thread.sleep(5);
					}
				}
			}
		} catch (Exception e) {
			Log.e("Error Happened while starting the server , please check if port 1200 is available");
		}

	}
    public static class TCPThread extends Thread {
    public Socket s;
    public Server Server;
    TCPThread(Socket s,Server S) {
        this.s = s;
        this.Server = S;
    }
    @Override
    public void run() {
        try {
            DataInputStream DIS = new DataInputStream(s.getInputStream());
            DataOutputStream DOS = new DataOutputStream(s.getOutputStream());
            String secret = "";
            Server.T T = new Server.T();
            while(true) {
                    String cmd = Net.read(DIS);
                    if(cmd.equals("start DH")) {
                        secret = DH.Server(DIS, DOS);
                                        }
                    else {
                        cmd = AES.decrypt(cmd, secret);
                        Net.write(DOS, AES.encrypt(T.redirector(cmd, s.getRemoteSocketAddress()), secret));
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
            current_cons--;
        }
    }
    }
}