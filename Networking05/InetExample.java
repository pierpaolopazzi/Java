import java.io.*;
import java.net.*;

public class InetExample {
    
    public static void main(String[] args) {
        try {
            InetAddress ip = InetAddress.getByName("www.github.com");

            System.out.println("Host name: "+ip.getHostName());
            System.out.println("Host address: "+ip.getHostAddress());
        } catch(Exception e) {
            System.out.println(e);
        }
    }
}
