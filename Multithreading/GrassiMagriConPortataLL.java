
class TipoVisitatore {
    public static final int GRASSO = 0;
    public static final int MAGRO = 1;
}

class Direzione {
    public static final int NORD = 0;
    public static final int SUD = 1;
}



public class GrassiMagriConPortataLL {
    public static void main(String[] args) {

        int nGrassi, nMagri, nVisitatori, i;
        Corridoio corridoioStretto = new Corridoio();

        try {
            nGrassi = Integer.parseInt(args[0]);
            nMagri = Integer.parseInt(args[1]);
        } catch(Exception e) {
            System.out.println("Utilizzo valori di default");
            nGrassi = 40;
            nMagri = 40;
        }

        System.out.println("[ ***** INIZIO CORRIDOIO CON PORTATA ***** ]");
        System.out.println("Numero persone GRASSE: " + nGrassi);
        System.out.println("Numero persone MAGRE: " + nMagri);

        nVisitatori = nGrassi*2 + nMagri*2;

        Visitatore VisitatoriVett [] = new Visitatore[nVisitatori];

        for(i = 0; i<nGrassi*2; i+=2) {
            VisitatoriVett[i] = new Visitatore(corridoioStretto, Direzione.NORD, TipoVisitatore.GRASSO);
            VisitatoriVett[i+1] = new Visitatore(corridoioStretto, Direzione.SUD, TipoVisitatore.GRASSO);
        }
        for(i = nGrassi*2; i<nGrassi*2+nMagri*2; i+=2) {
            VisitatoriVett[i] = new Visitatore(corridoioStretto, Direzione.NORD, TipoVisitatore.MAGRO);
            VisitatoriVett[i+1] = new Visitatore(corridoioStretto, Direzione.SUD, TipoVisitatore.MAGRO);
        }
    }    
}

class Visitatore implements Runnable {
    Corridoio corridoio;
    int direzioneVisitatore;
    int tipoVisitatore;
    Thread visitaThread;
    int sleepD = 2000;

    public Visitatore(Corridoio c, int dir, int tipo) {
        corridoio = c;
        direzioneVisitatore = dir;
        tipoVisitatore = tipo;
        visitaThread = new Thread(this);
        visitaThread.start();
    }

    public void run() {

        try {
            corridoio.richiestaAccesso(direzioneVisitatore, tipoVisitatore);
        } catch(InterruptedException e) {}

        try {
            Thread.sleep(sleepD);
        } catch(InterruptedException e) {}

        try {
            corridoio.richiestaUscita(direzioneVisitatore, tipoVisitatore);
        } catch(InterruptedException e) {}
    }
}

class Corridoio {
    private int inAttesa[][];
    private int inTransito[][];
    private final int PORTATA = 10;
    private int caricoAttuale;

    public Corridoio(){
        inAttesa = new int[2][2];
        inTransito = new int[2][2];
        caricoAttuale = 0;

        for(int i = 0; i<2; i++) {
            inAttesa[Direzione.NORD][i] = 0;
            inAttesa[Direzione.SUD][i] = 0;
            inTransito[Direzione.NORD][i] = 0;
            inTransito[Direzione.SUD][i] = 0; 
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
        
        // CORRIDOIO PIENO
        if(caricoTemp+1>=PORTATA){
            System.out.println("\n ***** MAX PORTATA ***** \n");
            notifyAll();
        }
        // CORRIDOIO VUOTO
        else if(inTransito[dir][tipo]==0){
            notifyAll();
        }
    }

    private void show() {
        System.out.println("Carico attuale: " + caricoAttuale);
        System.out.print("\nIn transito verso NORD (G - M): " + 
                                            inTransito[Direzione.NORD][TipoVisitatore.GRASSO] + " - " +
                                            inTransito[Direzione.NORD][TipoVisitatore.MAGRO] + ".  --");
        System.out.print("In attesa verso NORD (GRASSI - MAGRI): " + 
                                            inAttesa[Direzione.NORD][TipoVisitatore.GRASSO] + " - " +
                                            inAttesa[Direzione.NORD][TipoVisitatore.MAGRO] + ".  \n");
        System.out.print("\nIn transito verso SUD (G - M): " + 
                                            inTransito[Direzione.SUD][TipoVisitatore.GRASSO] + " - " +
                                            inTransito[Direzione.SUD][TipoVisitatore.MAGRO] + ".  --");
        System.out.print("In attesa verso SUD (GRASSI - MAGRI): " + 
                                            inAttesa[Direzione.SUD][TipoVisitatore.GRASSO] + " - " +
                                            inAttesa[Direzione.SUD][TipoVisitatore.MAGRO] + ".  \n");
     
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
            (tipo == TipoVisitatore.MAGRO && inTransito[otherdir][TipoVisitatore.GRASSO]>0) ||
            (tipo == TipoVisitatore.GRASSO && inTransito[otherdir][TipoVisitatore.MAGRO]>0) ||
            (tipo == TipoVisitatore.GRASSO && inTransito[otherdir][TipoVisitatore.GRASSO]>0) ||

            // PORTATA
            (caricoAttuale+1>PORTATA) ||

            // POLITICHE DI PRECEDENZA
            (tipo == TipoVisitatore.MAGRO && inAttesa[otherdir][TipoVisitatore.GRASSO]>0) ||
            (tipo == TipoVisitatore.MAGRO && inAttesa[dir][TipoVisitatore.GRASSO]>0)
        ) return true;
        else return false;

    }


}
