public class MyThread1 {
    public static void main(String[] args) {

        // creating an object of the Thread class using the constructor
        Thread t = new Thread("My first Thread");

        // moves the Thread to the active state
        t.start();

        // get the thread name
        String str = t.getName();
        System.out.println(str);
    }
}
