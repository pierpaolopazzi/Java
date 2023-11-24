class Contatore implements Runnable {
    private long conta = 0L;
    private Thread t;

    /**
     * volatile assicura che la variabile venga sempre letta dalla memoria principale e non dalla cache
     * quindi il dato e' sempre aggiornato. Il modificatore volatile inoltre garantisce che gli accessi 
     * in scrittura e lettura sulla variabile siano atomici. Ovvero mentre un thread legge o scrive
     * il valore di una variabile, un altro thread deve aspettare che l'operazione si concluda per poter
     * accedere al valore della variabile.
     */
    private volatile boolean stop = false;

    public Contatore(int priorita) {
        t = new Thread(this);
        t.setPriority(priorita);
    }

    public Contatore() {
        t = new Thread(this);
    }

    public void startThread() {
        t.start();
    }

    public void stopThread() {
        stop = true;
    }

    public long getConta() {
        return conta;
    }

    @Override
    public void run() {
        while(!stop) {
            conta++;
        }
    }
}


public class AvviaThread {
    public static void main(String[] args) {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        Contatore pAlta = new Contatore(Thread.NORM_PRIORITY + 2);
        Contatore pBassa = new Contatore(Thread.NORM_PRIORITY - 2);
        pAlta.startThread();
        pBassa.startThread();
        try {
            Thread.sleep(5000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        pBassa.stopThread();
        pAlta.stopThread();
        System.out.println("Contatore Thread priorita' bassa: " + pBassa.getConta());
        System.out.println("Contatore Thread priorita' alta: " + pAlta.getConta());
        long max = pBassa.getConta() > pAlta.getConta()? pBassa.getConta():pAlta.getConta();
        System.out.println("Valore piu' alto: " + max);
    }    
}
