package lib;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IO {
    public static String read(String filename) throws Exception {
        if(filename.endsWith(".db")) filename = "data/"+filename;
        return new String(Files.readAllBytes(Paths.get(filename)));
    }

    public static void write(String filename, String content, boolean append) throws Exception {
        if(filename.endsWith(".db")) filename = "data/"+filename;
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));
        out.print(content);
        out.close();
    }
}