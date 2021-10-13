package lib;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

public class Net {
    public static void write(DataOutputStream o, String s) throws Exception {
        ArrayList<String> b = new ArrayList<String>();
        int buf_size = 500;
        int l = s.length();
        int c = 0;
        int temp = 0;
        while (l - buf_size > 0) {
            l = l - buf_size;
            c++;
        }
        for (int i = 0; i < c; i++) {
            b.add(s.substring(temp, temp + buf_size) + "CONTIN");
            temp = temp + buf_size;
        }
        b.add(s.substring(temp));
        for (int i = 0; i < b.size(); i++) {
            o.writeUTF(b.get(i));
        }
    }

    public static String read(DataInputStream s) throws Exception {
        String out = "";
        out += s.readUTF();
        if (out.contains("CONTIN")) {
            while (out.contains("CONTIN")) {
                out = out.replaceAll("CONTIN", "");
                out += s.readUTF();
            }
        }
        return out;
    }
}
