import java.util.concurrent.locks.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
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
        switch(tipo) {
            case TipoVeicolo.AUTOMOBILE:
                return AUTOMOBILE;
            case TipoVeicolo.CAMION:
                return CAMION;
            default:
                return AUTOMOBILE;
        }
    }
}


public class PonteStrettoConPortataCUExecutor {
    public static void main(String[] args) {

        int numAuto, numCamion, nVeicoli, i;
        Ponte PonteStretto = new Ponte();

        try {
            numAuto = Integer.parseInt(args[0]);
            numCamion = Integer.parseInt(args[1]);
        } catch(Exception e) {
            System.out.println("Argomenti sbagliati\nUso valori di default: \n");
            numAuto = 20;
            numCamion = 20;
        }

        System.out.println("Numero automobili: " + numAuto);
        System.out.println("\nNumero Camion: " + numCamion);

        nVeicoli = numAuto * 2 + numCamion * 2;

        // creo executor
        ExecutorService exec = Executors.newFixedThreadPool(40);
        // utilizziamo un latch per aspettare la terminazione di tutti i task
        // inizializziamo il latch con il numero totali di task
        CountDownLatch latch = new CountDownLatch(nVeicoli);

        for(i = 0; i < numAuto * 2; i+=2) {
            exec.execute(new Veicolo(PonteStretto, Direzione.NORD, TipoVeicolo.AUTOMOBILE, latch));
            exec.execute(new Veicolo(PonteStretto, Direzione.SUD, TipoVeicolo.AUTOMOBILE, latch));
        }
        for(i=numAuto*2; i<numAuto*2+numCamion*2;i+=2) {
            exec.execute(new Veicolo(PonteStretto, Direzione.NORD, TipoVeicolo.CAMION, latch));
            exec.execute(new Veicolo(PonteStretto, Direzione.SUD, TipoVeicolo.CAMION, latch));
        } 

        try {
            latch.await();
        } catch(InterruptedException e) {
            System.err.println("Errore: InterruptedException  durante await() su latch\n");
            e.printStackTrace();
        }
        System.out.println("\nTutti i task sono terminati\n");
        exec.shutdown();
    }
}

class Veicolo implements Runnable {
    Ponte ponte;
    int direzioneVeicolo;
    int tipoVeicolo;
    CountDownLatch cdl;

    Veicolo(Ponte p, int dir, int tipo, CountDownLatch latch) {
        ponte = p;
        direzioneVeicolo = dir;
        tipoVeicolo = tipo;
        cdl = latch;
    }

    public void run() {

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            System.err.println(e);
        }

        try {
            ponte.richiestaAccesso(direzioneVeicolo, tipoVeicolo);
        } catch(InterruptedException e) {
            System.err.println(e);
        }

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            System.err.println(e);
        }

        try {
            ponte.uscita(direzioneVeicolo, tipoVeicolo);
        } catch(InterruptedException e) {
            System.err.println(e);
        }

        // decremento il latch
        cdl.countDown();
    }
}


class Ponte {
    private Lock lock;
    private int inAttesa[][];
    private int inTransito[][];
    private Condition attesaVeicolo[][];
    private final int PORTATA = 60;
    private int caricoAttuale;

