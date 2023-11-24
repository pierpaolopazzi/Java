package boundedbuffer;

public class Consumer extends Thread{
    private int id;
    private BoundedBuffer b;
    
    public Consumer(int id, BoundedBuffer b) {
        this.id = id;
        this.b = b;
    }

    public void run() {
        while(true) {
            // scelta di un tempo di attesa casuale (tra 500 e 1499 ms)
            long waitingTime = (long)(Math.random()*1000+300);
            try {
                sleep(waitingTime); // attesa
            } catch(InterruptedException e) {
                System.err.println(e);
            }
            System.out.println("Consumatore " + id + " vuole prelevare un item");
            Item item = b.get();
            System.out.println("Consumatore " + id + " ha prelevato: " + item.getValue());
        }
    }
}
