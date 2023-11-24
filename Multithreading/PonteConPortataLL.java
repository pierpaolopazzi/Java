import java.util.*;

import javax.imageio.plugins.tiff.TIFFDirectory;

class TipoVeicolo {
    public static final int BUS = 0;
    public static final int AUTO = 1;
    public static final int CAMION = 2;
}

class Direzione {
    public static final int NORD = 0;
    public static final int SUD = 1;
}



public class PonteConPortataLL {
    public static void main(String[] args) {

        int nBus, nAuto, nCamion, numVeicoli, i;
        Ponte ponteStretto = new Ponte();

        try {
            nBus = Integer.parseInt(args[0]);
            nAuto = Integer.parseInt(args[1]);
            nCamion = Integer.parseInt(args[2]);
        } catch(Exception e) {
            System.err.println("Utilizzo valori di default: ...\n");
            nBus = 20;
            nAuto = 50;
            nCamion = 15;
        }

        numVeicoli = nBus*2 + nAuto*2 + nCamion*2;

        Veicolo veicoloVett [] = new Veicolo[numVeicoli];

        for(i = 0; i<nBus*2; i+=2) {
            veicoloVett[i] = new Veicolo(ponteStretto, Direzione.NORD, TipoVeicolo.BUS);
            veicoloVett[i+1] = new Veicolo(ponteStretto, Direzione.SUD, TipoVeicolo.BUS);
        }
        for(i = nBus*2; i< nBus*2 + nAuto*2; i+=2) {
            veicoloVett[i] = new Veicolo(ponteStretto, Direzione.NORD, TipoVeicolo.AUTO);
            veicoloVett[i+1] = new Veicolo(ponteStretto, Direzione.SUD, TipoVeicolo.AUTO);
        }
        for(i = nBus*2 + nAuto*2; i < nBus*2 + nAuto*2 + nCamion*2; i+=2) {
            veicoloVett[i] = new Veicolo(ponteStretto, Direzione.NORD, TipoVeicolo.CAMION);
            veicoloVett[i+1] = new Veicolo(ponteStretto, Direzione.SUD, TipoVeicolo.CAMION);
        }

        System.out.println("\nTutti i task sono terminati\n");
    }
}

class Veicolo implements Runnable {
    Ponte ponte;
    int dirVeicolo;
    int tipoVeicolo;
    Thread veicoloThread;

    public Veicolo(Ponte p, int dir, int tipo) {
        ponte = p;
        dirVeicolo = dir;
        tipoVeicolo = tipo;
        veicoloThread = new Thread(this);
        veicoloThread.start();
    }

    public void run() {

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {}

        try {
            ponte.richiestaAccesso(dirVeicolo, tipoVeicolo);
        } catch(InterruptedException er) {}

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {}

        try {
            ponte.richiestaUscita(dirVeicolo, tipoVeicolo);
        } catch(InterruptedException e) {}
    }
}

class Ponte {
    private int inTransito[][];
    private int inAttesa[][];
    private int caricoAttuale;
    private final int PORTATA = 20;

    public Ponte() {
        inTransito = new int[2][3];
        inAttesa = new int[2][3];
        caricoAttuale = 0;

        for(int i=0; i<3; i++) {
            inTransito[Direzione.NORD][i] = 0;
            inTransito[Direzione.SUD][i] = 0;
            inAttesa[Direzione.NORD][i] = 0;
            inAttesa[Direzione.SUD][i] = 0;
        }
    }

    public synchronized void richiestaAccesso(int dir, int tipo) throws InterruptedException {
        while(Condizione(dir, tipo)){
            inAttesa[dir][tipo]++;
            wait();
            inAttesa[dir][tipo]--;
        }
        inTransito[dir][tipo]++;
        caricoAttuale++;
    }

    public synchronized void richiestaUscita(int dir, int tipo) throws InterruptedException {
        show();
        inTransito[dir][tipo]--;
        int caricoTemp = caricoAttuale;
        caricoAttuale--;

        if(caricoTemp+1>=PORTATA){
            System.out.println("\n[ ***** USCITA MAX PORTATA ***** ]\n");
            notifyAll();
        } else if(caricoAttuale==0) {
            notifyAll();
        } else notifyAll();

    }

