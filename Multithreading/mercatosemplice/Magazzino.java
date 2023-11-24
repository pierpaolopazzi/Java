package mercatosemplice;

public class Magazzino {
    private int numberOfProducts;
    private boolean isEmpty = true;

    public synchronized void put() {
        if(!isEmpty) {
            try {
                wait();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
        numberOfProducts++;
        stampaStatoMagazzino("Prodotto inserito ");
        isEmpty = false;
        notify();
    }

    public synchronized void get() {
        if(isEmpty){
            try {
                wait();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
        numberOfProducts--;
        stampaStatoMagazzino("Prodotto Consumato ");
        isEmpty = true;
        notify();
    }

    private synchronized void stampaStatoMagazzino(String value) {
        System.out.println(value + " - " + numberOfProducts + " Prodotto/i in magazzino");
    }
}
