

class Saluti extends Thread {

    public Saluti(String name) {
        super(name);
    }

    public void run(){
        for(int i=0; i<=7; i++) {
            System.out.println("Ciao da "+getName());
        }
    }
}
 
public class ThreadTest {
    public static void main(String[] args){

        Saluti t1 = new Saluti("Primo Thread");
        Saluti t2 = new Saluti("Secondo Thread");
        t1.start();
        t2.start();
    } 
}
