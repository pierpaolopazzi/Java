import java.net.*;

public class UrlDemo {
    public static void main(String[] args) {
        try {
            URL url = new URL("Inserisci qualsiasi Url");

            System.out.println("Protocol: " + url.getProtocol());
            System.out.println("Host name: " + url.getHost());
            System.out.println("Port Number: " + url.getPort());
            System.out.println("Default Port Number: "+url.getDefaultPort());    
            System.out.println("Query String: "+url.getQuery());    
            System.out.println("Path: "+url.getPath());    
            System.out.println("File name: " + url.getFile());
        } catch(Exception e) {
            System.out.println(e);
        }
    }
}
