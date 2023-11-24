import java.util.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CountDownLatch;

class TipoVeicolo {
    public static final int AUTO = 0;
    public static final int CAMION = 1;
    public static final int SPAZZANEVE = 2;
}

class Direzione {
    public static final int NORD = 0;
    public static final int SUD = 1;
}



public class StradaDiMontagnaCUExecutor {
    public static void main(String[] args) {

        int nAuto, nCamion, nSpazzaneve, nVeicoli, i;
        Strada stradaStretta = new Strada() ;

        try {
            nAuto = Integer.parseInt(args[0]);
            nCamion = Integer.parseInt(args[1]);
            nSpazzaneve = Integer.parseInt(args[2]);
        } catch(Exception e) {
            System.out.println("Uso valori di default...\n");
            nAuto = 20;
            nCamion = 20;
            nSpazzaneve = 3;
        }

        System.out.println("Inizio programma");
        System.out.println("Numero di auto: " + nAuto*2);
        System.out.println("Numero di camion: " + nCamion*2);
        System.out.println("Numero di Spazzaneve: " + nSpazzaneve*2);

        nVeicoli = nAuto*2 + nCamion*2 + nSpazzaneve*2;

        ExecutorService exec = Executors.newFixedThreadPool(12);
        CountDownLatch cdl = new CountDownLatch(nVeicoli);

        for(i = 0; i < nAuto*2; i+=2) {
            exec.execute(new Veicolo(stradaStretta, Direzione.NORD, TipoVeicolo.AUTO, cdl));
            exec.execute(new Veicolo(stradaStretta, Direzione.SUD, TipoVeicolo.AUTO, cdl));
        }
        for(i = nAuto*2; i < nAuto*2 + nCamion*2; i+=2) {
            exec.execute(new Veicolo(stradaStretta, Direzione.NORD, TipoVeicolo.CAMION, cdl));
            exec.execute(new Veicolo(stradaStretta, Direzione.SUD, TipoVeicolo.CAMION, cdl));
        }
        for(i = nAuto*2 + nCamion*2; i < nAuto*2 + nCamion*2 + nSpazzaneve*2; i+=2) {
            exec.execute(new Veicolo(stradaStretta, Direzione.NORD, TipoVeicolo.SPAZZANEVE, cdl));
            exec.execute(new Veicolo(stradaStretta, Direzione.SUD, TipoVeicolo.SPAZZANEVE, cdl));
        }

        try {
            cdl.await();
        } catch(InterruptedException e) {
            System.out.println("Errore di await() su latch nel main\n");
        }
        System.out.println("\nTutti i task sono terminati\n");
        exec.shutdown();
    }    
}

class Veicolo implements Runnable {
    Strada strada;
    int dirVeicolo;
    int tipoVeicolo;
    CountDownLatch cdlatch;

    public Veicolo(Strada s, int dir, int tipo, CountDownLatch cdl) {
        strada = s;
        dirVeicolo = dir;
        tipoVeicolo = tipo;
        cdlatch = cdl;
    }

    public void run() {


        try {
            strada.richiestaAccesso(dirVeicolo, tipoVeicolo);
        } catch(InterruptedException e) {}

        try{
            Thread.sleep(3000);
        } catch(InterruptedException e) {}

        try {
            strada.uscita(dirVeicolo, tipoVeicolo);
        } catch(InterruptedException e) {}

        cdlatch.countDown();
    }
}

class Strada {
    private int inAttesa[][];
    private int inTransito[][];
    private Condition attesaVeicolo[][];
    private int nTot;
    private Lock lock;

    public Strada() {
        inAttesa = new int[2][3];
        inTransito = new int[2][3];
        nTot = 0;

        lock = new ReentrantLock();
        attesaVeicolo = new Condition[2][3];
        for(int i=0; i<3; i++) {
            attesaVeicolo[Direzione.NORD][i] = lock.newCondition();
            attesaVeicolo[Direzione.SUD][i] = lock.newCondition();
        }
    }

    public void richiestaAccesso(int dir, int tipo) throws InterruptedException {
        lock.lock();
        try{
            while(Condizione(dir, tipo)) {
                inAttesa[dir][tipo]++;
                attesaVeicolo[dir][tipo].await();
                inAttesa[dir][tipo]--;
            }
            inTransito[dir][tipo]++;
            nTot++;
            attesaVeicolo[dir][tipo].signal();
        } finally {
            lock.unlock();
        }
    }
        

