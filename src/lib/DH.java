package lib;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.util.Random;

public class DH {
    public static String Server(DataInputStream DIS , DataOutputStream DOS) {
        String secret = "";
        try {
        double sec = Math.abs(new Random().nextInt(10 + 1) + 5); // Secret Num , NOT FOR SHARE
        // START : p and g Agreement
        BigInteger BIp = BigInteger.probablePrime(new Random().nextInt(8 + 1) + 6, new Random());
        int p = (int) Math.abs(BIp.longValue());
        double g = new PRM().getPrimitiveRoots(p).get(0);
        Log.i("P = "+p+" , G = "+g+" , "+"Secret = "+sec);
        DOS.writeUTF(p+","+g);
        // END
        // Send g^sec mod p , also known as pub_1
        //log.i("A Public Key = "+Math.pow(g, sec)%Double.valueOf(p));
        DOS.writeUTF(""+Math.pow(g, sec)%Double.valueOf(p)); // How I convert double datatype to a String , Shhhh.. don't tell anyone
        double pub_2 = Double.parseDouble(DIS.readUTF());
        //log.i("B Public Key = "+pub_2);
        // Calculate key
        secret = ""+Math.pow(pub_2, sec) % Double.valueOf(p);
        }catch(Exception e) {

        }
        return secret;
    }
    public static String Client(DataInputStream DIS , DataOutputStream DOS) {
        String secret = "";
        try {
        double sec = Math.abs(new Random().nextInt(10 + 1) + 5); // Secret Num , NOT FOR SHARE
        Log.i("Secret "+sec);
        // START : p and g Agreement
        String pgs = DIS.readUTF();
        String[] pg = pgs.split(",");
        double p = Double.parseDouble(pg[0]);
        double g = Double.parseDouble(pg[1]);
        // END
        double pub_1 = Double.parseDouble(DIS.readUTF()); // pub_1
        // Send g^sec mod p , also known as pub_2
        DOS.writeUTF(""+Math.pow(g, sec)%p);
        // Calculate key
        secret = ""+Math.pow(pub_1, sec) % p;
        }catch(Exception e) {

        }
        return secret;
    }
}