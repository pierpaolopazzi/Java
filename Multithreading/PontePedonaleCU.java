import java.util.concurrent.locks.*;
import java.util.*;

class TipoPersona {
    public static final int PEDONI = 0;
    public static final int DISABILI = 1;
    public static final int SPAZZINI = 2;
}

class Direzione {
    public static final int NORD = 0;
    public static final int SUD = 1;
}



public class PontePedonaleCU {
    public static void main(String[] args) {

        int nPedoni, nDisabili, nSpazzini, nPassantiTot, i;
        Ponte ponteStretto = new Ponte();

        try {
            nPedoni = Integer.parseInt(args[0]);
            nDisabili = Integer.parseInt(args[1]);
            nSpazzini = Integer.parseInt(args[2]);
        } catch(Exception e) {
            System.out.println("Utilizzo valori di default:...");
            nPedoni = 500;
            nDisabili = 1000;
            nSpazzini = 50;
        }

        nPassantiTot = nPedoni*2 + nDisabili*2 + nSpazzini*2;

        System.out.println("[ **** INIZIO **** ]\n");
        System.out.println("Numero PEDONI: " + nPedoni);
        System.out.println("\nNumero DISABILI: " + nDisabili);
        System.out.println("\nNumero SPAZZINI: " + nSpazzini);

        Passante PassanteVett [] = new Passante[nPassantiTot];
        
        for(i = 0; i < nPedoni*2; i+=2) {
            PassanteVett[i] = new Passante(ponteStretto, Direzione.NORD, TipoPersona.PEDONI);
            PassanteVett[i+1] = new Passante(ponteStretto, Direzione.SUD, TipoPersona.PEDONI);
        }
        for(i = nPedoni*2; i < nPedoni*2+nDisabili*2; i+=2) {
            PassanteVett[i] = new Passante(ponteStretto, Direzione.NORD, TipoPersona.DISABILI);
            PassanteVett[i+1] = new Passante(ponteStretto, Direzione.SUD, TipoPersona.DISABILI);
        }
        for(i = nPedoni*2+nDisabili*2; i < nPedoni*2+nDisabili*2+nSpazzini*2; i+=2) {
            PassanteVett[i] = new Passante(ponteStretto, Direzione.NORD, TipoPersona.SPAZZINI);
            PassanteVett[i+1] = new Passante(ponteStretto, Direzione.SUD, TipoPersona.SPAZZINI);
        }
    }    
}

class Passante implements Runnable {
    Ponte ponte;
    int direzionePassante;
    int tipoPassante;
    Thread passanteThread;

    public Passante(Ponte p, int dir, int tipo) {
        ponte = p;
        direzionePassante = dir;
        tipoPassante = tipo;
        passanteThread = new Thread(this);
        passanteThread.start();
    }

    public void run() {

        try {
            ponte.richiestaAccesso(direzionePassante, tipoPassante);
        } catch(InterruptedException e) {}

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {}

        try {
            ponte.richiestaUscita(direzionePassante, tipoPassante);
        } catch(InterruptedException e) {}

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {}
    }
}

class Ponte {
    private Lock lock;
    private int inAttesa[][];
    private int inTransito[][];
    private Condition attesaPassante[][];
    private int totPedoni;

    public Ponte() {
        inAttesa = new int[2][3];
        inTransito = new int[2][3];
        totPedoni = 0;

        // lock e condition variable
        lock = new ReentrantLock();
        attesaPassante = new Condition[2][3];
        for(int i = 0; i<3; i++) {
            attesaPassante[Direzione.NORD][i] = lock.newCondition();
            attesaPassante[Direzione.SUD][i] = lock.newCondition();
        }
    }

    public void richiestaAccesso(int dir, int tipo) throws InterruptedException {
        lock.lock();
        try {
            while(Condizione(dir, tipo)) {
                inAttesa[dir][tipo]++;
                attesaPassante[dir][tipo].await();
                inAttesa[dir][tipo]--;
            }
            inTransito[dir][tipo]++;
            totPedoni++;
            attesaPassante[dir][tipo].signal();
        } finally {
            lock.unlock();
        }
    }

