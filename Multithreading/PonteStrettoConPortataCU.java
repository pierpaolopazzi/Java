import java.util.concurrent.locks.*;

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
        if(tipo == TipoVeicolo.AUTOMOBILE)
            return AUTOMOBILE;
        else return CAMION;
    }
}


public class PonteStrettoConPortataCU {
    public static void main(String[] args) {
        
        int numAuto, numCamion, nVeicoliTot;
        int i;
        Ponte PonteStretto = new Ponte();

        try {
            numAuto = Integer.parseInt(args[0]);
            numCamion = Integer.parseInt(args[1]);
        } catch(Exception e) {
            System.out.println("Argomenti non validi\nUtilizzo default:");
            numAuto = 4;
            numCamion = 4;
        }

        nVeicoliTot = numAuto * 2 + numCamion * 2;

        Veicolo VeicoloVett [] = new Veicolo[nVeicoliTot];

        System.out.println("Numero automobili: " + numAuto + "\nNumero camion: " + numCamion + "\n\n");


        for(i=0; i<numAuto*2; i+=2) {
            VeicoloVett[i] = new Veicolo(PonteStretto, Direzione.NORD, TipoVeicolo.AUTOMOBILE);
            VeicoloVett[i+1] = new Veicolo(PonteStretto, Direzione.SUD, TipoVeicolo.AUTOMOBILE);
        }
        for(i=numAuto*2; i < numAuto*2+numCamion*2; i+=2) {
            VeicoloVett[i] = new Veicolo(PonteStretto, Direzione.NORD, TipoVeicolo.CAMION);
            VeicoloVett[i+1] = new Veicolo(PonteStretto, Direzione.SUD, TipoVeicolo.CAMION);
        }
    }    
}


class Veicolo implements Runnable {
    Ponte PonteStretto;
    Thread VeicoloThread;
    int direzioneVeicolo;
    int tipoVeicolo;
    int sleepD = 5000;

    Veicolo(Ponte Ponte, int direzione, int tipo) {
        PonteStretto = Ponte;
        direzioneVeicolo = direzione;
        tipoVeicolo = tipo;
        VeicoloThread = new Thread(this);
        VeicoloThread.start();
    }

    public void run() {

        try{
            PonteStretto.richiestaAccesso(direzioneVeicolo, tipoVeicolo);  
        } catch(InterruptedException e) {
            System.err.println(e);
        }

        try {
            Thread.sleep(sleepD);
        } catch(InterruptedException e){
            System.err.println(e);
        }

        try {
            PonteStretto.uscita(direzioneVeicolo, tipoVeicolo);
        } catch(InterruptedException e) {
            System.err.println(e);
        }
    }
}

class Ponte {
    private int inAttesa[][];
    private int inTransito[][];
    private Lock lock;
    private Condition attesaVeicolo[][];
    private final int PORTATA = 60;
    private int caricoAttuale;

    // costruttore
    public Ponte() {
        inAttesa = new int[2][2];
        inTransito = new int[2][2];
        lock = new ReentrantLock();
        attesaVeicolo = new Condition[2][2];
        for(int i=0; i<2; i++) {
            attesaVeicolo[Direzione.NORD][i] = lock.newCondition();
            attesaVeicolo[Direzione.SUD][i] = lock.newCondition();
        }
        caricoAttuale = 0;
    }

    public void richiestaAccesso(int dir, int tipo) throws InterruptedException {
        lock.lock();
        try {
            while(Condizione(dir,tipo)) {
                inAttesa[dir][tipo]++;
                attesaVeicolo[dir][tipo].wait();
                inAttesa[dir][tipo]--;
            }
            // in transito
            inTransito[dir][tipo]++;
            // incremento il carico del ponte
            caricoAttuale += PesoVeicolo.getPeso(tipo);
            // risveglio tutti i miei simili in coda
            attesaVeicolo[dir][tipo].signal();
        } finally {
            // rilascio il monitor (il lock in questo caso)
            lock.unlock();
        }
    }

