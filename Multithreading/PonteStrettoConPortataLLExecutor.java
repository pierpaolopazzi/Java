import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CountDownLatch;

class TipoVeicolo {
    public static final int AUTOMOBILE = 0;
    public static final int CAMION = 1;
}

// Direzione NORD e SUD
class Direzione {
    public static final int NORD = 0;
    public static final int SUD = 1;
}

// peso veicolo in tonnellate
class PesoVeicolo {
    public static final int AUTOMOBILE = 2;
    public static final int CAMION = 6;
    
    public static int getPeso(int type) {
        switch(type) {
            case TipoVeicolo.AUTOMOBILE:
                return AUTOMOBILE;
            case TipoVeicolo.CAMION:
                return CAMION;
            default:
                return AUTOMOBILE;
        }
    }
}




public class PonteStrettoConPortataLLExecutor {
    public static void main(String[] args) {

        int i;
        int numAuto, numCamion, nVeicoli;

        try {   
            numAuto = Integer.parseInt(args[0]);
            numCamion = Integer.parseInt(args[1]);
        } catch(Exception e) {
            // args[1] o args[0]?
            System.err.println("utilizzo default:400");
            numAuto = 100;
            numCamion = 100;
        }

        nVeicoli = numAuto * 2 + numCamion * 2;

        Ponte ponteStretto = new Ponte();

        // ExecutorService per creare un pool di thread
        ExecutorService exec = Executors.newFixedThreadPool(40);

        System.out.println("[ ***** INIZIO PONTE ***** ]\n");
        System.out.println("NUMERO VEICOLI: " + nVeicoli + "\n");

        // utilizziamo un latch per aspettare la terminazione di tutti i task
        // CountDownLatch va inizializzato con il numero di task che andremo a creare
        CountDownLatch latch = new CountDownLatch(nVeicoli);

        for(i=0; i<numAuto; i++) {
             exec.execute(new Veicolo(ponteStretto, Direzione.NORD, TipoVeicolo.AUTOMOBILE, latch));
             exec.execute(new Veicolo(ponteStretto, Direzione.NORD, TipoVeicolo.CAMION, latch));
             exec.execute(new Veicolo(ponteStretto, Direzione.SUD, TipoVeicolo.AUTOMOBILE, latch));
             exec.execute(new Veicolo(ponteStretto, Direzione.SUD, TipoVeicolo.CAMION, latch));
        }


        // chiamo wait su latch per aspettarne la terminazione
        try {
            latch.await();
        } catch(InterruptedException e) {
            System.err.println("Ricevuta InterruptedException durante await() su latch");
            e.printStackTrace();
        }
        System.out.println("[ **** TUTTI I TASK SONO TERMINATI **** ]");
        exec.shutdown();
    }
}


class Veicolo implements Runnable {
    Ponte ponte;
    int direzioneVeicolo;
    int tipoVeicolo;
    CountDownLatch cdlatch;
    static final int sleepD = 1000;

    public Veicolo(Ponte p, int direzione, int tipo, CountDownLatch latch) {
        ponte = p;
        direzioneVeicolo = direzione;
        tipoVeicolo = tipo;
        cdlatch = latch;
    }

    public void run() {
        // creo una variabile attesaRandom;
       
        try {
            ponte.richiestaAccesso(direzioneVeicolo, tipoVeicolo);
        } catch(InterruptedException e) {
            System.err.println(e);
        }


        try {
            Thread.sleep(sleepD);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }


        try {
            ponte.uscita(direzioneVeicolo, tipoVeicolo);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        // decremento il CountDownLatch
        cdlatch.countDown();

    }
}


class Ponte {

    private int inAttesa[][];
    private int inTransito[][];
    private final int PORTATA = 60;
    private int caricoAttuale;

    public Ponte() {
        inAttesa = new int[2][2];
        inTransito = new int[2][2];
        caricoAttuale = 0;
    }

    public synchronized void richiestaAccesso(int dir, int tipo) throws InterruptedException {
        while(Condizione(dir,tipo)) {
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

        if(caricoTemp + PesoVeicolo.CAMION >= PORTATA) {
            System.out.println("[\n\n***** USCITA MAX PORTATA *****]\n\n");
            notifyAll();
        }

        if(inTransito[dir][tipo]==0) {
            notifyAll();
        }   
    }

    private void show() {
        System.out.println("--------------\nCarico ponte " + caricoAttuale);
        System.out.print("Verso NORD in transito (A-C): " + 
                                    inTransito[Direzione.NORD][TipoVeicolo.AUTOMOBILE] + " - " +
                                    inTransito[Direzione.NORD][TipoVeicolo.CAMION] + ".");
        System.out.print(" Verso NORD in attesa (A-C): " + 
                                    inAttesa[Direzione.NORD][TipoVeicolo.AUTOMOBILE] + " - " +
                                    inAttesa[Direzione.NORD][TipoVeicolo.CAMION] + ".");
        System.out.print("\nVerso SUD in transito (A-C): " + 
                                    inTransito[Direzione.SUD][TipoVeicolo.AUTOMOBILE] + " - " +
                                    inTransito[Direzione.SUD][TipoVeicolo.CAMION] + ".");
        System.out.print(" Verso SUD in attesa (A-C): " + 
                                    inAttesa[Direzione.SUD][TipoVeicolo.AUTOMOBILE] + " - " +
                                    inAttesa[Direzione.SUD][TipoVeicolo.CAMION] + "\n--------------\n");                                     
    }

    private boolean Condizione(int dir, int tipo) {
        int otherDir;
        if(dir == Direzione.NORD) {
            otherDir = Direzione.SUD;
        } else {
            otherDir = Direzione.NORD;
        }

        if(
            // SAFATY
            (tipo == TipoVeicolo.AUTOMOBILE && inTransito[otherDir][TipoVeicolo.CAMION]>0) ||
            (tipo == TipoVeicolo.CAMION && inTransito[otherDir][TipoVeicolo.AUTOMOBILE]>0) ||
            (tipo == TipoVeicolo.CAMION && inTransito[otherDir][TipoVeicolo.CAMION]>0) ||

            // PORTATA
            (tipo == TipoVeicolo.AUTOMOBILE && (caricoAttuale + PesoVeicolo.getPeso(tipo)>PORTATA)) ||
            (tipo == TipoVeicolo.CAMION && (caricoAttuale + PesoVeicolo.getPeso(tipo)>PORTATA)) ||

            // PRECEDENZA
            (tipo == TipoVeicolo.CAMION && inAttesa[otherDir][TipoVeicolo.AUTOMOBILE]>0) ||
            (tipo == TipoVeicolo.CAMION && inAttesa[dir][TipoVeicolo.AUTOMOBILE]>0)
        ) return true;

        return false;
    }

}
