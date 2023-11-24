import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;


class TipoPersona {
    public static final int PEDONI = 0;
    public static final int DISABILI = 1;
    public static final int SPAZZINI = 2;
}

class Direzione {
    public static final int NORD = 0;
    public static final int SUD = 1;
}
/* 
class Distribution {
    List<Double> probs = new ArrayList<>();
    List<Integer> events = new ArrayList<>();
    double sumProb;
    Random rand = new Random();

    public Distribution(Map<Integer,Double> probs) {
        for(Integer event : probs.keySet()){
            sumProb+=probs.get(event);
            events.add(event);
            this.probs.add(probs.get(event));
        }
    }

    public Integer sample() {
        double prob = rand.nextDouble() * sumProb;
        int i;
        for(i=0; prob>0; i++){
            prob-= probs.get(i);
        }
        return events.get(i-1);
    }
}
*/

public class PontePedonaleLLExecutor {
    public static void main(String[] args) {

        int nPedoni, nDisabili, nSpazzini, numPassanti, i;
        Ponte ponteStretto = new Ponte();

        try {
            nPedoni = Integer.parseInt(args[0]);
            nDisabili = Integer.parseInt(args[1]);
            nSpazzini = Integer.parseInt(args[2]);
        } catch(Exception e) {
            System.out.println("Utilizzo valori di default...");
            nPedoni = 50;
            nDisabili = 50;
            nSpazzini = 20;
        }

        numPassanti = nPedoni*2 + nDisabili*2 + nSpazzini*2;

        ExecutorService exec = Executors.newFixedThreadPool(40);
        CountDownLatch latch = new CountDownLatch(numPassanti);

        for(i = 0; i<nDisabili*2; i+=2) {
            exec.execute(new Passante(ponteStretto, Direzione.NORD, TipoPersona.DISABILI, latch));
            exec.execute(new Passante(ponteStretto, Direzione.SUD, TipoPersona.DISABILI, latch));
        }
        for(i = nDisabili*2; i<nDisabili*2+nPedoni*2; i+=2) {
            exec.execute(new Passante(ponteStretto, Direzione.NORD, TipoPersona.PEDONI, latch));
            exec.execute(new Passante(ponteStretto, Direzione.SUD, TipoPersona.PEDONI, latch));
        }
        for(i = nDisabili*2+nPedoni*2; i<nDisabili*2+nPedoni*2+nSpazzini; i+=2) {
            exec.execute(new Passante(ponteStretto, Direzione.NORD, TipoPersona.SPAZZINI, latch));
            exec.execute(new Passante(ponteStretto, Direzione.SUD, TipoPersona.SPAZZINI, latch));
        }

        try {
            latch.await();
        } catch(InterruptedException e) {
            System.err.println(e + ": Errore su await() di latch");
        }
        System.out.println("\n\n Tutti i task sono terminati! \n\n");
        exec.shutdown();
    }
}

class Passante implements Runnable {
    Ponte ponte;
    int direzionePassante;
    int tipoPersona;
    CountDownLatch cdlatch;

    Passante(Ponte p, int dir, int tipo, CountDownLatch latch) {
        ponte = p;
        direzionePassante = dir;
        tipoPersona = tipo;
        cdlatch = latch;
    }

    public void run() {

        try {
            ponte.richiestaAccesso(direzionePassante, tipoPersona);
        } catch(InterruptedException e) {}

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {}

        try {
            ponte.richiestaUscita(direzionePassante, tipoPersona);
        } catch(InterruptedException e) {}

        cdlatch.countDown();
    }
}

class Ponte {
    private int inTransito[][];
    private int inAttesa[][];
    private int totPersone;
 
    public Ponte() {
        inTransito = new int[2][3];
        inAttesa = new int[2][3];
        totPersone = 0;

        for(int i = 0; i < 3; i++) {
            inTransito[Direzione.NORD][i] = 0;
            inTransito[Direzione.SUD][i] = 0;
            inAttesa[Direzione.NORD][i] = 0;
            inAttesa[Direzione.SUD][i] = 0; 
        }
    }

