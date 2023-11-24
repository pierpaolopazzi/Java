package boundedbuffermonitor;

public class BoundedBufferMain {
    public static void main(String[] args) {
        int dimBuffer = 10;
        int numProducers = 10;
        int numConsumers = 10;
        BoundedBuffer b = new BoundedBuffer(dimBuffer);
        for(int i = 0; i< numProducers; i++) {
            Producer p = new Producer(i, b);
            p.start();
        }
        for(int i = 0; i<numConsumers; i++) {
            Consumer c = new Consumer(i, b);
            c.start();
        }
    }
}
