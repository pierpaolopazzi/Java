

class Saluti implements Runnable {
    private String nome;

    public Saluti(String nome) {
        this.nome = nome;
    }

    public void run() {
        for(int i=0; i<=5; i++) {
            System.out.println("Ciao da " + nome);
        }
    }
}



public class RunnableTest {
    
    public static void main(String[] args) {
        Saluti s = new Saluti("Runnable test");
        Thread t = new Thread(s);
        t.start();
    }
}