    public void uscita(int dir, int tipo) throws InterruptedException {
        int otherdir;
        if(dir==Direzione.NORD) otherdir = Direzione.SUD;
        else otherdir = Direzione.NORD;

        lock.lock();
        try{
            show();
            inTransito[dir][tipo]--;
            nTot--;
            if(nTot == 0) {
                if(inAttesa[dir][TipoVeicolo.SPAZZANEVE]>0){
                    attesaVeicolo[dir][TipoVeicolo.SPAZZANEVE].signal();
                }
                else if(inAttesa[otherdir][TipoVeicolo.SPAZZANEVE]>0){
                    attesaVeicolo[otherdir][TipoVeicolo.SPAZZANEVE].signal();
                }
                else if((inAttesa[otherdir][TipoVeicolo.AUTO]>0 && inAttesa[dir][TipoVeicolo.AUTO]>0)) {
                    attesaVeicolo[otherdir][TipoVeicolo.AUTO].signal();
                    attesaVeicolo[dir][TipoVeicolo.AUTO].signal();
                }
                else if(inAttesa[otherdir][TipoVeicolo.AUTO]>0){
                            attesaVeicolo[otherdir][TipoVeicolo.AUTO].signal();
                            attesaVeicolo[otherdir][TipoVeicolo.CAMION].signal();
                }
                else if(inAttesa[dir][TipoVeicolo.AUTO]>0) {
                            attesaVeicolo[dir][TipoVeicolo.AUTO].signal();
                            attesaVeicolo[dir][TipoVeicolo.CAMION].signal();
                }
                else {
                    attesaVeicolo[dir][TipoVeicolo.AUTO].signal();
                    attesaVeicolo[otherdir][TipoVeicolo.AUTO].signal();
                    attesaVeicolo[otherdir][TipoVeicolo.CAMION].signal();
                    attesaVeicolo[dir][TipoVeicolo.CAMION].signal();
                }
            } 
            else {
                if(tipo==TipoVeicolo.CAMION && inTransito[dir][TipoVeicolo.CAMION]==0) {
                    attesaVeicolo[otherdir][TipoVeicolo.AUTO].signal();
                }
                else if(tipo == TipoVeicolo.AUTO && inTransito[dir][TipoVeicolo.CAMION]==0) {
                    attesaVeicolo[otherdir][TipoVeicolo.CAMION].signal();
                }
            }

        } finally {
            lock.unlock();
        }

    }

    private void show() {
        System.out.println("\nNumero di veicoli nella strada: " + nTot);
        System.out.print("In TRANSITO verso NORD (A - C - S): " +
                                    inTransito[Direzione.NORD][TipoVeicolo.AUTO] + " - " +
                                    inTransito[Direzione.NORD][TipoVeicolo.CAMION] + " - " +
                                    inTransito[Direzione.NORD][TipoVeicolo.SPAZZANEVE] + ".  ");
        System.out.print("In ATTESA verso NORD (A - C - S): " +
                                    inAttesa[Direzione.NORD][TipoVeicolo.AUTO] + " - " +
                                    inAttesa[Direzione.NORD][TipoVeicolo.CAMION] + " - " +
                                    inAttesa[Direzione.NORD][TipoVeicolo.SPAZZANEVE] + ". \n");
        System.out.print("In TRANSITO verso SUD (A - C - S): " +
                                    inTransito[Direzione.SUD][TipoVeicolo.AUTO] + " - " +
                                    inTransito[Direzione.SUD][TipoVeicolo.CAMION] + " - " +
                                    inTransito[Direzione.SUD][TipoVeicolo.SPAZZANEVE] + ".  ");
        System.out.print("In ATTESA verso SUD (A - C - S): " +
                                    inAttesa[Direzione.SUD][TipoVeicolo.AUTO] + " - " +
                                    inAttesa[Direzione.SUD][TipoVeicolo.CAMION] + " - " +
                                    inAttesa[Direzione.SUD][TipoVeicolo.SPAZZANEVE] + ". \n");
    }

    private boolean Condizione(int dir, int tipo) {
        int otherdir;
        if(dir == Direzione.NORD) otherdir=Direzione.SUD;
        else otherdir = Direzione.NORD;

        if(
            // SAFATY
            (tipo == TipoVeicolo.SPAZZANEVE && nTot>0) ||
            (tipo == TipoVeicolo.AUTO && inTransito[otherdir][TipoVeicolo.CAMION]>0) ||
            (tipo == TipoVeicolo.AUTO && inTransito[otherdir][TipoVeicolo.SPAZZANEVE]>0) ||
            (tipo == TipoVeicolo.AUTO && inTransito[dir][TipoVeicolo.SPAZZANEVE]>0) ||
            (tipo == TipoVeicolo.CAMION && inTransito[otherdir][TipoVeicolo.CAMION]>0) ||
            (tipo == TipoVeicolo.CAMION && inTransito[otherdir][TipoVeicolo.AUTO]>0) ||
            (tipo == TipoVeicolo.CAMION && inTransito[otherdir][TipoVeicolo.SPAZZANEVE]>0) ||
            (tipo == TipoVeicolo.CAMION && inTransito[dir][TipoVeicolo.SPAZZANEVE]>0) ||

            // PRECEDENZE
            (tipo == TipoVeicolo.AUTO && inAttesa[dir][TipoVeicolo.SPAZZANEVE]>0) ||
            (tipo == TipoVeicolo.AUTO && inAttesa[otherdir][TipoVeicolo.SPAZZANEVE]>0) ||
            (tipo == TipoVeicolo.CAMION && inAttesa[dir][TipoVeicolo.SPAZZANEVE]>0) ||
            (tipo == TipoVeicolo.CAMION && inAttesa[otherdir][TipoVeicolo.SPAZZANEVE]>0) ||
            (tipo == TipoVeicolo.CAMION && inAttesa[otherdir][TipoVeicolo.AUTO]>0) 
        ) return true;
        else return false;
    }
}


