package test;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by paulo on 28/08/14.
 */
public class Reader {

    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("/home/paulo/msgs/nike-2531203.txt"));
            String line;
            int seqno = 0;
            while ((line = br.readLine()) != null) {
                if(line.contains("{")) {
                    System.out.println(line.trim());
                    seqno++;
                }
            }
            br.close();
            System.out.println(seqno);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }

    }
}