    public void uscita(int dir, int tipo) throws InterruptedException {
        int otherdir;
        if(dir == Direzione.NORD) 
            otherdir = Direzione.SUD;
        else 
            otherdir = Direzione.NORD;

        lock.lock();

        try {
            show();
            inTransito[dir][tipo]--;
            int caricoTemp = caricoAttuale;
            caricoAttuale -= PesoVeicolo.getPeso(tipo);

            // ponte pieno
            if(caricoTemp + PesoVeicolo.CAMION >= PORTATA) {
                System.out.println("\n\n------ USCITA PORTATA MAX ------\n\n");
                attesaVeicolo[dir][TipoVeicolo.AUTOMOBILE].signal();
                attesaVeicolo[dir][TipoVeicolo.CAMION].signal();
                attesaVeicolo[otherdir][TipoVeicolo.AUTOMOBILE].signal();
                attesaVeicolo[otherdir][TipoVeicolo.CAMION].signal();
            }
            // ponte non pieno
            else {
                if((tipo==TipoVeicolo.CAMION) && (inTransito[dir][tipo]==0)) {
                    attesaVeicolo[otherdir][TipoVeicolo.AUTOMOBILE].signal();
                    if(inTransito[dir][TipoVeicolo.AUTOMOBILE]==0){
                        attesaVeicolo[otherdir][TipoVeicolo.CAMION].signal();
                    }
                }
                if((tipo==TipoVeicolo.AUTOMOBILE) && (inTransito[dir][tipo]==0) && (inTransito[dir][TipoVeicolo.CAMION]==0)) {
                    attesaVeicolo[otherdir][TipoVeicolo.CAMION].signal();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void show() {
        System.out.println("---------\nCarico attuale del ponte: " + caricoAttuale);
        System.out.print("Verso NORD in transito (A-C): " +
                        inTransito[Direzione.NORD][TipoVeicolo.AUTOMOBILE] + " - " +
                        inTransito[Direzione.NORD][TipoVeicolo.CAMION] + ".  ");
        System.out.print("Verso NORD in attesa (A-C): " +
                        inAttesa[Direzione.NORD][TipoVeicolo.AUTOMOBILE] + " - " +
                        inAttesa[Direzione.NORD][TipoVeicolo.CAMION] + ".  ");
        System.out.print("Verso SUD in transito (A-C): " +
                        inTransito[Direzione.SUD][TipoVeicolo.AUTOMOBILE] + " - " +
                        inTransito[Direzione.SUD][TipoVeicolo.CAMION] + ".  ");
        System.out.print("Verso SUD in attesa (A-C): " +
                        inAttesa[Direzione.SUD][TipoVeicolo.AUTOMOBILE] + " - " +
                        inAttesa[Direzione.SUD][TipoVeicolo.CAMION] + ".  ");
        
    }

    private boolean Condizione(int dir, int tipo) {
        int otherdir;
        if(dir == Direzione.NORD)
            otherdir = Direzione.SUD;
        else
            otherdir = Direzione.NORD;
        
        if(
            // SAFATY
            (tipo == TipoVeicolo.AUTOMOBILE && inTransito[otherdir][TipoVeicolo.CAMION]>0) ||
            (tipo == TipoVeicolo.CAMION && inTransito[otherdir][TipoVeicolo.AUTOMOBILE]>0) ||
            (tipo == TipoVeicolo.CAMION && inTransito[otherdir][TipoVeicolo.CAMION]>0) ||
            // PORTATA
            (tipo == TipoVeicolo.AUTOMOBILE && (caricoAttuale + PesoVeicolo.AUTOMOBILE > PORTATA)) ||
            (tipo == TipoVeicolo.CAMION && (caricoAttuale+PesoVeicolo.CAMION>PORTATA)) ||
            // POLITICHE DI PRECEDENZA
            (tipo == TipoVeicolo.CAMION && inAttesa[otherdir][TipoVeicolo.AUTOMOBILE]>0) ||
            (tipo == TipoVeicolo.CAMION && inAttesa[dir][TipoVeicolo.AUTOMOBILE]>0)
        )
            return true;
        return false;
    }

}