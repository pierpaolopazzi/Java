package mercatosemplice;

public class Consumatore implements Runnable {
    private Magazzino magazzino;
    
    public Consumatore(Magazzino magazzino) {
        this.magazzino = magazzino;
        new Thread(this, "Consumatore").start();
    }

    @Override
    public void run() {
        for(int i=1; i<=10; i++) {
            magazzino.get();
        }
    }
}
