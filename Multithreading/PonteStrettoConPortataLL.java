import java.util.concurrent.*;
import java.util.*;


class TipoVeicolo {
    public static final int AUTOMOBILE = 0;
    public static final int CAMION = 1;
}

class Direzione {
    public static final int NORD = 0;
    public static final int SUD = 1;
}

class PesoVeicolo {
    public static final int AUTOMOBILE = 2;
    public static final int CAMION = 6;

    public static int getPeso(int tipo) {
        switch(tipo){
            case TipoVeicolo.AUTOMOBILE:
                return AUTOMOBILE;
            case TipoVeicolo.CAMION:
                return CAMION;
            default:
                return AUTOMOBILE;
        }
    }
}

public class PonteStrettoConPortataLL {
    
    public static void main(String[] args) {

        int numAuto, numCamion, nVeicoli, i;
        Ponte ponteStretto = new Ponte();

        try {
            numAuto = Integer.parseInt(args[0]);
            numCamion = Integer.parseInt(args[1]);
        } catch(Exception e) {
            System.out.println("Argomenti sbagliati. Utilizzo default: ");
            numAuto = 100;
            numCamion = 100;
        }

        System.out.println("Inizio ponte con portata: ");
        System.out.println("Numero automobili: " + numAuto);
        System.out.println("Numero camion: " + numCamion);

        nVeicoli = numAuto * 2 + numCamion * 2;

        Veicolo VeicoloVett [] = new Veicolo[nVeicoli];

        for(i = 0; i < numAuto * 2; i+=2) {
            VeicoloVett[i] = new Veicolo(ponteStretto, Direzione.NORD, TipoVeicolo.AUTOMOBILE);
            VeicoloVett[i+1] = new Veicolo(ponteStretto, Direzione.SUD, TipoVeicolo.AUTOMOBILE);
        }
        for(i = numAuto*2; i<numAuto*2+numCamion*2; i+=2 ) {
            VeicoloVett[i] = new Veicolo(ponteStretto, Direzione.NORD, TipoVeicolo.CAMION);
            VeicoloVett[i+1] =  new Veicolo(ponteStretto, Direzione.SUD, TipoVeicolo.CAMION);
        }
    }
}


class Veicolo implements Runnable {
    Ponte ponte;
    int direzioneVeicolo;
    int tipoVeicolo;
    Thread VeicoloThread;
    int sleepD=2000;

    public Veicolo(Ponte p, int dir, int tipo) {
        ponte = p;
        direzioneVeicolo = dir;
        tipoVeicolo = tipo;
        VeicoloThread = new Thread(this);
        VeicoloThread.start();
    }

    public void run() {

        try {
            Thread.sleep(sleepD);
        } catch(InterruptedException e){
            System.err.println(e);
        }

        try{
            ponte.richiestaAccesso(direzioneVeicolo, tipoVeicolo);
        } catch(InterruptedException e ) {
            System.err.println(e);
        }

        try{
            Thread.sleep(sleepD);
        } catch(InterruptedException e) {
            System.err.println(e);
        }

        try{
            ponte.uscita(direzioneVeicolo, tipoVeicolo);
        } catch(InterruptedException e ){
            System.err.println(e);
        }
    }
}

class Ponte {
    int inAttesa[][];
    int inTransito[][];
    int caricoAttuale;
    final int PORTATA = 60;

    public Ponte() {
        inAttesa = new int[2][2];
        inTransito = new int[2][2];

        for(int i=0; i<2; i++) {
            inAttesa[Direzione.NORD][i] = 0;
            inAttesa[Direzione.SUD][i] = 0;
            inTransito[Direzione.NORD][i] = 0;
            inTransito[Direzione.SUD][i] = 0; 
        }
        caricoAttuale = 0;
    }

    public synchronized void richiestaAccesso(int dir, int tipo) throws InterruptedException {
        while(Condizione(dir, tipo)) {
            inAttesa[dir][tipo]++;
            wait();
            inAttesa[dir][tipo]--;
        }
        inTransito[dir][tipo]++;
        caricoAttuale += PesoVeicolo.getPeso(tipo);
    }

    public synchronized void uscita(int dir, int tipo) throws InterruptedException {
        show();
        inTransito[dir][tipo]--;
        int caricoTemp = caricoAttuale;
        caricoAttuale -= PesoVeicolo.getPeso(tipo);
        if(caricoTemp + PesoVeicolo.CAMION >= PORTATA) { // PONTE PIENO
            System.out.println("\n ------- USCITA MAX PORTATA ------- \n");
            notifyAll();
        }
        if(inTransito[dir][tipo] == 0) 
            notifyAll();
    }

    public void show() {
        System.out.println(" ------ Carico ponte attuale: " + caricoAttuale);
        System.out.println("In transito verso NORD (A - C): " + 
                                        inTransito[Direzione.NORD][TipoVeicolo.AUTOMOBILE] + " - " +
                                        inTransito[Direzione.NORD][TipoVeicolo.CAMION] + ".  ");
        System.out.println("In transito verso SUD (A - C): " +
                                        inTransito[Direzione.SUD][TipoVeicolo.AUTOMOBILE] + " - " +
                                        inTransito[Direzione.SUD][TipoVeicolo.CAMION] + ".  "); 

    }

    public boolean Condizione(int dir, int tipo) {
        int otherdir;
        if(dir == Direzione.NORD) {
            otherdir = Direzione.SUD;
        } else {
            otherdir = Direzione.NORD;
        }

        if(
            // SAFATY
            (tipo == TipoVeicolo.AUTOMOBILE && inTransito[otherdir][TipoVeicolo.CAMION]>0) ||
            (tipo == TipoVeicolo.CAMION && inTransito[otherdir][TipoVeicolo.AUTOMOBILE]>0) ||
            (tipo == TipoVeicolo.CAMION && inTransito[otherdir][TipoVeicolo.CAMION]>0) ||

            // PORTATA
            (tipo == TipoVeicolo.AUTOMOBILE && (caricoAttuale + PesoVeicolo.AUTOMOBILE > PORTATA)) ||
            (tipo == TipoVeicolo.CAMION && (caricoAttuale + PesoVeicolo.CAMION > PORTATA)) ||

            // POLITICHE DI PRECEDENZA
            (tipo == TipoVeicolo.CAMION && inAttesa[otherdir][TipoVeicolo.AUTOMOBILE]>0) ||
            (tipo == TipoVeicolo.CAMION && inAttesa[dir][TipoVeicolo.AUTOMOBILE]>0)
        ) return true;
        return false;
    }
    
}