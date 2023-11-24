package mercatosemplice;


public class MercatoSemplice {
    public static void main(String[] args) {
        Magazzino magazzino = new Magazzino();
        new Produttore(magazzino);
        new Consumatore(magazzino);
    }
}
