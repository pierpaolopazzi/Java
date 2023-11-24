package boundedbuffermonitor;

public class Producer extends Thread{
    private int id;
    private BoundedBuffer b;
    
    public Producer(int id, BoundedBuffer b) {
        this.id = id;
        this.b = b;
    }

    public void run() {
        while(true) {
            // scelta di un tempo di attesa casuale (tra 500 e 1499 ms)
            long waitingTime = (long)(Math.random()*1000+500);
            try {
                sleep(waitingTime); // attesa
            } catch(InterruptedException e) {
                System.err.println(e);
            }
            // scelta di un valore casuale per l'item
            int itemValue = (int) (Math.random()*1000);
            Item item = new Item(itemValue);
            System.out.println("Produttore " + id + " vuole inserire: " + item.getValue());
            b.put(item);
            System.out.println("Produttore " + id + " ha inserito: " + item.getValue());
        }
    }
    
}
