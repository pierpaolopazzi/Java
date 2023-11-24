package mercatosemplice;

public class Produttore implements Runnable {
    private Magazzino magazzino;

    public Produttore(Magazzino magazzino) {
        this.magazzino = magazzino;
        new Thread(this, "Produttore").start();
    }

    @Override
    public void run() {
        for(int i=1; i<=10; i++) {
            magazzino.put();
        }
    }

}