    public void richiestaUscita(int dir, int tipo) throws InterruptedException {
        int otherdir;
        if(dir == Direzione.NORD) {
            otherdir = Direzione.SUD;
        } else {
            otherdir = Direzione.NORD;
        }

        lock.lock();
        try {
            show();
            inTransito[dir][tipo]--;
            totPedoni--;
            if(totPedoni==0) {
                if(tipo == TipoPersona.DISABILI){
                    attesaPassante[otherdir][TipoPersona.DISABILI].signal();
                    attesaPassante[otherdir][TipoPersona.PEDONI].signal();
                    attesaPassante[otherdir][TipoPersona.SPAZZINI].signal();
                    attesaPassante[dir][TipoPersona.SPAZZINI].signal();
                } else if(tipo == TipoPersona.PEDONI){
                    attesaPassante[otherdir][TipoPersona.DISABILI].signal();
                    attesaPassante[otherdir][TipoPersona.PEDONI].signal();
                    attesaPassante[otherdir][TipoPersona.SPAZZINI].signal();
                    attesaPassante[dir][TipoPersona.SPAZZINI].signal();
                } else {
                    attesaPassante[dir][TipoPersona.DISABILI].signal();
                    attesaPassante[dir][TipoPersona.PEDONI].signal();
                    attesaPassante[otherdir][TipoPersona.DISABILI].signal();
                    attesaPassante[otherdir][TipoPersona.PEDONI].signal();
                    attesaPassante[otherdir][TipoPersona.SPAZZINI].signal();
                }
            }

        } finally {
            lock.unlock();
        }
    }

    private void show() {
        System.out.println("\nCarico ponte: " + totPedoni);
        System.out.print("In TRANSITO verso NORD (D - P - S): " +
                                    inTransito[Direzione.NORD][TipoPersona.DISABILI] + " - " +
                                    inTransito[Direzione.NORD][TipoPersona.PEDONI] + " - " +
                                    inTransito[Direzione.NORD][TipoPersona.SPAZZINI] + ".  --");
        System.out.print("In ATTESA verso NORD (D - P - S): " +
                                    inAttesa[Direzione.NORD][TipoPersona.DISABILI] + " - " +
                                    inAttesa[Direzione.NORD][TipoPersona.PEDONI] + " - " +
                                    inAttesa[Direzione.NORD][TipoPersona.SPAZZINI] + ". \n");
        System.out.print("In TRANSITO verso SUD (D - P - S): " +
                                    inTransito[Direzione.SUD][TipoPersona.DISABILI] + " - " +
                                    inTransito[Direzione.SUD][TipoPersona.PEDONI] + " - " +
                                    inTransito[Direzione.SUD][TipoPersona.SPAZZINI] + ".  --");
        System.out.print("In ATTESA verso SUD (D - P - S): " +
                                    inAttesa[Direzione.SUD][TipoPersona.DISABILI] + " - " +
                                    inAttesa[Direzione.SUD][TipoPersona.PEDONI] + " - " +
                                    inAttesa[Direzione.SUD][TipoPersona.SPAZZINI] + ".  --");       
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
            (tipo == TipoPersona.PEDONI && inTransito[otherdir][TipoPersona.DISABILI]>0) ||
            (tipo == TipoPersona.DISABILI && inTransito[otherdir][TipoPersona.DISABILI]>0) ||
            (tipo == TipoPersona.DISABILI && inTransito[otherdir][TipoPersona.PEDONI]>0) ||
            (tipo == TipoPersona.PEDONI && inTransito[otherdir][TipoPersona.SPAZZINI]>0) ||
            (tipo == TipoPersona.DISABILI && inTransito[otherdir][TipoPersona.SPAZZINI]>0) ||
            (tipo == TipoPersona.PEDONI && inTransito[dir][TipoPersona.SPAZZINI]>0) ||
            (tipo == TipoPersona.DISABILI && inTransito[dir][TipoPersona.SPAZZINI]>0) ||
            (tipo == TipoPersona.SPAZZINI && inTransito[otherdir][TipoPersona.PEDONI]>0) ||
            (tipo == TipoPersona.SPAZZINI && inTransito[otherdir][TipoPersona.DISABILI]>0) ||
            (tipo == TipoPersona.SPAZZINI && inTransito[dir][TipoPersona.DISABILI]>0) ||
            (tipo == TipoPersona.SPAZZINI && inTransito[dir][TipoPersona.PEDONI]>0) ||

            // POLITICHE DI PRECEDENZA
            (tipo == TipoPersona.PEDONI && inAttesa[dir][TipoPersona.DISABILI]>0) ||
            (tipo == TipoPersona.PEDONI && inAttesa[otherdir][TipoPersona.DISABILI]>0) ||
            (tipo == TipoPersona.SPAZZINI && inAttesa[dir][TipoPersona.DISABILI]>0) ||
            (tipo == TipoPersona.SPAZZINI && inAttesa[otherdir][TipoPersona.DISABILI]>0) ||
            (tipo == TipoPersona.SPAZZINI && inAttesa[dir][TipoPersona.PEDONI]>0) ||
            (tipo == TipoPersona.SPAZZINI && inAttesa[otherdir][TipoPersona.PEDONI]>0)
        ) return true;
        else return false;

    }


}