    Ponte() {
        inAttesa = new int[2][2];
        inTransito = new int[2][2];

        // creo lock e condition variable
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
                attesaVeicolo[dir][tipo].await();
                inAttesa[dir][tipo]--;
            }
            inTransito[dir][tipo]++;
            caricoAttuale += PesoVeicolo.getPeso(tipo);
            attesaVeicolo[dir][tipo].signal();
        } finally {
            lock.unlock();
        }
    }

    public void uscita(int dir, int tipo) throws InterruptedException {
        int otherdir;
        if(dir == Direzione.NORD){
            otherdir = Direzione.SUD;
        } else {
            otherdir = Direzione.NORD;
        }

        lock.lock();
        try {
            show();
            inTransito[dir][tipo]--;
            int caricoTemp = caricoAttuale;
            caricoAttuale -= PesoVeicolo.getPeso(tipo);

            if(caricoTemp + PesoVeicolo.CAMION >= PORTATA) {
                System.out.println("\n\nPORTATA MAX RAGGIUNTA\n\n");
                attesaVeicolo[dir][TipoVeicolo.AUTOMOBILE].signal();
                attesaVeicolo[otherdir][TipoVeicolo.AUTOMOBILE].signal();
                attesaVeicolo[dir][TipoVeicolo.CAMION].signal();
                attesaVeicolo[otherdir][TipoVeicolo.AUTOMOBILE].signal();
            }
            // ponte non pieno
            else{
                if(tipo==TipoVeicolo.CAMION && (inTransito[dir][tipo])==0){
                    attesaVeicolo[otherdir][TipoVeicolo.AUTOMOBILE].signal();
                    if((inTransito[dir][TipoVeicolo.AUTOMOBILE]==0)){
                        attesaVeicolo[otherdir][TipoVeicolo.CAMION].signal();
                    }
                }
                if(tipo == TipoVeicolo.AUTOMOBILE && (inTransito[dir][tipo]==0) && (inTransito[dir][TipoVeicolo.CAMION]==0)) {
                    attesaVeicolo[otherdir][TipoVeicolo.CAMION].signal();
                }
            }
        }finally {
            lock.unlock();
        }
    }

    private void show() {
		System.out.println("------------------\nCarico ponte: " + caricoAttuale);
		System.out.print("Verso NORD: in Transito (A-C): " +
		                 inTransito[Direzione.NORD][TipoVeicolo.AUTOMOBILE] + "-" +
						 inTransito[Direzione.NORD][TipoVeicolo.CAMION] + ".  ");
		System.out.println("Verso NORD: in Attesa (A-C): " +
		                   inAttesa[Direzione.NORD][TipoVeicolo.AUTOMOBILE] + "-" +
		                   inAttesa[Direzione.NORD][TipoVeicolo.CAMION] + ". ");

		System.out.print("Verso  SUD: in Transito (A-C): " +
		                 inTransito[Direzione.SUD][TipoVeicolo.AUTOMOBILE] + "-" +
						 inTransito[Direzione.SUD][TipoVeicolo.CAMION] + ".  ");
						 
		
		System.out.println("Verso  SUD: in Attesa (A-C): " +
		                   inAttesa[Direzione.SUD][TipoVeicolo.AUTOMOBILE] + "-" +
						   inAttesa[Direzione.SUD][TipoVeicolo.CAMION] + "\n"
						   + "------------------\n");
    }

    private boolean Condizione(int dir, int tipo) {
        int otherdir;
        if(dir == Direzione.NORD){
            otherdir = Direzione.SUD;
        } else {
            otherdir = Direzione.NORD;
        }

        if(
            // SAFATY
            (tipo==TipoVeicolo.AUTOMOBILE && inTransito[otherdir][TipoVeicolo.CAMION]>0) ||
            (tipo==TipoVeicolo.CAMION && inTransito[otherdir][TipoVeicolo.AUTOMOBILE]>0) ||
            (tipo==TipoVeicolo.CAMION && inTransito[otherdir][TipoVeicolo.CAMION]>0) ||

            // PORTATA
            (tipo==TipoVeicolo.AUTOMOBILE && (caricoAttuale+PesoVeicolo.AUTOMOBILE>PORTATA)) ||
            (tipo==TipoVeicolo.CAMION && (caricoAttuale+PesoVeicolo.CAMION>PORTATA)) ||

            // POLITICHE DI PRECEDENZA
            (tipo==TipoVeicolo.CAMION && inAttesa[otherdir][TipoVeicolo.AUTOMOBILE]>0) ||
            (tipo==TipoVeicolo.CAMION && inAttesa[dir][TipoVeicolo.AUTOMOBILE]>0)
        )
            return true;
        else return false;
    
    }
}