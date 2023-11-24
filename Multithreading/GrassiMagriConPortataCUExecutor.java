import java.util.concurrent.locks.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CountDownLatch;


class TipoVisitatore {
    public static final int GRASSO = 0;
    public static final int MAGRO = 1;
}

class Direzione {
    public static final int NORD = 0;
    public static final int SUD = 1;
}




public class GrassiMagriConPortataCUExecutor {
    public static void main(String[] args) {
        
        int nGrassi, nMagri, nVisitatori, i;
        Corridoio corridoioStretto = new Corridoio();

        try {
            nGrassi = Integer.parseInt(args[0]);
            nMagri = Integer.parseInt(args[1]);
        } catch(Exception e) {
            System.out.println("Argomenti non validi\nUtilizzo valori di defualt: ");
            nGrassi = 100;
            nMagri = 100;
        }

        System.out.println("Numero di grassi: " + nGrassi);
        System.out.println("Numero di magri: " + nMagri);

        nVisitatori = nGrassi * 2 + nMagri * 2;

        ExecutorService exec = Executors.newFixedThreadPool(40);
        CountDownLatch latch = new CountDownLatch(nVisitatori);

        for(i=0; i < nGrassi*2; i+=2) {
            exec.execute(new Visitatore(corridoioStretto, Direzione.NORD, TipoVisitatore.GRASSO, latch));
            exec.execute(new Visitatore(corridoioStretto, Direzione.SUD, TipoVisitatore.GRASSO, latch));
        }
        for(i = nGrassi*2; i < nGrassi*2+nMagri*2; i+=2) {
            exec.execute(new Visitatore(corridoioStretto, Direzione.NORD, TipoVisitatore.MAGRO, latch));
            exec.execute(new Visitatore(corridoioStretto, Direzione.SUD, TipoVisitatore.MAGRO, latch));
        }


        try {
            latch.await();
        } catch(InterruptedException e) {
            System.err.println(e + "\nErrore InterruptedException su await() di latch");
        }
        System.out.println("\nTutti i task sono terminati\n\n");
        exec.shutdown();
    }    
}


class Visitatore implements Runnable {
    Corridoio corridoio;
    int dirVisitatore;
    int tipoVisitatore;
    CountDownLatch cdlatch;
    int sleepD = 4000;

    public Visitatore(Corridoio c, int direzione, int tipo, CountDownLatch cdl) {
        corridoio = c;
        dirVisitatore = direzione;
        tipoVisitatore = tipo;
        cdlatch = cdl;
    }

    public void run() {

        try {
            corridoio.richiestaAccesso(dirVisitatore, tipoVisitatore);
        } catch(InterruptedException e) {}

        try{
            Thread.sleep(sleepD);
        } catch(InterruptedException e) {}

        try{
            corridoio.richiestaUscita(dirVisitatore, tipoVisitatore);
        } catch(InterruptedException e) {}

        cdlatch.countDown();
    }
}

class Corridoio {
    private Lock lock;
    private int inAttesa[][];
    private int inTransito[][];
    private int totaleInTransito;
    private Condition attesaVisitatore[][];
    private final int PORTATA = 30;
    private int caricoAttuale;

    public Corridoio() {
        inAttesa = new int[2][2];
        inTransito = new int[2][2];
        caricoAttuale = 0;
        totaleInTransito = 0;

        // lock e condition variable
        lock = new ReentrantLock();
        attesaVisitatore = new Condition[2][2];
        for(int i = 0; i<2; i++) {
            attesaVisitatore[Direzione.NORD][i] = lock.newCondition();
            attesaVisitatore[Direzione.SUD][i] = lock.newCondition();
        }
    }

