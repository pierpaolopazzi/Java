package boundedbuffer;

import java.util.concurrent.Semaphore;

public class BoundedBuffer {
    
    /* definisco la dimensione del buffer */
    private int size; 

    /* definisco il mio buffer: un array di oggetti di tipo Item */
    private Item buffer[];

    /* variabili puntatori */
    private int in, out;
    private int counter;

    /* oggetti semaphore */ 
    private Semaphore full, empty, mutex;

    public BoundedBuffer(int size) {
        this.size = size;
        buffer = new Item[size];
        in = 0;
        out = 0;
        counter = 0;
        full = new Semaphore(size);
        empty = new Semaphore(size);
        mutex = new Semaphore(1);
        try {
            full.acquire(size);
        } catch(InterruptedException e) {
            System.err.println(e);
        }
    }


    public void put(Item item) {
        try {
            empty.acquire();
            mutex.acquire();
        } catch(InterruptedException e) { 
            System.err.println(e);
        }
        buffer[in] = item;
        /* calcolo l'indice per l'array circolare */
        in = (in + 1) % size;
        mutex.release();
        full.release();
    }

    public Item get() {
        try{
            full.acquire();
            mutex.acquire();
        } catch(InterruptedException e) {
            System.err.println(e);
        }
        Item item = buffer[out];
        out = (out + 1) % size;
        mutex.release();
        empty.release();
        return item;
    }

    public int getCount(int counter) {
        return counter; 
    }
}