    private void show() {
        System.out.println("\nCarico attuale: " + caricoAttuale);
        System.out.print("In TRANSITO verso NORD (BUS - AUTO - CAMION): " +
                                        inTransito[Direzione.NORD][TipoVeicolo.BUS] + " - " +
                                        inTransito[Direzione.NORD][TipoVeicolo.AUTO] + " - " +
                                        inTransito[Direzione.NORD][TipoVeicolo.CAMION] + ". - ");
        System.out.print("In ATTESA verso NORD (BUS - AUTO - CAMION): " +
                                        inAttesa[Direzione.NORD][TipoVeicolo.BUS] + " - " +
                                        inAttesa[Direzione.NORD][TipoVeicolo.AUTO] + " - " +
                                        inAttesa[Direzione.NORD][TipoVeicolo.CAMION] + ".\n" );
        System.out.print("In TRANSITO verso SUD (BUS - AUTO - CAMION): " +
                                        inTransito[Direzione.SUD][TipoVeicolo.BUS] + " - " +
                                        inTransito[Direzione.SUD][TipoVeicolo.AUTO] + " - " +
                                        inTransito[Direzione.SUD][TipoVeicolo.CAMION] + ". - ");
        System.out.print("In ATTESA verso SUD (BUS - AUTO - CAMION): " +
                                        inAttesa[Direzione.SUD][TipoVeicolo.BUS] + " - " +
                                        inAttesa[Direzione.SUD][TipoVeicolo.AUTO] + " - " +
                                        inAttesa[Direzione.SUD][TipoVeicolo.CAMION] + ".\n" );
    }

    private boolean Condizione(int dir, int tipo) {
        int otherdir;
        if(dir == Direzione.NORD) {
            otherdir = Direzione.SUD;
        } else {
            otherdir = Direzione.NORD;
        }


        if(
            // SAFATY 
            (tipo == TipoVeicolo.BUS && inTransito[otherdir][TipoVeicolo.AUTO]>0) ||
            (tipo == TipoVeicolo.BUS && inTransito[otherdir][TipoVeicolo.BUS]>0) ||
            (tipo == TipoVeicolo.BUS && inTransito[otherdir][TipoVeicolo.CAMION]>0) ||
            (tipo == TipoVeicolo.BUS && inTransito[dir][TipoVeicolo.CAMION]>0) ||
            (tipo == TipoVeicolo.AUTO && inTransito[otherdir][TipoVeicolo.CAMION]>0) ||
            (tipo == TipoVeicolo.AUTO && inTransito[otherdir][TipoVeicolo.BUS]>0) ||
            (tipo == TipoVeicolo.AUTO && inTransito[dir][TipoVeicolo.CAMION]>0) ||
            (tipo == TipoVeicolo.CAMION && inTransito[otherdir][TipoVeicolo.AUTO]>0) ||
            (tipo == TipoVeicolo.CAMION && inTransito[otherdir][TipoVeicolo.BUS]>0) ||
            (tipo == TipoVeicolo.CAMION && inTransito[dir][TipoVeicolo.AUTO]>0) ||
            (tipo == TipoVeicolo.CAMION && inTransito[dir][TipoVeicolo.BUS]>0) ||
            (tipo == TipoVeicolo.CAMION && inTransito[dir][TipoVeicolo.CAMION]>0) ||
            
            // PORTATA
            (caricoAttuale+1 > PORTATA) ||

            // POLITICHE DI PRECEDENZA
            (tipo == TipoVeicolo.AUTO && inAttesa[otherdir][TipoVeicolo.BUS]>0) ||
            (tipo == TipoVeicolo.AUTO && inAttesa[dir][TipoVeicolo.BUS]>0) ||
            (tipo == TipoVeicolo.CAMION && inAttesa[otherdir][TipoVeicolo.AUTO]>0) ||
            (tipo == TipoVeicolo.CAMION && inAttesa[dir][TipoVeicolo.AUTO]>0)

        ) return true;
        else return false;
    }
}
