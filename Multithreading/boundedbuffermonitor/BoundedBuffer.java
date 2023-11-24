package boundedbuffermonitor;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBuffer {
    private int size;  // size = numero di elementi totali che puo' avere un buffer
    private Item buffer[];
    private int in, out, counter; // counter = numero di elementi nel buffer
    private Lock l;
    private Condition bufferPieno;
    private Condition bufferVuoto;

    public BoundedBuffer(int size) {
        this.size = size;
        buffer = new Item[size];
        in = 0;
        out = 0;
        counter = 0;
        l = new ReentrantLock(true); // true = fair
        bufferPieno = l.newCondition();
        bufferVuoto = l.newCondition();
    }

    public void put(Item item) {
        l.lock(); // si prende il lock sul monitor
        try {
            while (counter == size) {
                // se counter = size vuol dire che il buffer e' pieno e mi devo sospendere
                // sulla variabile condizione bufferPieno. Ovvero il produttore si deve sospendere
                bufferPieno.await();
            }
            // altrimenti
            buffer[in] = item;
            in = (in + 1) % size;
            counter++;
            // mi devo preoccupare di risvegliare un consumatore sospesi sulla variabile condizione bufferVuoto
            bufferVuoto.signal();   
        } catch(InterruptedException e) {
            System.err.println(e);
        } finally {
            // mi devo preoccupare di rilasciare il lock
            l.unlock();
        }
    }

    // prelevo un oggetto dal buffer con il metodo get()
    public Item get() {
        Item item = null;
        // acquisisco il lock sul monitor
        l.lock();
        try {
            while(counter == 0) {
                // se counter = 0 allora il buffer e' vuoto e il consumatore si deve sospendere
                bufferVuoto.wait();
            }
            item = buffer[out];
            out = (out + 1) % 2;
            counter--;
            bufferPieno.signal(); // devo risvegliare eventuali produttori
        } catch(InterruptedException e) {
            System.err.println(e);
        } finally {
            l.unlock();
        }
        return item;

    }

}
