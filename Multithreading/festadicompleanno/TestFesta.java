package festadicompleanno;

import java.util.concurrent.CyclicBarrier;

public class TestFesta {
    public static void main(String[] args) {
        CyclicBarrier luogoDellaFesta = new CyclicBarrier(3, new Runnable(){

            @Override
            public void run() {
                System.out.println("Tutti gli invitati sono arrivati alla festa e possiamo iniziare a festeggiare");
            }
        });
        Festa festa = new Festa(luogoDellaFesta);
        new Thread(festa, "Marco").start();
        new Thread(festa, "Giovanni").start();
        new Thread(festa, "Francesca").start(); 
    }
}
