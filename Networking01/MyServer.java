import java.io.*;
import java.net.*;

public class MyServer {

    public static void main(String[] args) {
        try {

            // ServerSocket class can be used to create a server socket
            // This object is used to establish communication with the clients
            ServerSocket ss = new ServerSocket(6666);
            
            // Socket class is used to create a socket
            Socket s = ss.accept();

            // getInputStream() returns the InputStream attached with this socket
            DataInputStream dis = new DataInputStream(s.getInputStream());
            
            String str = (String)dis.readUTF();
            System.out.println("message= " + str);
            
            // close the ServerSocket
            ss.close();
        } catch(Exception e) {
            System.out.println(e);
        }
    }
}