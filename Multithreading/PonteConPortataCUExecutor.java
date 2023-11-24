import java.util.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

class TipoVeicolo {
    public static final int BUS = 0;
    public static final int AUTO = 1;
    public static final int CAMION = 2;
}

class Direzione {
    public static final int NORD = 0;
    public static final int SUD = 1;
}



public class PonteConPortataCUExecutor {
    
    public static void main(String[] args) {

        int nBus, nAuto, nCamion, numVeicoli, i;
        Ponte ponteStretto = new Ponte();

        try {
            nBus = Integer.parseInt(args[0]);
            nAuto = Integer.parseInt(args[1]);
            nCamion = Integer.parseInt(args[2]);
        } catch(Exception e) {
            System.out.println("Utilizzo valori di default...\n");
            nBus = 10;
            nAuto = 20;
            nCamion = 15;
        }

        numVeicoli = nBus*2 + nAuto*2 + nCamion*2;

        // executor + latch
        ExecutorService exec = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numVeicoli);

        for(i = 0; i < nBus*2; i+=2) {
            exec.execute(new Veicolo(ponteStretto, Direzione.NORD, TipoVeicolo.BUS, latch));
            exec.execute(new Veicolo(ponteStretto, Direzione.SUD, TipoVeicolo.BUS, latch));
        }
        for(i = nBus*2; i < nBus*2 + nAuto*2; i+=2) {
            exec.execute(new Veicolo(ponteStretto, Direzione.NORD, TipoVeicolo.AUTO, latch));
            exec.execute(new Veicolo(ponteStretto, Direzione.SUD, TipoVeicolo.AUTO, latch));
        }
        for(i = nBus*2+nAuto*2; i < nBus*2 + nAuto*2 + nCamion*2; i+=2) {
            exec.execute(new Veicolo(ponteStretto, Direzione.NORD, TipoVeicolo.CAMION, latch));
            exec.execute(new Veicolo(ponteStretto, Direzione.SUD, TipoVeicolo.CAMION, latch));
        }

        try{
            latch.await();
        } catch(InterruptedException e) {
            System.err.println("\nErrore await su latch\n");
            e.printStackTrace();
        }
        System.out.println("\nTutti i task sono terminati!\n");
        exec.shutdown();
    }
}

class Veicolo implements Runnable {
    Ponte ponte;
    int dirVeicolo;
    int tipoVeicolo;
    CountDownLatch cdlatch;

    public Veicolo(Ponte p, int dir, int tipo, CountDownLatch cdl) {
        ponte = p;
        dirVeicolo = dir;
        tipoVeicolo = tipo;
        cdlatch = cdl;
    }

    public void run() {

        try {
            Thread.sleep(5000);
        } catch(InterruptedException e) {}

        try {
            ponte.richiestaAccesso(dirVeicolo, tipoVeicolo);
        } catch(InterruptedException e) {}

        try {
            Thread.sleep(5000);
        } catch(InterruptedException e) {}

        try {
            ponte.richiestaUscita(dirVeicolo, tipoVeicolo);
        } catch(InterruptedException e) {}

        cdlatch.countDown();
    }
}

class Ponte {
    private int inAttesa[][];
    private int inTransito[][];
    private Condition attesaVeicolo[][];
    private int caricoAttuale;
    private final int PORTATA = 20;
    private Lock lock;

    public Ponte() {

        inAttesa = new int[2][3];
        inTransito = new int[2][3];
        caricoAttuale = 0;

        lock = new ReentrantLock();
        attesaVeicolo = new Condition[2][3];
        for(int i=0; i<3; i++) {
            attesaVeicolo[Direzione.NORD][i] = lock.newCondition();
            attesaVeicolo[Direzione.SUD][i] = lock.newCondition();
        }
    }

    public void richiestaAccesso(int dir, int tipo) throws InterruptedException {
        lock.lock();
        try {
            while(Condition(dir, tipo)) {
                inAttesa[dir][tipo]++;
                attesaVeicolo[dir][tipo].await();
                inAttesa[dir][tipo]--;
            }
            inTransito[dir][tipo]++;
            caricoAttuale++;
            if(tipo == TipoVeicolo.BUS || tipo == TipoVeicolo.AUTO) attesaVeicolo[dir][tipo].signal();
        } finally {
            lock.unlock();
        }
    }

    public void richiestaUscita(int dir, int tipo) throws InterruptedException {
        int otherdir;
        if(dir == Direzione.SUD) {
            otherdir = Direzione.NORD;
        } else {
            otherdir = Direzione.SUD;
        }

        lock.lock();
        try {
            show();
            inTransito[dir][tipo]--;
            int caricoTemp = caricoAttuale;
            caricoAttuale--;

            // CASO PONTE PIENO
            if(caricoTemp+1 >= PORTATA) {
                System.out.println("\n[ ***** USCITA MAX PORTATA ***** ]\n");
                for(int i = 0; i<3; i++) {
                    attesaVeicolo[Direzione.NORD][i].signal();
                    attesaVeicolo[Direzione.SUD][i].signal();
                }
            // PONTE VUOTO
            } else if(caricoAttuale==0) {
                if(inAttesa[otherdir][TipoVeicolo.BUS]>0){
                    attesaVeicolo[otherdir][TipoVeicolo.BUS].signal();
                    attesaVeicolo[otherdir][TipoVeicolo.AUTO].signal();
                } else if (inAttesa[otherdir][TipoVeicolo.AUTO]>0) {
                    attesaVeicolo[otherdir][TipoVeicolo.AUTO].signal();
                } else {
                    attesaVeicolo[otherdir][TipoVeicolo.CAMION].signal();
                    attesaVeicolo[dir][TipoVeicolo.CAMION].signal();
                }
            // PONTE NON VUOTO E NON PIENO
            } else {
                if(tipo == TipoVeicolo.BUS && (inTransito[dir][TipoVeicolo.BUS]==0) && (inAttesa[otherdir][TipoVeicolo.AUTO]>0)){
                    attesaVeicolo[otherdir][TipoVeicolo.AUTO].signal();
                }
            }
        } finally {
            lock.unlock();
        }
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

    private boolean Condition(int dir, int tipo) {
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
            (tipo == TipoVeicolo.AUTO && inTransito[otherdir][TipoVeicolo.BUS]>0) ||
            (tipo == TipoVeicolo.AUTO && inTransito[otherdir][TipoVeicolo.CAMION]>0) ||
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
        return false;
    }

}
