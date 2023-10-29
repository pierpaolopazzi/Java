import java.net.*;

public class DSender {
    
    public static void main(String[] args) throws Exception {

        // Connection-less socket for sending and receiving datagram packet
        DatagramSocket ds = new DatagramSocket();
        String str = "Welcome Java";
        InetAddress ip = InetAddress.getByName("127.0.0.1");

        // DatagramPacket is a data container and a message that can be sent or received
        // We specify the ip address and the port number
        DatagramPacket dp = new DatagramPacket(str.getBytes(), str.length(), ip, 3000);
        ds.send(dp);
        ds.close();
    }
}