    public synchronized void richiestaAccesso(int dir, int tipo) throws InterruptedException {
        while(Condizione(dir, tipo)) {
            inAttesa[dir][tipo]++;
            wait();
            inAttesa[dir][tipo]--;
        }
        inTransito[dir][tipo]++;
        totPersone++;
    }

    public synchronized void richiestaUscita(int dir, int tipo) throws InterruptedException {
        show();
        inTransito[dir][tipo]--;
        totPersone--;
        if(inTransito[dir][tipo]==0) {
            notifyAll();
        } else {
            notifyAll();
        }
    }

    private void show() {
        System.out.println("\nPassanti in transito: " + totPersone);
        System.out.print("In TRANSITO verso NORD (D - P - S): " +
                                        inTransito[Direzione.NORD][TipoPersona.DISABILI] + " - " +
                                        inTransito[Direzione.NORD][TipoPersona.PEDONI] + " - " +
                                        inTransito[Direzione.NORD][TipoPersona.SPAZZINI] + ".  -- ");
        System.out.print("In ATTESA verso NORD (D - P - S): " +
                                        inAttesa[Direzione.NORD][TipoPersona.DISABILI] + " - " +
                                        inAttesa[Direzione.NORD][TipoPersona.PEDONI] + " - " +
                                        inAttesa[Direzione.NORD][TipoPersona.SPAZZINI] + ".  \n" );
        System.out.print("In TRANSITO verso SUD (D - P - S): " +
                                        inTransito[Direzione.SUD][TipoPersona.DISABILI] + " - " +
                                        inTransito[Direzione.SUD][TipoPersona.PEDONI] + " - " +
                                        inTransito[Direzione.SUD][TipoPersona.SPAZZINI] + ".  -- ");
        System.out.print("In ATTESA verso SUD (D - P - S): " +
                                        inAttesa[Direzione.SUD][TipoPersona.DISABILI] + " - " +
                                        inAttesa[Direzione.SUD][TipoPersona.PEDONI] + " - " +
                                        inAttesa[Direzione.SUD][TipoPersona.SPAZZINI] + ".  \n" );
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
            (tipo == TipoPersona.DISABILI && inTransito[otherdir][TipoPersona.PEDONI]>0) ||
            (tipo == TipoPersona.DISABILI && inTransito[otherdir][TipoPersona.DISABILI]>0) ||
            (tipo == TipoPersona.DISABILI && inTransito[otherdir][TipoPersona.SPAZZINI]>0) ||
            (tipo == TipoPersona.DISABILI && inTransito[dir][TipoPersona.SPAZZINI]>0) ||
            (tipo == TipoPersona.PEDONI && inTransito[otherdir][TipoPersona.DISABILI]>0) ||
            (tipo == TipoPersona.PEDONI && inTransito[otherdir][TipoPersona.SPAZZINI]>0) ||
            (tipo == TipoPersona.PEDONI && inTransito[dir][TipoPersona.SPAZZINI]>0) ||
            (tipo == TipoPersona.SPAZZINI && inTransito[otherdir][TipoPersona.DISABILI]>0) ||
            (tipo == TipoPersona.SPAZZINI && inTransito[otherdir][TipoPersona.PEDONI]>0) ||
            (tipo == TipoPersona.SPAZZINI && inTransito[dir][TipoPersona.DISABILI]>0) ||
            (tipo == TipoPersona.SPAZZINI && inTransito[dir][TipoPersona.PEDONI]>0) ||
            
            // POLITICHE DI PRECEDENZA
            (tipo == TipoPersona.PEDONI && inAttesa[otherdir][TipoPersona.DISABILI]>0) ||
            (tipo == TipoPersona.SPAZZINI && inAttesa[otherdir][TipoPersona.PEDONI]>0) ||
            (tipo == TipoPersona.SPAZZINI && inAttesa[dir][TipoPersona.DISABILI]>0) ||
            (tipo == TipoPersona.SPAZZINI && inAttesa[dir][TipoPersona.DISABILI]>0)
        ) return true;
        else return false;
    }

}
