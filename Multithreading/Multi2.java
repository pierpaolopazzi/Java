/* Java Thread Example by implementing Runnable interface */

public class Multi2 implements Runnable {
    public void run() {
        System.out.println("Thread is running...");
    }

    public static void main(String[] args){
        Multi2 m2 = new Multi2();
        Thread t2 = new Thread(m2); // Using the constructor Thread(Runnable r)
        t2.start();
    }
}
