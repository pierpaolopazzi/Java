public class BankAccount {
    public static void main(String[] args) {
        SynchronizedBankAccount sba = new SynchronizedBankAccount(0);
        Employee e1 = new Employee("Mason", sba);
        Employee e2 = new Employee("Steinberg", sba);

        e1.start();
        e2.start();
    }
}

class SynchronizedBankAccount {
    private int _value;

    SynchronizedBankAccount(int initialValue) {
        _value = initialValue;
    }

    synchronized public void increase(int amount) {
        int temp = _value;
        _value = temp + amount;
        System.out.println("Saldo attuale: " + _value);
    }

}

class Employee extends Thread{
    private String _name;
    private SynchronizedBankAccount _conto;
    private static final int NUM_OF_DEPOSITS = 30;
    private static final int AMOUNT_PER_DEPOSITS = 100;
    

    // costruttore
    Employee(String name, SynchronizedBankAccount conto) {
        _name = name;
        _conto = conto;
    }

    public void run() {
        try {

            for(int i=0; i<NUM_OF_DEPOSITS; ++i) {
                Thread.sleep(500);
                _conto.increase(AMOUNT_PER_DEPOSITS);

            }
            System.out.println("Impiegato " + _name + ": ha versato un totale di: " + NUM_OF_DEPOSITS*AMOUNT_PER_DEPOSITS);

        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

}