//package CanaleSuezLowLevel;

import java.util.*;


class TipologiaNave {
	public static final int PASSEGGERI = 0;
	public static final int PETROLIERA = 1;
	public static final int MERCI = 2;
}


public class SuezLL {

	public static void main(String[] args) {

		int NumPasseggeri, NumPetroliere, NumMerci, NumNaviTot, i;
		Canale CanaleSuez = new Canale();

		try {
			NumPasseggeri = Integer.parseInt(args[0]);
			NumPetroliere = Integer.parseInt(args[1]);
			NumMerci = Integer.parseInt(args[2]);
		}
		catch (Exception e) {
			System.err.println("uso: java Suez NumPasseggeri NumPetroliere NumMerci");
			System.err.println("continuo con valori di default:");
			System.err.println();
			NumPasseggeri = 2;
			NumPetroliere = 2;
			NumMerci = 2;
		}

		System.out.println("Inizio Suez:");
		System.out.println("- numero di navi passeggere:\t" + NumPasseggeri);
		System.out.println("- numero di navi petroliere:\t" + NumPetroliere);
		System.out.println("- numero di navi merci:\t\t" + NumMerci);
		System.out.println();

		NumNaviTot = NumPasseggeri + NumPetroliere + NumMerci;

		Nave NaveVett [] = new Nave [NumNaviTot];

		for(i=0;i<NumPasseggeri;i++)
			NaveVett[i] = new Nave(CanaleSuez, TipologiaNave.PASSEGGERI);
		for(i=NumPasseggeri;i<NumPasseggeri+NumPetroliere;i++)
			NaveVett[i] = new Nave(CanaleSuez, TipologiaNave.PETROLIERA);
		for(i=NumPasseggeri+NumPetroliere;i<NumNaviTot;i++)
			NaveVett[i] = new Nave(CanaleSuez, TipologiaNave.MERCI);
	}
}


class Nave implements Runnable {

	Canale CanSuez;
	Thread NaveThread;
	int TipoNave;

	Nave (Canale c, int tipo) {
		CanSuez = c;
		TipoNave = tipo;
		NaveThread  = new Thread (this);
		NaveThread.start();
	}

	public void run() {

		Random AttesaRandom = new Random();

        while(true)  {

            try {
                int randomInteger = AttesaRandom.nextInt(10);
                Thread.sleep(randomInteger*500);
            } catch(InterruptedException e){}

            try {
                CanSuez.richiestaAccesso(TipoNave);
            } catch(InterruptedException e){}

            try {
                int randomInteger = AttesaRandom.nextInt(10);
                Thread.sleep(randomInteger*500);
            } catch(InterruptedException e){}

            try {
                CanSuez.uscita(TipoNave);
            } catch(InterruptedException e){}
        }
	}
}

// monitor/risorsa condivisa
class Canale {

	private int inAttesa[];
	private int inTransito[];

	public Canale() {
		int i;
		inAttesa = new int[3];
		inTransito = new int[3];

		// inizializzo a zero le code
		for (i=0; i<3; i++) inAttesa[i] = 0;
		for (i=0; i<3; i++) inTransito[i] = 0;
	}

	public synchronized void richiestaAccesso(int tipo) throws InterruptedException {
		while (Condizione(tipo)) {
			inAttesa[tipo]++;
			wait();
			inAttesa[tipo]--;
		}
		inTransito[tipo]++;
		show();
	}

	public synchronized void uscita(int tipo) throws InterruptedException {
		inTransito[tipo]--;
		show();
		if(inTransito[tipo]==0) {
			notifyAll();
		}
	}

	private synchronized void show() {
		int i;
		System.out.print("Navi nel canale (Ps-Pt-M): ");
		for (i=0;i<3;i++) {
			if (i==TipologiaNave.PASSEGGERI) System.out.print(inTransito[i]+ "-");
			if (i==TipologiaNave.PETROLIERA) System.out.print(inTransito[i] + "-");
			if (i==TipologiaNave.MERCI) System.out.print(inTransito[i] + ". ");
		}
		System.out.println( "Attendono (Ps-Pt-M): " + inAttesa[TipologiaNave.PASSEGGERI] + "-" + inAttesa[TipologiaNave.PETROLIERA] + "-" + inAttesa[TipologiaNave.MERCI] + "");
	}

	private synchronized boolean Condizione (int tipo) {
		if ( (tipo == TipologiaNave.PASSEGGERI && inTransito[TipologiaNave.PETROLIERA] != 0) || // safety
		     (tipo == TipologiaNave.PETROLIERA && inTransito[TipologiaNave.PASSEGGERI] != 0) ||
		     (tipo == TipologiaNave.PETROLIERA && inTransito[TipologiaNave.MERCI] != 0) ||
		     (tipo == TipologiaNave.MERCI && inTransito[TipologiaNave.PETROLIERA] != 0) ||

		     (tipo == TipologiaNave.PETROLIERA && inAttesa[TipologiaNave.PASSEGGERI] != 0)   || // precedenze
		     (tipo == TipologiaNave.MERCI && inAttesa[TipologiaNave.PETROLIERA] != 0)
		   )
			return true;
		else
			return false;
	}
}