    public void richiestaAccesso(int dir, int tipo) throws InterruptedException {
        lock.lock();
        try {
            while(Condizione(dir, tipo)) {
                inAttesa[dir][tipo]++;
                attesaVisitatore[dir][tipo].await();
                inAttesa[dir][tipo]--;
            }
            inTransito[dir][tipo]++;
            caricoAttuale++;
            totaleInTransito++;
            attesaVisitatore[dir][tipo].signal();
        } finally {
            lock.unlock();
        }
    }

    public void richiestaUscita(int dir, int tipo) throws InterruptedException {
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
            caricoAttuale--;
            totaleInTransito--;
            // PONTE CON PORTATA MAX RAGGIUNTA
            if(caricoTemp+1>=PORTATA) {
                System.out.println("\n [ ****** PORTATA MAX RAGGIUNTA ****** ] \n");
                attesaVisitatore[dir][TipoVisitatore.GRASSO].signal();
                attesaVisitatore[otherdir][TipoVisitatore.GRASSO].signal();
                attesaVisitatore[dir][TipoVisitatore.MAGRO].signal();
                attesaVisitatore[otherdir][TipoVisitatore.MAGRO].signal();
            }
            else {
                if(totaleInTransito==0){
                    if(inAttesa[otherdir][TipoVisitatore.GRASSO]>0){
                        attesaVisitatore[otherdir][TipoVisitatore.GRASSO].signal();
                        attesaVisitatore[otherdir][TipoVisitatore.MAGRO].signal();
                    } else if(inAttesa[dir][TipoVisitatore.GRASSO]>0){
                        attesaVisitatore[dir][TipoVisitatore.GRASSO].signal();
                        attesaVisitatore[dir][TipoVisitatore.MAGRO].signal();
                    } else if(inAttesa[otherdir][TipoVisitatore.MAGRO]>0 && inAttesa[dir][TipoVisitatore.MAGRO]>0){
                        attesaVisitatore[dir][TipoVisitatore.MAGRO].signal();
                        attesaVisitatore[otherdir][TipoVisitatore.MAGRO].signal();
                    }
                }
                
                
            }

        } finally {
            lock.unlock();
        }
    }

    private void show() {
        System.out.println("\nCarico attuale: " + caricoAttuale);
        System.out.print("Direzione NORD (G - M): " +
                                    inTransito[Direzione.NORD][TipoVisitatore.GRASSO] + " - " +
                                    inTransito[Direzione.NORD][TipoVisitatore.MAGRO] + ".  ");
        System.out.print(" In Attesa NORD (G - M): " +
                                    inAttesa[Direzione.NORD][TipoVisitatore.GRASSO] + " - " +
                                    inAttesa[Direzione.NORD][TipoVisitatore.MAGRO] + ".  ");
        System.out.print("\nDirezione SUD (G - M): " +
                                    inTransito[Direzione.SUD][TipoVisitatore.GRASSO] + " - " +
                                    inTransito[Direzione.SUD][TipoVisitatore.MAGRO] + ". ");

        System.out.print(" In Attesa SUD (G - M): " +
                                    inAttesa[Direzione.SUD][TipoVisitatore.GRASSO] + " - " +
                                    inAttesa[Direzione.SUD][TipoVisitatore.MAGRO] + ". ");
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
            (tipo == TipoVisitatore.MAGRO && inTransito[otherdir][TipoVisitatore.GRASSO]>0) ||
            (tipo == TipoVisitatore.GRASSO && inTransito[otherdir][TipoVisitatore.GRASSO]>0) ||
            (tipo == TipoVisitatore.GRASSO && inTransito[otherdir][TipoVisitatore.MAGRO]>0) ||

            // PORTATA
            (caricoAttuale+1>PORTATA) ||
            
            // POLITICHE DI PRECEDENZA
            (tipo == TipoVisitatore.MAGRO && inAttesa[otherdir][TipoVisitatore.GRASSO]>0) ||
            (tipo == TipoVisitatore.MAGRO && inAttesa[dir][TipoVisitatore.GRASSO]>0)
        ) return true;
        else return false;
    }
}
