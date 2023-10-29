import java.io.*;    
import java.net.*;  

public class UrlConnectionExample {    
   
    public static void main(String[] args) {
        try {
            URL url = new URL("Mettere un sito a vostra scelta");
            
            // HttpURLConnection works for HTTP protocol only
            HttpURLConnection huc = (HttpURLConnection)url.openConnection();
            for(int i = 1; i <= 100; i++) {
                
                // It returns the key for the url header file and the value for the url file 
                System.out.println(huc.getHeaderFieldKey(i)+"=" + huc.getHeaderField(i));
            }
            huc.disconnect();
        } catch(Exception e) {
            System.out.println(e);
        }
    }
}  
